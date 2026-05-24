package client.gui;

import common.Vehicle;
import common.VehicleType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class VehicleCanvasController {
    private final LocalizationManager localization;
    private Canvas canvas;
    private GraphicsContext gc;
    private Consumer<Vehicle> onVehicleClicked;

    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;

    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;
    private double baseOffsetX = 0.0;
    private double baseOffsetY = 0.0;

    private List<Vehicle> vehicles = List.of();
    private Vehicle hoveredVehicle = null;
    private Vehicle selectedVehicle = null;
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();

    private static final Color[] MODERN_COLORS = {
            Color.rgb(41, 121, 255), Color.rgb(76, 175, 80), Color.rgb(255, 152, 0),
            Color.rgb(233, 30, 99), Color.rgb(156, 39, 176), Color.rgb(0, 188, 212),
            Color.rgb(255, 87, 34), Color.rgb(63, 81, 181)
    };

    public VehicleCanvasController(LocalizationManager localization) {
        this.localization = localization;
    }

    public Canvas createCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);

        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseExited(e -> {
            hoveredVehicle = null;
            drawAll();
        });

        canvas.setOnMousePressed(e -> {
            isPanning = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            canvas.setCursor(javafx.scene.Cursor.MOVE);
        });

        canvas.setOnMouseDragged(e -> {
            if (isPanning) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                panX += dx;
                panY += dy;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                drawAll();
            }
        });

        canvas.setOnMouseReleased(e -> {
            isPanning = false;
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        canvas.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = 1.1;
            if (e.getDeltaY() < 0) zoomFactor = 1 / 1.1;

            double mouseX = e.getX();
            double mouseY = e.getY();

            panX = mouseX - (mouseX - panX) * zoomFactor;
            panY = mouseY - (mouseY - panY) * zoomFactor;

            zoom *= zoomFactor;
            zoom = Math.max(0.1, Math.min(zoom, 10.0));

            drawAll();
        });

        return canvas;
    }

    public void setOnVehicleClicked(Consumer<Vehicle> callback) {
        this.onVehicleClicked = callback;
    }

    public void updateData(List<Vehicle> vehicles) {
        if (vehicles == null) return;
        this.vehicles = vehicles;

        if (baseScaleX == 1.0 && baseScaleY == 1.0 && vehicles.size() > 0) {
            calculateBaseScaling();
        }
        drawAll();
    }

    private void calculateBaseScaling() {
        if (vehicles.isEmpty()) {
            baseScaleX = 1.0; baseScaleY = 1.0; baseOffsetX = 0; baseOffsetY = 0;
            return;
        }

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Vehicle v : vehicles) {
            double x = v.getCoordinates().getX();
            double y = v.getCoordinates().getY();
            if (x < minX) minX = x; if (x > maxX) maxX = x;
            if (y < minY) minY = y; if (y > maxY) maxY = y;
        }

        double padding = 60;
        double rangeX = maxX - minX + 2 * padding;
        double rangeY = maxY - minY + 2 * padding;

        if (rangeX == 0) rangeX = 1; if (rangeY == 0) rangeY = 1;

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w == 0) w = 600; if (h == 0) h = 400;

        baseScaleX = (w - 2 * padding) / rangeX;
        baseScaleY = (h - 2 * padding) / rangeY;

        baseOffsetX = padding - minX * baseScaleX;
        baseOffsetY = padding - minY * baseScaleY;
    }

    private double toPixelX(double logicalX) {
        return (logicalX * baseScaleX + baseOffsetX) * zoom + panX;
    }

    private double toPixelY(double logicalY) {
        return (canvas.getHeight() - (logicalY * baseScaleY + baseOffsetY)) * zoom + panY;
    }

    private Color getOwnerColor(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isEmpty()) return MODERN_COLORS[0];
        return ownerColors.computeIfAbsent(ownerLogin, login -> {
            int index = Math.abs(login.hashCode()) % MODERN_COLORS.length;
            return MODERN_COLORS[index];
        });
    }

    private void drawAll() {
        if (gc == null) return;

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGridAndAxes();

        for (Vehicle v : vehicles) {
            drawVehicle(v);
        }
    }

    private void drawGridAndAxes() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setStroke(Color.rgb(240, 240, 240));
        gc.setLineWidth(1);

        double step = 50 * zoom;
        if (step < 10) step = 10;

        for (double x = 0; x <= w; x += step) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y <= h; y += step) gc.strokeLine(0, y, w, y);

        double zeroX = toPixelX(0);
        double zeroY = toPixelY(0);

        gc.setStroke(Color.rgb(150, 150, 150));
        gc.setLineWidth(2);

        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            gc.setFill(Color.rgb(100, 100, 100));
            gc.setFont(Font.font(12 * Math.max(0.8, zoom)));
            gc.fillText("X", w - 20 * zoom, zeroY - 5);
        }

        if (zeroX >= 0 && zeroX <= w) {
            gc.strokeLine(zeroX, 0, zeroX, h);
            gc.setFill(Color.rgb(100, 100, 100));
            gc.setFont(Font.font(12 * Math.max(0.8, zoom)));
            gc.fillText("Y", zeroX + 5, 15 * zoom);
            gc.fillText("0", zeroX + 5, zeroY + 15 * zoom);
        }
    }

    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX());
        double py = toPixelY(v.getCoordinates().getY());

        double baseSize = 48 * zoom;
        if (baseSize < 20) baseSize = 20;
        if (baseSize > 80) baseSize = 80;

        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        boolean isSelected = (selectedVehicle != null && selectedVehicle.getId() == v.getId());

        double size = isHovered ? baseSize * 1.2 : baseSize;

        Color baseColor = getOwnerColor(v.getOwnerLogin());

        // Тень для выбранного
        if (isSelected) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.RED);
            drawVehicleShape(v.getType(), px + 3, py + 3, size, baseColor);
            gc.setGlobalAlpha(1.0);
        }

        // Свечение при наведении
        if (isHovered) {
            gc.setGlobalAlpha(0.2);
            gc.setFill(baseColor);
            drawVehicleShape(v.getType(), px, py, size * 1.3, baseColor);
            gc.setGlobalAlpha(1.0);
        }

        // Основной объект
        drawVehicleShape(v.getType(), px, py, size, baseColor);

        // ID
        gc.setFill(Color.rgb(80, 80, 80));
        gc.setFont(Font.font(11 * Math.max(0.8, zoom)));
        gc.fillText(String.valueOf(v.getId()), px - 5, py + size/2 + 20 * zoom);

        // Имя при наведении
        if (isHovered) {
            gc.setFill(Color.rgb(50, 50, 50));
            gc.setFont(Font.font("System Bold", 12 * Math.max(0.8, zoom)));
            gc.fillText(v.getName(), px - 30, py - size/2 - 8);
        }

        // Рамка для выбранного
        if (isSelected) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            double rectSize = size * 1.3;
            gc.strokeRect(px - rectSize/2, py - rectSize/2, rectSize, rectSize);
        }
    }

    private void drawVehicleShape(VehicleType type, double cx, double cy, double size, Color color) {
        gc.setFill(color);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);

        switch (type) {
            case BOAT -> drawBoat(cx, cy, size);
            case SHIP -> drawShip(cx, cy, size);
            case HELICOPTER -> drawHelicopter(cx, cy, size);
            case PLANE -> drawPlane(cx, cy, size);
            case HOVERBOARD -> drawHoverboard(cx, cy, size);
            default -> drawCircle(cx, cy, size);
        }
    }

    private void drawCircle(double cx, double cy, double size) {
        gc.fillOval(cx - size/2, cy - size/2, size, size);
        gc.strokeOval(cx - size/2, cy - size/2, size, size);
    }

    private void drawBoat(double cx, double cy, double size) {
        // Корпус лодки
        gc.beginPath();
        gc.moveTo(cx - size/2, cy + size/4);
        gc.quadraticCurveTo(cx, cy + size/2, cx + size/2, cy + size/4);
        gc.lineTo(cx + size/2, cy - size/4);
        gc.quadraticCurveTo(cx, cy - size/6, cx - size/2, cy - size/4);
        gc.closePath();
        gc.fill();
        gc.stroke();

        // Парус
        gc.beginPath();
        gc.moveTo(cx, cy - size/4);
        gc.lineTo(cx, cy - size/2);
        gc.lineTo(cx + size/3, cy - size/6);
        gc.closePath();
        gc.setFill(Color.WHITE);
        gc.fill();
        gc.stroke();
    }

    private void drawShip(double cx, double cy, double size) {
        // Корпус корабля
        gc.fillRect(cx - size/2, cy - size/6, size, size/3);
        gc.strokeRect(cx - size/2, cy - size/6, size, size/3);

        // Кабина
        gc.setFill(Color.WHITE);
        gc.fillRect(cx - size/4, cy - size/3, size/2, size/4);
        gc.strokeRect(cx - size/4, cy - size/3, size/2, size/4);

        // Труба
        gc.setFill(Color.rgb(100, 100, 100));
        gc.fillRect(cx - size/8, cy - size/2, size/4, size/5);
        gc.strokeRect(cx - size/8, cy - size/2, size/4, size/5);
    }

    private void drawHelicopter(double cx, double cy, double size) {
        // Корпус
        gc.fillOval(cx - size/3, cy - size/4, size/1.5, size/2);
        gc.strokeOval(cx - size/3, cy - size/4, size/1.5, size/2);

        // Кабина
        gc.setFill(Color.rgb(200, 230, 255));
        gc.fillOval(cx - size/4, cy - size/5, size/3, size/3);
        gc.strokeOval(cx - size/4, cy - size/5, size/3, size/3);

        // Винт
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(3);
        gc.beginPath();
        gc.moveTo(cx - size/2, cy - size/2);
        gc.lineTo(cx + size/2, cy - size/2);
        gc.stroke();

        // Хвост
        gc.beginPath();
        gc.moveTo(cx - size/3, cy);
        gc.lineTo(cx - size/2, cy - size/4);
        gc.stroke();
    }

    private void drawPlane(double cx, double cy, double size) {
        // Фюзеляж
        gc.beginPath();
        gc.moveTo(cx - size/2, cy);
        gc.lineTo(cx + size/2, cy - size/6);
        gc.lineTo(cx + size/2, cy + size/6);
        gc.closePath();
        gc.fill();
        gc.stroke();

        // Крылья
        gc.beginPath();
        gc.moveTo(cx - size/6, cy);
        gc.lineTo(cx, cy - size/2);
        gc.lineTo(cx + size/6, cy - size/2);
        gc.lineTo(cx, cy);
        gc.closePath();
        gc.fill();
        gc.stroke();

        gc.beginPath();
        gc.moveTo(cx - size/6, cy);
        gc.lineTo(cx, cy + size/2);
        gc.lineTo(cx + size/6, cy + size/2);
        gc.lineTo(cx, cy);
        gc.closePath();
        gc.fill();
        gc.stroke();
    }

    private void drawHoverboard(double cx, double cy, double size) {
        // Платформа
        gc.beginPath();
        gc.rect(cx - size/2, cy - size/6, size, size/3);
        gc.fill();
        gc.stroke();

        // Колеса
        gc.setFill(Color.rgb(50, 50, 50));
        gc.fillOval(cx - size/2 - size/4, cy - size/4, size/2, size/2);
        gc.strokeOval(cx - size/2 - size/4, cy - size/4, size/2, size/2);

        gc.fillOval(cx + size/4, cy - size/4, size/2, size/2);
        gc.strokeOval(cx + size/4, cy - size/4, size/2, size/2);

        // Спицы колес
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(cx - size/2 - size/4, cy, cx, cy);
        gc.strokeLine(cx + size/4, cy, cx + size/2, cy);
    }

    private void handleMouseMove(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 30 * zoom;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.sqrt(Math.pow(clickX - vx, 2) + Math.pow(clickY - vy, 2));

            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        if ((hoveredVehicle == null && closest != null) ||
                (hoveredVehicle != null && closest == null) ||
                (hoveredVehicle != null && closest != null && hoveredVehicle.getId() != closest.getId())) {
            hoveredVehicle = closest;
            drawAll();
        }
    }

    private void handleMouseClick(MouseEvent event) {
        if (Math.abs(event.getX() - lastMouseX) > 5 || Math.abs(event.getY() - lastMouseY) > 5) return;

        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 25 * zoom;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.sqrt(Math.pow(clickX - vx, 2) + Math.pow(clickY - vy, 2));

            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        if (closest != null) {
            this.selectedVehicle = closest;
            drawAll();
            if (onVehicleClicked != null) onVehicleClicked.accept(closest);
        } else {
            this.selectedVehicle = null;
            drawAll();
        }
    }
}