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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class VehicleCanvasController {
    private final LocalizationManager localization;
    private Canvas canvas;
    private GraphicsContext gc;
    private Consumer<Vehicle> onVehicleClicked;

    // Переменные для масштаба и смещения (Pan & Zoom)
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;

    // Базовые параметры координатной системы (рассчитываются один раз или при ресайзе)
    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;
    private double baseOffsetX = 0.0;
    private double baseOffsetY = 0.0;

    private List<Vehicle> vehicles = List.of();
    private Vehicle hoveredVehicle = null;
    private Vehicle selectedVehicle = null; // Выбранный пользователем объект

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

        // Обработка перемещения (Pan)
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

        // Обработка зума (Scroll)
        canvas.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = 1.1;
            if (e.getDeltaY() < 0) zoomFactor = 1 / 1.1;

            // Зум относительно позиции мыши
            double mouseX = e.getX();
            double mouseY = e.getY();

            // Корректировка смещения, чтобы точка под мышью оставалась на месте
            panX = mouseX - (mouseX - panX) * zoomFactor;
            panY = mouseY - (mouseY - panY) * zoomFactor;

            zoom *= zoomFactor;
            // Ограничения зума
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

        // ВАЖНО: Не сбрасываем zoom, panX, panY здесь, чтобы сохранить позицию пользователя
        // calculateBaseScaling вызываем только если размеры канваса изменились или это первый запуск
        // Но для простоты, если список пуст или изменился кардинально, можно пересчитать базу,
        // но лучше оставить как есть, чтобы пользователь мог исследовать карту.

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

        // Центрируем начальное положение
        baseOffsetX = padding - minX * baseScaleX;
        baseOffsetY = padding - minY * baseScaleY;
    }

    // Преобразование логических координат (из Vehicle) в пиксельные (на экране с учетом зума и пана)
    private double toPixelX(double logicalX) {
        return (logicalX * baseScaleX + baseOffsetX) * zoom + panX;
    }

    private double toPixelY(double logicalY) {
        // Y инвертирован в JavaFX Canvas (0 сверху), а в декартовой системе обычно снизу.
        // Здесь используем логику из оригинального кода: canvas.getHeight() - ...
        return (canvas.getHeight() - (logicalY * baseScaleY + baseOffsetY)) * zoom + panY;
    }

    // Обратное преобразование: пиксельные координаты (клик мыши) в логические
    private double toLogicalX(double pixelX) {
        return ((pixelX - panX) / zoom - baseOffsetX) / baseScaleX;
    }

    private double toLogicalY(double pixelY) {
        return (canvas.getHeight() - ((pixelY - panY) / zoom) - baseOffsetY) / baseScaleY;
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

        // Рисуем сетку с учетом зума
        double step = 50 * zoom;
        if (step < 10) step = 10;

        // Вертикальные линии
        for (double x = 0; x <= w; x += step) gc.strokeLine(x, 0, x, h);
        // Горизонтальные линии
        for (double y = 0; y <= h; y += step) gc.strokeLine(0, y, w, y);

        // Оси X и Y (логические 0,0)
        double zeroX = toPixelX(0);
        double zeroY = toPixelY(0);

        gc.setStroke(Color.rgb(150, 150, 150));
        gc.setLineWidth(2);

        // Ось X
        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            gc.setFill(Color.rgb(100, 100, 100));
            gc.setFont(Font.font(12 * Math.max(0.8, zoom)));
            gc.fillText("X", w - 20 * zoom, zeroY - 5);
        }
        // Ось Y
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

        double baseSize = 18 * zoom;
        if (baseSize < 5) baseSize = 5;

        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        boolean isSelected = (selectedVehicle != null && selectedVehicle.getId() == v.getId());

        double size = isHovered ? baseSize * 1.2 : baseSize;
        Color baseColor = getOwnerColor(v.getOwnerLogin());

        // Рисуем сам объект
        if (isHovered) {
            gc.setGlobalAlpha(0.2);
            gc.setFill(baseColor);
            drawShape(px, py, size * 1.5, v.getType());
            gc.setGlobalAlpha(1.0);

            gc.setFill(baseColor);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
        } else {
            gc.setFill(baseColor);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
        }
        drawShape(px, py, size, v.getType());

        // ID под объектом
        gc.setFill(Color.rgb(80, 80, 80));
        gc.setFont(Font.font(11 * Math.max(0.8, zoom)));
        gc.fillText(String.valueOf(v.getId()), px - 5, py + size + 14 * zoom);

        // Имя при наведении
        if (isHovered) {
            gc.setFill(Color.rgb(50, 50, 50));
            gc.setFont(Font.font("System Bold", 12 * Math.max(0.8, zoom)));
            gc.fillText(v.getName(), px - 20, py - size - 8);
        }

        // === ВЫДЕЛЕНИЕ ВЫБРАННОГО ОБЪЕКТА ===
        if (isSelected) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            // Рисуем рамку вокруг объекта
            double rectSize = size * 1.5;
            gc.strokeRect(px - rectSize/2, py - rectSize/2, rectSize, rectSize);
        }
    }

    private void drawShape(double cx, double cy, double size, VehicleType type) {
        switch (type) {
            case BOAT -> drawBoat(cx, cy, size);
            case HELICOPTER -> drawHelicopter(cx, cy, size);
            case HOVERBOARD -> drawHoverboard(cx, cy, size);
            case PLANE -> drawPlane(cx, cy, size);
            case SHIP -> drawShip(cx, cy, size);
            default -> drawCircle(cx, cy, size);
        }
    }

    private void drawCircle(double cx, double cy, double r) {
        gc.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        gc.strokeOval(cx - r, cy - r, 2 * r, 2 * r);
    }

    private void drawBoat(double cx, double cy, double size) {
        double[] xP = {cx - size, cx + size, cx};
        double[] yP = {cy + size/2, cy + size/2, cy - size};
        gc.fillPolygon(xP, yP, 3); gc.strokePolygon(xP, yP, 3);
    }

    private void drawHelicopter(double cx, double cy, double size) {
        gc.fillRect(cx - size/2, cy - size/2, size, size);
        gc.strokeRect(cx - size/2, cy - size/2, size, size);
        gc.strokeLine(cx - size, cy - size/2 - 5, cx + size, cy - size/2 - 5);
    }

    private void drawHoverboard(double cx, double cy, double size) {
        gc.fillRoundRect(cx - size/2, cy - size/4, size, size/2, 10, 10);
        gc.strokeRoundRect(cx - size/2, cy - size/4, size, size/2, 10, 10);
    }

    private void drawPlane(double cx, double cy, double size) {
        double[] xP = {cx - size, cx + size, cx, cx - size/2, cx + size/2};
        double[] yP = {cy, cy, cy - size, cy + size/2, cy + size/2};
        gc.fillPolygon(xP, yP, 5); gc.strokePolygon(xP, yP, 5);
    }

    private void drawShip(double cx, double cy, double size) {
        double[] xP = {cx - size, cx + size, cx + size/2, cx - size/2};
        double[] yP = {cy + size/2, cy + size/2, cy - size, cy - size};
        gc.fillPolygon(xP, yP, 4); gc.strokePolygon(xP, yP, 4);
    }

    private void handleMouseMove(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;

        // Радиус попадания зависит от зума
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

        if ((hoveredVehicle == null && closest != null) ||
                (hoveredVehicle != null && closest == null) ||
                (hoveredVehicle != null && closest != null && hoveredVehicle.getId() != closest.getId())) {
            hoveredVehicle = closest;
            drawAll();
        }
    }

    private void handleMouseClick(MouseEvent event) {
        // Игнорируем клик, если было перетаскивание
        if (Math.abs(event.getX() - lastMouseX) > 5 || Math.abs(event.getY() - lastMouseY) > 5) return;

        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 20 * zoom;

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
            // Устанавливаем выбранный объект
            this.selectedVehicle = closest;
            drawAll(); // Перерисовываем, чтобы показать рамку

            if (onVehicleClicked != null) onVehicleClicked.accept(closest);
        } else {
            // Если кликнули в пустоту, снимаем выделение
            this.selectedVehicle = null;
            drawAll();
        }
    }
}