package client.gui;

import common.Vehicle;
import common.VehicleType;
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

    // Pan & Zoom
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;

    // Масштабирование координат
    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;
    private double baseOffsetX = 0.0;
    private double baseOffsetY = 0.0;

    private List<Vehicle> vehicles = List.of();
    private Vehicle hoveredVehicle = null;
    private Vehicle selectedVehicle = null;
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();

    // Кэш изображений
    private final Map<VehicleType, Image> vehicleImages = new HashMap<>();

    private static final Color[] MODERN_COLORS = {
            Color.rgb(41, 121, 255), Color.rgb(76, 175, 80), Color.rgb(255, 152, 0),
            Color.rgb(233, 30, 99), Color.rgb(156, 39, 176), Color.rgb(0, 188, 212),
            Color.rgb(255, 87, 34), Color.rgb(63, 81, 181)
    };

    public VehicleCanvasController(LocalizationManager localization) {
        this.localization = localization;
        loadVehicleImages();
    }

    /**
     * Загружает изображения ТС.
     * 6-й параметр `false` в конструкторе Image включает СИНХРОННУЮ загрузку,
     * что гарантирует доступность картинки сразу после вызова конструктора.
     */
    private void loadVehicleImages() {
        loadAndCache(VehicleType.BOAT,       "/003-boat.png",      48, 48);  // БЫЛО: /resources/003-boat.png
        loadAndCache(VehicleType.SHIP,       "/002-ship.png",      64, 64);  // БЫЛО: /resources/002-ship.png
        loadAndCache(VehicleType.HELICOPTER, "/005-helicopter.png",48, 48);  // БЫЛО: /resources/005-helicopter.png
        loadAndCache(VehicleType.PLANE,      "/004-plane.png",     64, 64);  // БЫЛО: /resources/004-plane.png
        loadAndCache(VehicleType.HOVERBOARD, "/001-hoverboard.png",48, 48);  // БЫЛО: /resources/001-hoverboard.png
    }

    private void loadAndCache(VehicleType type, String resourcePath, double reqW, double reqH) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                Image img = new Image(url.toExternalForm(), reqW, reqH, true, true, false);
                if (!img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                    vehicleImages.put(type, img);
                    System.out.println("[✓] Изображение загружено: " + resourcePath);
                } else {
                    System.err.println("[!] Ошибка изображения: " + resourcePath);
                }
            } else {
                System.err.println("[!] Ресурс не найден: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[!] Исключение при загрузке " + resourcePath + ": " + e.getMessage());
        }
    }

    public Canvas createCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        setupMouseHandlers();

        // Пересчитываем масштаб при изменении размера Canvas
        canvas.widthProperty().addListener((obs, oldW, newW) -> { if (vehicles.size() > 1) calculateBaseScaling(); drawAll(); });
        canvas.heightProperty().addListener((obs, oldH, newH) -> { if (vehicles.size() > 1) calculateBaseScaling(); drawAll(); });

        return canvas;
    }

    private void setupMouseHandlers() {
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
                panX += e.getX() - lastMouseX;
                panY += e.getY() - lastMouseY;
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
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
            double mouseX = e.getX();
            double mouseY = e.getY();

            // Зум к курсору
            panX = mouseX - (mouseX - panX) * zoomFactor;
            panY = mouseY - (mouseY - panY) * zoomFactor;
            zoom = Math.max(0.1, Math.min(zoom * zoomFactor, 10.0));
            drawAll();
        });
    }

    public void setOnVehicleClicked(Consumer<Vehicle> callback) {
        this.onVehicleClicked = callback;
    }

    public void updateData(List<Vehicle> vehicles) {
        if (vehicles == null) return;
        this.vehicles = vehicles;
        if (vehicles.size() > 1) calculateBaseScaling();
        drawAll();
    }

    private void calculateBaseScaling() {
        if (vehicles.isEmpty()) {
            baseScaleX = 1.0; baseScaleY = 1.0; baseOffsetX = 0; baseOffsetY = 0;
            return;
        }
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Vehicle v : vehicles) {
            double x = v.getCoordinates().getX();
            double y = v.getCoordinates().getY();
            if (x < minX) minX = x; if (x > maxX) maxX = x;
            if (y < minY) minY = y; if (y > maxY) maxY = y;
        }
        double padding = 80;
        double rangeX = Math.max(maxX - minX, 1) + 2 * padding;
        double rangeY = Math.max(maxY - minY, 1) + 2 * padding;
        double w = canvas.getWidth() > 0 ? canvas.getWidth() : 600;
        double h = canvas.getHeight() > 0 ? canvas.getHeight() : 400;

        baseScaleX = (w - 2 * padding) / rangeX;
        baseScaleY = (h - 2 * padding) / rangeY;
        baseOffsetX = padding - minX * baseScaleX;
        baseOffsetY = padding - minY * baseScaleY;
    }

    private double toPixelX(double logicalX) {
        return (logicalX * baseScaleX + baseOffsetX) * zoom + panX;
    }

    private double toPixelY(double logicalY) {
        // Инвертируем Y для декартовой системы координат
        return (canvas.getHeight() - (logicalY * baseScaleY + baseOffsetY)) * zoom + panY;
    }

    private Color getOwnerColor(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isEmpty()) return MODERN_COLORS[0];
        return ownerColors.computeIfAbsent(ownerLogin, login ->
                MODERN_COLORS[Math.abs(login.hashCode()) % MODERN_COLORS.length]);
    }

    private void drawAll() {
        if (gc == null || canvas == null) return;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGridAndAxes();
        for (Vehicle v : vehicles) drawVehicle(v);
    }

    private void drawGridAndAxes() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        gc.setStroke(Color.rgb(235, 235, 235));
        gc.setLineWidth(1);
        double step = Math.max(25, 50 * zoom);

        for (double x = 0; x <= w; x += step) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y <= h; y += step) gc.strokeLine(0, y, w, y);

        double zeroX = toPixelX(0), zeroY = toPixelY(0);
        gc.setStroke(Color.rgb(150, 150, 150));
        gc.setLineWidth(2);
        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            gc.setFill(Color.DARKGRAY);
            gc.setFont(Font.font(11 * Math.max(0.8, zoom)));
            gc.fillText("X", w - 15, zeroY - 5);
        }
        if (zeroX >= 0 && zeroX <= w) {
            gc.strokeLine(zeroX, 0, zeroX, h);
            gc.fillText("Y", zeroX + 5, 15);
            gc.fillText("0", zeroX + 5, zeroY + 15);
        }
    }

    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX());
        double py = toPixelY(v.getCoordinates().getY());
        double baseSize = 48 * zoom;
        baseSize = Math.max(24, Math.min(baseSize, 96));

        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        boolean isSelected = (selectedVehicle != null && selectedVehicle.getId() == v.getId());
        double size = isHovered ? baseSize * 1.25 : baseSize;
        Color baseColor = getOwnerColor(v.getOwnerLogin());

        // Эффект свечения для выделенного/наведённого
        if (isSelected) {
            gc.setGlobalAlpha(0.35);
            gc.setFill(Color.RED);
            gc.fillOval(px - size/2 + 3, py - size/2 + 3, size, size);
            gc.setGlobalAlpha(1.0);
        } else if (isHovered) {
            gc.setGlobalAlpha(0.2);
            gc.setFill(baseColor);
            gc.fillOval(px - size/2, py - size/2, size * 1.2, size * 1.2);
            gc.setGlobalAlpha(1.0);
        }

        // Рисуем картинку или фоллбэк
        drawVehicleImage(v, px, py, size);

        // Подписи
        gc.setFill(isSelected ? Color.RED : Color.rgb(80, 80, 80));
        gc.setFont(Font.font("System Bold", 11 * Math.max(0.8, zoom)));
        gc.fillText(String.valueOf(v.getId()), px - 6, py + size/2 + 16);

        if (isHovered) {
            gc.setFill(Color.rgb(40, 40, 40));
            gc.setFont(Font.font("System Bold", 13 * Math.max(0.8, zoom)));
            double nameWidth = gc.getFont().getSize() * v.getName().length() * 0.6;
            gc.fillText(v.getName(), px - nameWidth/2, py - size/2 - 8);
        }

        // Рамка выделения
        if (isSelected) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2.5);
            double rectSize = size * 1.15;
            gc.strokeRect(px - rectSize/2, py - rectSize/2, rectSize, rectSize);
        }
    }

    private void drawVehicleImage(Vehicle v, double cx, double cy, double size) {
        Image img = vehicleImages.get(v.getType());
        if (img != null && !img.isError() && img.getWidth() > 0) {
            // Рисуем PNG с сохранением прозрачности
            gc.drawImage(img, cx - size/2, cy - size/2, size, size);
        } else {
            // Fallback: цветной круг с первой буквой типа
            Color c = getOwnerColor(v.getOwnerLogin());
            gc.setFill(c);
            gc.fillOval(cx - size/2, cy - size/2, size, size);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(cx - size/2, cy - size/2, size, size);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System Bold", size/2.5));
            String letter = v.getType().toString().substring(0, 1);
            // Центрируем текст по базовой линии
            double yOffset = cy + gc.getFont().getSize() * 0.35;
            gc.fillText(letter, cx - gc.getFont().getSize() * 0.3, yOffset);
        }
    }

    private void handleMouseMove(MouseEvent event) {
        if (isPanning) return;
        double clickX = event.getX(), clickY = event.getY();
        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 35 * zoom;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);
            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        boolean changed = (hoveredVehicle == null && closest != null) ||
                (hoveredVehicle != null && closest == null) ||
                (hoveredVehicle != null && closest != null && hoveredVehicle.getId() != closest.getId());
        if (changed) {
            hoveredVehicle = closest;
            canvas.setCursor(closest != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
            drawAll();
        }
    }

    private void handleMouseClick(MouseEvent event) {
        // Игнорируем клик, если было перетаскивание
        if (Math.abs(event.getX() - lastMouseX) > 5 || Math.abs(event.getY() - lastMouseY) > 5) return;

        double clickX = event.getX(), clickY = event.getY();
        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 30 * zoom;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);
            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        if (closest != null) {
            selectedVehicle = closest;
            drawAll();
            if (onVehicleClicked != null) onVehicleClicked.accept(closest);
        } else {
            selectedVehicle = null;
            drawAll();
        }
    }


    /**
     * Приближает камеру к указанному транспортному средству.
     * Объект оказывается в центре canvas с увеличенным масштабом.
     */
    public void focusOnVehicle(Vehicle vehicle) {
        if (vehicle == null || canvas == null) return;

        // Целевой масштаб (крупный план)
        double targetZoom = 2.5;

        // Координаты объекта в логической системе
        double logicX = vehicle.getCoordinates().getX();
        double logicY = vehicle.getCoordinates().getY();

        // Размеры canvas
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Вычисляем, где должен быть центр экрана в логических координатах при текущем базовом масштабировании
        // Формула обратного преобразования: pixel = (logic * baseScale + baseOffset) * zoom + pan
        // Мы хотим, чтобы logicX/Y оказались в центре экрана (w/2, h/2) при targetZoom

        // 1. Сначала сбрасываем pan, чтобы расчеты были чище, или рассчитываем новый pan относительно текущего baseScale
        // Проще всего рассчитать новые panX и panY так, чтобы:
        // w/2 = (logicX * baseScaleX + baseOffsetX) * targetZoom + newPanX
        // h/2 = (canvasHeight - (logicY * baseScaleY + baseOffsetY)) * targetZoom + newPanY

        double projectedX = (logicX * baseScaleX + baseOffsetX);
        double projectedY = (h - (logicY * baseScaleY + baseOffsetY)); // Инверсия Y уже учтена в toPixelY, но здесь мы работаем с "экранной" проекцией до зума

        // Новые значения панорамирования
        this.panX = (w / 2) - (projectedX * targetZoom);
        this.panY = (h / 2) - (projectedY * targetZoom);

        // Устанавливаем зум
        this.zoom = targetZoom;

        // Обновляем выбранный элемент для подсветки
        this.selectedVehicle = vehicle;

        drawAll();
    }

    /**
     * Сбрасывает вид к исходному состоянию (вместить все объекты).
     */
    public void resetView() {
        this.zoom = 1.0;
        this.panX = 0.0;
        this.panY = 0.0;
        this.selectedVehicle = null; // Снимаем выделение при сбросе вида, если нужно
        // Пересчитываем базовое масштабирование, чтобы вместить все элементы
        if (vehicles.size() > 1) {
            calculateBaseScaling();
        } else if (vehicles.size() == 1) {
            // Если один элемент, можно тоже отцентровать его, но с меньшим зумом, или оставить как есть
            calculateBaseScaling();
        }
        drawAll();
    }
}