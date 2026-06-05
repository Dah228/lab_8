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


//визуализация объектов на холсте
public class VehicleCanvasController {
    private final LocalizationManager localization;
    private Canvas canvas;
    private GraphicsContext gc;
    private Consumer<Vehicle> onVehicleClicked;

    //параметры камеры
    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private boolean isPanning = false;
    private double lastMouseX, lastMouseY;

    //базовое масштабирование
    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;
    private double baseOffsetX = 0.0;
    private double baseOffsetY = 0.0;

    //анимация
    private AnimationTimer animationTimer;
    private double animStartZoom, animStartPanX, animStartPanY;
    private double animTargetZoom, animTargetPanX, animTargetPanY;
    private long animStartTime;
    private static final long ANIM_DURATION_MS = 350;

    //данные
    private List<Vehicle> vehicles = List.of();
    private Vehicle hoveredVehicle = null;
    private Vehicle selectedVehicle = null;
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();
    private final Map<VehicleType, Image> vehicleImages = new HashMap<>();
    private boolean isDarkMode = false;

    private static final Color[] MODERN_COLORS = {
            Color.rgb(41, 121, 255), Color.rgb(76, 175, 80),
            Color.rgb(255, 152, 0), Color.rgb(233, 30, 99),
            Color.rgb(156, 39, 176), Color.rgb(0, 188, 212),
            Color.rgb(255, 87, 34), Color.rgb(63, 81, 181)
    };

    public VehicleCanvasController(LocalizationManager localization) {
        this.localization = localization;
        loadVehicleImages();
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        drawAll();
    }

    //загружаем иконки транспортных средств
    private void loadVehicleImages() {
        loadAndCache(VehicleType.BOAT, "/003-boat.png", 48, 48);
        loadAndCache(VehicleType.SHIP, "/002-ship.png", 64, 64);
        loadAndCache(VehicleType.HELICOPTER, "/005-helicopter.png", 48, 48);
        loadAndCache(VehicleType.PLANE, "/004-plane.png", 64, 64);
        loadAndCache(VehicleType.HOVERBOARD, "/001-hoverboard.png", 48, 48);
    }

    //загружаем одну иконку и сохраняем в кэш
    private void loadAndCache(VehicleType type, String resourcePath, double reqW, double reqH) {
        try {
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                Image img = new Image(url.toExternalForm(), reqW, reqH, true, true, false);
                if (!img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
                    vehicleImages.put(type, img);
                }
            }
        } catch (Exception ignored) {
            //игнорируем ошибку загрузки
        }
    }

    //создаём холст
    public Canvas createCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        setupMouseHandlers();

        //обновляем масштаб при изменении размера
        canvas.widthProperty().addListener((obs, oldW, newW) -> {
            if (vehicles.size() > 1) calculateBaseScaling();
            drawAll();
        });
        canvas.heightProperty().addListener((obs, oldH, newH) -> {
            if (vehicles.size() > 1) calculateBaseScaling();
            drawAll();
        });

        return canvas;
    }

    //настраиваем обработчики мыши
    private void setupMouseHandlers() {
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseExited(e -> {
            hoveredVehicle = null;
            drawAll();
        });

        //панорамирование (перетаскивание)
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

        //зум колесом мыши
        canvas.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
            double mouseX = e.getX();
            double mouseY = e.getY();

            //зум к курсору
            panX = mouseX - (mouseX - panX) * zoomFactor;
            panY = mouseY - (mouseY - panY) * zoomFactor;
            zoom = Math.max(0.1, Math.min(zoom * zoomFactor, 10.0));
            drawAll();
        });
    }

    public void setOnVehicleClicked(Consumer<Vehicle> callback) {
        this.onVehicleClicked = callback;
    }

    //обновляем данные
    public void updateData(List<Vehicle> vehicles) {
        if (vehicles == null) return;
        this.vehicles = vehicles;
        if (vehicles.size() > 1) calculateBaseScaling();
        drawAll();
    }

    //вычисляем базовый масштаб для отображения всех объектов
    private void calculateBaseScaling() {
        if (vehicles.isEmpty()) {
            baseScaleX = 1.0;
            baseScaleY = 1.0;
            baseOffsetX = 0;
            baseOffsetY = 0;
            return;
        }

        //находим границы всех объектов
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Vehicle v : vehicles) {
            double x = v.getCoordinates().getX();
            double y = v.getCoordinates().getY();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        //вычисляем масштаб с отступами
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

    //переводим логический X в пиксели
    private double toPixelX(double logicalX) {
        return (logicalX * baseScaleX + baseOffsetX) * zoom + panX;
    }

    //переводим логический Y в пиксели (Y инвертирован)
    private double toPixelY(double logicalY) {
        return (canvas.getHeight() - (logicalY * baseScaleY + baseOffsetY)) * zoom + panY;
    }

    //получаем цвет для владельца
    private Color getOwnerColor(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isEmpty()) {
            return MODERN_COLORS[0];
        }
        return ownerColors.computeIfAbsent(ownerLogin, login ->
                MODERN_COLORS[Math.abs(login.hashCode()) % MODERN_COLORS.length]
        );
    }

    //рисуем всё
    private void drawAll() {
        if (gc == null || canvas == null) return;

        //очищаем фон
        if (isDarkMode) {
            gc.setFill(Color.rgb(30, 41, 59));
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //рисуем сетку и оси
        drawGridAndAxes();

        //рисуем все объекты
        for (Vehicle v : vehicles) {
            drawVehicle(v);
        }
    }

    //рисуем сетку и оси координат
    private void drawGridAndAxes() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        //выбираем цвета для темы
        Color gridColor, axisColor, textColor;
        if (isDarkMode) {
            gridColor = Color.WHITE.deriveColor(0, 0, 1, 0.1);
            axisColor = Color.rgb(148, 163, 184);
            textColor = Color.rgb(168, 85, 247);
        } else {
            gridColor = Color.rgb(235, 235, 235);
            axisColor = Color.rgb(150, 150, 150);
            textColor = Color.DARKGRAY;
        }

        //рисуем сетку
        gc.setStroke(gridColor);
        gc.setLineWidth(1);
        double step = Math.max(25, 50 * zoom);

        for (double x = 0; x <= w; x += step) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y <= h; y += step) {
            gc.strokeLine(0, y, w, y);
        }

        //рисуем оси координат
        double zeroX = toPixelX(0);
        double zeroY = toPixelY(0);
        gc.setStroke(axisColor);
        gc.setLineWidth(2);

        //ось X
        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            gc.setFill(textColor);
            gc.setFont(Font.font(11 * Math.max(0.8, zoom)));
            gc.fillText("X", w - 15, zeroY - 5);
        }

        //ось Y
        if (zeroX >= 0 && zeroX <= w) {
            gc.strokeLine(zeroX, 0, zeroX, h);
            gc.fillText("Y", zeroX + 5, 15);
            gc.fillText("0", zeroX + 5, zeroY + 15);
        }
    }

    //рисуем один объект
    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX());
        double py = toPixelY(v.getCoordinates().getY());

        //вычисляем размер
        double baseSize = 48 * zoom;
        baseSize = Math.max(24, Math.min(baseSize, 96));

        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        boolean isSelected = (selectedVehicle != null && selectedVehicle.getId() == v.getId());

        double size = isHovered ? baseSize * 1.25 : baseSize;
        Color baseColor = getOwnerColor(v.getOwnerLogin());

        //рисуем подсветку
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

        //рисуем иконку или кружок
        drawVehicleImage(v, px, py, size);

        //рисуем ID под объектом
        gc.setFill(isSelected ? Color.RED : (isDarkMode ? Color.rgb(226, 232, 240) : Color.rgb(80, 80, 80)));
        gc.setFont(Font.font("System Bold", 11 * Math.max(0.8, zoom)));
        gc.fillText(String.valueOf(v.getId()), px - 6, py + size/2 + 16);

        //рисуем имя при наведении
        if (isHovered) {
            gc.setFill(isDarkMode ? Color.WHITE : Color.rgb(40, 40, 40));
            gc.setFont(Font.font("System Bold", 13 * Math.max(0.8, zoom)));
            double nameWidth = gc.getFont().getSize() * v.getName().length() * 0.6;
            gc.fillText(v.getName(), px - nameWidth/2, py - size/2 - 8);
        }

        //рисуем рамку для выбранного
        if (isSelected) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2.5);
            double rectSize = size * 1.15;
            gc.strokeRect(px - rectSize/2, py - rectSize/2, rectSize, rectSize);
        }
    }

    //рисуем иконку транспортного средства
    private void drawVehicleImage(Vehicle v, double cx, double cy, double size) {
        Image img = vehicleImages.get(v.getType());

        //рисуем PNG-иконку
        if (img != null && !img.isError() && img.getWidth() > 0) {
            gc.drawImage(img, cx - size/2, cy - size/2, size, size);
        }
        //рисуем кружок с буквой если иконки нет
        else {
            Color c = getOwnerColor(v.getOwnerLogin());
            gc.setFill(c);
            gc.fillOval(cx - size/2, cy - size/2, size, size);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(cx - size/2, cy - size/2, size, size);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System Bold", size/2.5));
            String letter = v.getType().toString().substring(0, 1);
            gc.fillText(letter, cx - gc.getFont().getSize() * 0.3, cy + gc.getFont().getSize() * 0.35);
        }
    }

    //обрабатываем движение мыши
    private void handleMouseMove(MouseEvent event) {
        if (isPanning) return;

        double clickX = event.getX();
        double clickY = event.getY();
        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 35 * zoom;

        //ищем ближайший объект
        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);

            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        //проверяем изменился ли объект под курсором
        boolean changed = (hoveredVehicle == null && closest != null) ||
                (hoveredVehicle != null && closest == null) ||
                (hoveredVehicle != null && closest != null &&
                        hoveredVehicle.getId() != closest.getId());

        if (changed) {
            hoveredVehicle = closest;
            canvas.setCursor(closest != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
            drawAll();
        }
    }

    //обрабатываем клик мыши
    private void handleMouseClick(MouseEvent event) {
        //игнорируем если было перетаскивание
        if (Math.abs(event.getX() - lastMouseX) > 5 ||
                Math.abs(event.getY() - lastMouseY) > 5) {
            return;
        }

        double clickX = event.getX();
        double clickY = event.getY();
        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;
        double hitRadius = 30 * zoom;

        //ищем ближайший объект
        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.hypot(clickX - vx, clickY - vy);

            if (dist < hitRadius && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        //выбираем объект или сбрасываем выделение
        if (closest != null) {
            selectedVehicle = closest;
            drawAll();
            if (onVehicleClicked != null) {
                onVehicleClicked.accept(closest);
            }
        } else {
            selectedVehicle = null;
            drawAll();
            //двойной клик сбрасывает вид
            if (event.getClickCount() == 2) {
                resetView();
            }
        }
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    //фокусируемся на объекте
    public void focusOnVehicle(Vehicle vehicle) {
        if (vehicle == null || canvas == null) return;

        this.selectedVehicle = vehicle;
        double targetZoom = 2.5;
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double logicX = vehicle.getCoordinates().getX();
        double logicY = vehicle.getCoordinates().getY();
        double projectedX = (logicX * baseScaleX + baseOffsetX);
        double projectedY = (h - (logicY * baseScaleY + baseOffsetY));

        //вычисляем смещение для центрирования
        double targetPanX = (w / 2.0) - (projectedX * targetZoom);
        double targetPanY = (h / 2.0) - (projectedY * targetZoom);

        startAnimation(targetZoom, targetPanX, targetPanY);
    }

    //сбрасываем вид
    public void resetView() {
        this.selectedVehicle = null;
        calculateBaseScaling();
        startAnimation(1.0, 0.0, 0.0);
    }

    //запускаем анимацию камеры
    private void startAnimation(double targetZoom, double targetPanX, double targetPanY) {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        animStartZoom = this.zoom;
        animStartPanX = this.panX;
        animStartPanY = this.panY;
        animTargetZoom = targetZoom;
        animTargetPanX = targetPanX;
        animTargetPanY = targetPanY;
        animStartTime = System.nanoTime();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = (now - animStartTime) / 1_000_000;
                double t = Math.min(1.0, (double) elapsed / ANIM_DURATION_MS);

                //плавное замедление
                double ease = 1 - Math.pow(1 - t, 3);

                //интерполяция значений
                zoom = animStartZoom + (animTargetZoom - animStartZoom) * ease;
                panX = animStartPanX + (animTargetPanX - animStartPanX) * ease;
                panY = animStartPanY + (animTargetPanY - animStartPanY) * ease;

                drawAll();

                //завершаем анимацию
                if (t >= 1.0) {
                    zoom = animTargetZoom;
                    panX = animTargetPanX;
                    panY = animTargetPanY;
                    drawAll();
                    this.stop();
                    animationTimer = null;
                }
            }
        };

        animationTimer.start();
    }
}