package client.gui;
import common.Vehicle;
import common.VehicleType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    private double zoom = 1.0, panX = 0.0, panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;
    private double baseScaleX = 1.0, baseScaleY = 1.0, baseOffsetX = 0.0, baseOffsetY = 0.0;
    private AnimationTimer animationTimer;
    private double animStartZoom, animStartPanX, animStartPanY, animTargetZoom, animTargetPanX, animTargetPanY;
    private long animStartTime;
    private static final long ANIM_DURATION_MS = 350;
    private List<Vehicle> vehicles = List.of();
    private Vehicle hoveredVehicle = null, selectedVehicle = null;
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();
    private final Map<VehicleType, Image> vehicleImages = new HashMap<>();
    private boolean isDarkMode = false;

    private static final Color[] MODERN_COLORS = { Color.rgb(41, 121, 255), Color.rgb(76, 175, 80), Color.rgb(255, 152, 0), Color.rgb(233, 30, 99), Color.rgb(156, 39, 176), Color.rgb(0, 188, 212), Color.rgb(255, 87, 34), Color.rgb(63, 81, 181) };

    public VehicleCanvasController(LocalizationManager localization) {
        this.localization = localization;
        loadVehicleImages();
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        drawAll();
    }

    private void loadVehicleImages() {
        loadAndCache(VehicleType.BOAT, "/003-boat.png", 48, 48);
        loadAndCache(VehicleType.SHIP, "/002-ship.png", 64, 64);
        loadAndCache(VehicleType.HELICOPTER, "/005-helicopter.png", 48, 48);
        loadAndCache(VehicleType.PLANE, "/004-plane.png", 64, 64);
        loadAndCache(VehicleType.HOVERBOARD, "/001-hoverboard.png", 48, 48);
    }

    private void loadAndCache(VehicleType type, String resourcePath, double reqW, double reqH) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                Image img = new Image(url.toExternalForm(), reqW, reqH, true, true, false);
                if (!img.isError() && img.getWidth() > 0 && img.getHeight() > 0) vehicleImages.put(type, img);
            }
        } catch (Exception ignored) {}
    }

    public Canvas createCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        setupMouseHandlers();
        canvas.widthProperty().addListener((obs, oldW, newW) -> { if (vehicles.size() > 1) calculateBaseScaling(); drawAll(); });
        canvas.heightProperty().addListener((obs, oldH, newH) -> { if (vehicles.size() > 1) calculateBaseScaling(); drawAll(); });
        return canvas;
    }

    private void setupMouseHandlers() {
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseExited(e -> { hoveredVehicle = null; drawAll(); });
        canvas.setOnMousePressed(e -> { isPanning = true; lastMouseX = e.getX(); lastMouseY = e.getY(); canvas.setCursor(javafx.scene.Cursor.MOVE); });
        canvas.setOnMouseDragged(e -> {
            if (isPanning) { panX += e.getX() - lastMouseX; panY += e.getY() - lastMouseY; lastMouseX = e.getX(); lastMouseY = e.getY(); drawAll(); }
        });
        canvas.setOnMouseReleased(e -> { isPanning = false; canvas.setCursor(javafx.scene.Cursor.DEFAULT); });
        canvas.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
            double mouseX = e.getX(), mouseY = e.getY();
            panX = mouseX - (mouseX - panX) * zoomFactor;
            panY = mouseY - (mouseY - panY) * zoomFactor;
            zoom = Math.max(0.1, Math.min(zoom * zoomFactor, 10.0));
            drawAll();
        });
    }

    public void setOnVehicleClicked(Consumer<Vehicle> callback) { this.onVehicleClicked = callback; }

    public void updateData(List<Vehicle> vehicles) { if (vehicles == null) return; this.vehicles = vehicles; if (vehicles.size() > 1) calculateBaseScaling(); drawAll(); }

    private void calculateBaseScaling() {
        if (vehicles.isEmpty()) { baseScaleX = 1.0; baseScaleY = 1.0; baseOffsetX = 0; baseOffsetY = 0; return; }
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Vehicle v : vehicles) { double x = v.getCoordinates().getX(); double y = v.getCoordinates().getY(); if (x < minX) minX = x; if (x > maxX) maxX = x; if (y < minY) minY = y; if (y > maxY) maxY = y; }
        double padding = 80, rangeX = Math.max(maxX - minX, 1) + 2 * padding, rangeY = Math.max(maxY - minY, 1) + 2 * padding;
        double w = canvas.getWidth() > 0 ? canvas.getWidth() : 600, h = canvas.getHeight() > 0 ? canvas.getHeight() : 400;
        baseScaleX = (w - 2 * padding) / rangeX; baseScaleY = (h - 2 * padding) / rangeY;
        baseOffsetX = padding - minX * baseScaleX; baseOffsetY = padding - minY * baseScaleY;
    }

    private double toPixelX(double logicalX) { return (logicalX * baseScaleX + baseOffsetX) * zoom + panX; }
    private double toPixelY(double logicalY) { return (canvas.getHeight() - (logicalY * baseScaleY + baseOffsetY)) * zoom + panY; }
    private Color getOwnerColor(String ownerLogin) { if (ownerLogin == null || ownerLogin.isEmpty()) return MODERN_COLORS[0]; return ownerColors.computeIfAbsent(ownerLogin, login -> MODERN_COLORS[Math.abs(login.hashCode()) % MODERN_COLORS.length]); }

    private void drawAll() {
        if (gc == null || canvas == null) return;
        // Очистка фона в зависимости от темы
        if (isDarkMode) {
            gc.setFill(Color.rgb(30, 41, 59)); // Цвет D_CARD (#1E293B)
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawGridAndAxes();
        for (Vehicle v : vehicles) drawVehicle(v);
    }

    private void drawGridAndAxes() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        Color gridColor, axisColor, textColor;

        if (isDarkMode) {
            gridColor = Color.WHITE.deriveColor(0, 0, 1, 0.1); // Очень тонкая сетка
            axisColor = Color.rgb(148, 163, 184); // Светлые оси
            textColor = Color.rgb(168, 85, 247); // Фиолетовые координаты
        } else {
            gridColor = Color.rgb(235, 235, 235);
            axisColor = Color.rgb(150, 150, 150);
            textColor = Color.DARKGRAY;
        }

        gc.setStroke(gridColor); gc.setLineWidth(1);
        double step = Math.max(25, 50 * zoom);
        for (double x = 0; x <= w; x += step) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y <= h; y += step) gc.strokeLine(0, y, w, y);

        double zeroX = toPixelX(0), zeroY = toPixelY(0);
        gc.setStroke(axisColor); gc.setLineWidth(2);
        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            gc.setFill(textColor); gc.setFont(Font.font(11 * Math.max(0.8, zoom)));
            gc.fillText("X", w - 15, zeroY - 5);
        }
        if (zeroX >= 0 && zeroX <= w) {
            gc.strokeLine(zeroX, 0, zeroX, h);
            gc.fillText("Y", zeroX + 5, 15);
            gc.fillText("0", zeroX + 5, zeroY + 15);
        }
    }

    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX()), py = toPixelY(v.getCoordinates().getY());
        double baseSize = 48 * zoom; baseSize = Math.max(24, Math.min(baseSize, 96));
        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        boolean isSelected = (selectedVehicle != null && selectedVehicle.getId() == v.getId());
        double size = isHovered ? baseSize * 1.25 : baseSize;
        Color baseColor = getOwnerColor(v.getOwnerLogin());

        if (isSelected) { gc.setGlobalAlpha(0.35); gc.setFill(Color.RED); gc.fillOval(px - size/2 + 3, py - size/2 + 3, size, size); gc.setGlobalAlpha(1.0); }
        else if (isHovered) { gc.setGlobalAlpha(0.2); gc.setFill(baseColor); gc.fillOval(px - size/2, py - size/2, size * 1.2, size * 1.2); gc.setGlobalAlpha(1.0); }

        drawVehicleImage(v, px, py, size);

        gc.setFill(isSelected ? Color.RED : (isDarkMode ? Color.rgb(226, 232, 240) : Color.rgb(80, 80, 80)));
        gc.setFont(Font.font("System Bold", 11 * Math.max(0.8, zoom)));
        gc.fillText(String.valueOf(v.getId()), px - 6, py + size/2 + 16);

        if (isHovered) {
            gc.setFill(isDarkMode ? Color.WHITE : Color.rgb(40, 40, 40));
            gc.setFont(Font.font("System Bold", 13 * Math.max(0.8, zoom)));
            double nameWidth = gc.getFont().getSize() * v.getName().length() * 0.6;
            gc.fillText(v.getName(), px - nameWidth/2, py - size/2 - 8);
        }
        if (isSelected) { gc.setStroke(Color.RED); gc.setLineWidth(2.5); double rectSize = size * 1.15; gc.strokeRect(px - rectSize/2, py - rectSize/2, rectSize, rectSize); }
    }

    private void drawVehicleImage(Vehicle v, double cx, double cy, double size) {
        Image img = vehicleImages.get(v.getType());
        if (img != null && !img.isError() && img.getWidth() > 0) { gc.drawImage(img, cx - size/2, cy - size/2, size, size); }
        else {
            Color c = getOwnerColor(v.getOwnerLogin());
            gc.setFill(c); gc.fillOval(cx - size/2, cy - size/2, size, size);
            gc.setStroke(Color.WHITE); gc.setLineWidth(2); gc.strokeOval(cx - size/2, cy - size/2, size, size);
            gc.setFill(Color.WHITE); gc.setFont(Font.font("System Bold", size/2.5));
            String letter = v.getType().toString().substring(0, 1);
            gc.fillText(letter, cx - gc.getFont().getSize() * 0.3, cy + gc.getFont().getSize() * 0.35);
        }
    }

    private void handleMouseMove(MouseEvent event) {
        if (isPanning) return;
        double clickX = event.getX(), clickY = event.getY();
        Vehicle closest = null; double minDist = Double.MAX_VALUE, hitRadius = 35 * zoom;
        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX()), vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);
            if (dist < hitRadius && dist < minDist) { minDist = dist; closest = v; }
        }
        boolean changed = (hoveredVehicle == null && closest != null) || (hoveredVehicle != null && closest == null) || (hoveredVehicle != null && closest != null && hoveredVehicle.getId() != closest.getId());
        if (changed) { hoveredVehicle = closest; canvas.setCursor(closest != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT); drawAll(); }
    }

    private void handleMouseClick(MouseEvent event) {
        if (Math.abs(event.getX() - lastMouseX) > 5 || Math.abs(event.getY() - lastMouseY) > 5) return;
        double clickX = event.getX(), clickY = event.getY();
        Vehicle closest = null; double minDist = Double.MAX_VALUE, hitRadius = 30 * zoom;
        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX()), vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);
            if (dist < hitRadius && dist < minDist) { minDist = dist; closest = v; }
        }
        if (closest != null) { selectedVehicle = closest; drawAll(); if (onVehicleClicked != null) onVehicleClicked.accept(closest); }
        else { selectedVehicle = null; drawAll(); if (event.getClickCount() == 2) resetView(); }
    }

    public List<Vehicle> getVehicles() { return vehicles; }

    public void focusOnVehicle(Vehicle vehicle) {
        if (vehicle == null || canvas == null) return;
        this.selectedVehicle = vehicle;
        double targetZoom = 2.5, w = canvas.getWidth(), h = canvas.getHeight();
        double logicX = vehicle.getCoordinates().getX(), logicY = vehicle.getCoordinates().getY();
        double projectedX = (logicX * baseScaleX + baseOffsetX), projectedY = (h - (logicY * baseScaleY + baseOffsetY));
        double targetPanX = (w / 2.0) - (projectedX * targetZoom), targetPanY = (h / 2.0) - (projectedY * targetZoom);
        startAnimation(targetZoom, targetPanX, targetPanY);
    }

    public void resetView() {
        this.selectedVehicle = null;
        calculateBaseScaling();
        startAnimation(1.0, 0.0, 0.0);
    }

    private void startAnimation(double targetZoom, double targetPanX, double targetPanY) {
        if (animationTimer != null) animationTimer.stop();
        animStartZoom = this.zoom; animStartPanX = this.panX; animStartPanY = this.panY;
        animTargetZoom = targetZoom; animTargetPanX = targetPanX; animTargetPanY = targetPanY;
        animStartTime = System.nanoTime();
        animationTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                long elapsed = (now - animStartTime) / 1_000_000;
                double t = Math.min(1.0, (double) elapsed / ANIM_DURATION_MS);
                double ease = 1 - Math.pow(1 - t, 3);
                zoom = animStartZoom + (animTargetZoom - animStartZoom) * ease;
                panX = animStartPanX + (animTargetPanX - animStartPanX) * ease;
                panY = animStartPanY + (animTargetPanY - animStartPanY) * ease;
                drawAll();
                if (t >= 1.0) { zoom = animTargetZoom; panX = animTargetPanX; panY = animTargetPanY; drawAll(); this.stop(); animationTimer = null; }
            }
        };
        animationTimer.start();
    }
}