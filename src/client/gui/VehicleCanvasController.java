package client.gui;
import common.Vehicle;
import common.VehicleType;
import javafx.animation.ScaleTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Контроллер Canvas для визуализации объектов Vehicle.
 * С неоновыми фиолетовыми цветами и анимацией при наведении.
 */
public class VehicleCanvasController {
    private final LocalizationManager localization;
    private Canvas canvas;
    private GraphicsContext gc;
    private Consumer<Vehicle> onVehicleClicked;

    // Параметры отображения
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    // Текущие данные
    private List<Vehicle> vehicles = List.of();

    // Отслеживание наведения
    private Vehicle hoveredVehicle = null;
    private double hoverScale = 1.0;
    private ScaleTransition hoverAnimation;

    // Неоновые фиолетовые цвета для владельцев
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();
    private static final Color[] NEON_PURPLE_COLORS = {
            Color.rgb(180, 0, 255),    // Ярко-фиолетовый
            Color.rgb(255, 0, 255),    // Маджента
            Color.rgb(204, 0, 204),    // Тёмная маджента
            Color.rgb(153, 0, 255),    // Фиолетовый
            Color.rgb(218, 112, 255),  // Светло-фиолетовый
            Color.rgb(148, 0, 211),    // Тёмно-фиолетовый
            Color.rgb(255, 105, 180),  // Hot Pink
            Color.rgb(255, 20, 147),   // Deep Pink
            Color.rgb(138, 43, 226),   // Blue Violet
            Color.rgb(123, 104, 238)   // Medium Slate Blue
    };

    public VehicleCanvasController(LocalizationManager localization) {
        this.localization = localization;
    }

    /**
     * Создает Canvas и возвращает его.
     */
    public Canvas createCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);

        // Обработка клика
        canvas.setOnMouseClicked(this::handleMouseClick);
        // Обработка наведения
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseExited(e -> {
            hoveredVehicle = null;
            hoverScale = 1.0;
            drawAll();
        });

        return canvas;
    }

    public void setOnVehicleClicked(Consumer<Vehicle> callback) {
        this.onVehicleClicked = callback;
    }

    /**
     * Обновляет данные и перерисовывает Canvas.
     */
    public void updateData(List<Vehicle> vehicles) {
        if (vehicles == null) return;
        this.vehicles = vehicles;
        calculateScaling();
        drawAll();
    }

    /**
     * Рассчитывает масштаб и смещение на основе всех координат.
     */
    private void calculateScaling() {
        if (vehicles.isEmpty()) {
            scaleX = 1.0;
            scaleY = 1.0;
            offsetX = 0;
            offsetY = 0;
            return;
        }

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Vehicle v : vehicles) {
            double x = v.getCoordinates().getX();
            double y = v.getCoordinates().getY();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // Добавляем отступы, чтобы объекты не прилипали к краям
        double padding = 50;
        double rangeX = maxX - minX + 2 * padding;
        double rangeY = maxY - minY + 2 * padding;

        if (rangeX == 0) rangeX = 1;
        if (rangeY == 0) rangeY = 1;

        scaleX = (canvas.getWidth() - 2 * padding) / rangeX;
        scaleY = (canvas.getHeight() - 2 * padding) / rangeY;

        // Центрируем
        offsetX = padding - minX * scaleX;
        offsetY = padding - minY * scaleY;
    }

    /**
     * Преобразует логические координаты в пиксели Canvas.
     */
    private double toPixelX(double logicalX) {
        return logicalX * scaleX + offsetX;
    }

    private double toPixelY(double logicalY) {
        // Инвертируем Y
        return canvas.getHeight() - (logicalY * scaleY + offsetY);
    }

    /**
     * Получает неоновый фиолетовый цвет для владельца.
     */
    private Color getOwnerColor(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isEmpty()) {
            return NEON_PURPLE_COLORS[0];
        }

        return ownerColors.computeIfAbsent(ownerLogin, login -> {
            // Используем хеш для выбора цвета из неоновой палитры
            int index = Math.abs(login.hashCode()) % NEON_PURPLE_COLORS.length;
            return NEON_PURPLE_COLORS[index];
        });
    }

    /**
     * Отрисовывает все объекты.
     */
    private void drawAll() {
        if (gc == null) return;

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Рисуем сетку и оси
        drawGridAndAxes();

        // Рисуем объекты
        for (Vehicle v : vehicles) {
            drawVehicle(v);
        }
    }

    /**
     * Отрисовывает сетку и координатные оси X и Y.
     */
    private void drawGridAndAxes() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Сетка
        gc.setStroke(Color.rgb(230, 230, 250));
        gc.setLineWidth(0.5);

        // Вертикальные линии сетки
        for (double x = 0; x <= w; x += 50) {
            gc.strokeLine(x, 0, x, h);
        }

        // Горизонтальные линии сетки
        for (double y = 0; y <= h; y += 50) {
            gc.strokeLine(0, y, w, y);
        }

        // Оси X и Y
        // Находим пиксельные координаты нуля (0,0) из логических координат
        double zeroX = toPixelX(0);
        double zeroY = toPixelY(0);

        gc.setStroke(Color.rgb(138, 43, 226)); // Фиолетовые оси
        gc.setLineWidth(2.5);

        // Ось X (горизонтальная линия через Y=0)
        if (zeroY >= 0 && zeroY <= h) {
            gc.strokeLine(0, zeroY, w, zeroY);
            // Подпись "X"
            gc.setFill(Color.rgb(138, 43, 226));
            gc.setFont(Font.font(14));
            gc.fillText("X", w - 20, zeroY - 10);
        }

        // Ось Y (вертикальная линия через X=0)
        if (zeroX >= 0 && zeroX <= w) {
            gc.strokeLine(zeroX, 0, zeroX, h);
            // Подпись "Y"
            gc.setFill(Color.rgb(138, 43, 226));
            gc.setFont(Font.font(14));
            gc.fillText("Y", zeroX + 10, 20);
            // Подпись "0" в начале координат
            gc.fillText("0", zeroX + 5, zeroY + 15);
        }
    }

    /**
     * Отрисовывает один объект в зависимости от типа.
     */
    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX());
        double py = toPixelY(v.getCoordinates().getY());
        double baseSize = 20;

        // Проверяем, является ли этот объект наведённым
        boolean isHovered = (hoveredVehicle != null && hoveredVehicle.getId() == v.getId());
        double size = isHovered ? baseSize * hoverScale : baseSize;

        Color baseColor = getOwnerColor(v.getOwnerLogin());

        if (isHovered) {
            // Для наведённого объекта — яркий неон с эффектом свечения
            gc.setFill(baseColor);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);

            // Рисуем эффект свечения
            gc.setGlobalAlpha(0.3);
            gc.setFill(baseColor);
            drawShape(px, py, size * 1.3, v.getType());
            gc.setGlobalAlpha(1.0);
        } else {
            // Обычный объект
            gc.setFill(baseColor);
            gc.setStroke(baseColor.darker());
            gc.setLineWidth(2);
        }

        // Рисуем основную форму
        drawShape(px, py, size, v.getType());

        // Подпись ID
        gc.setFill(isHovered ? Color.rgb(138, 43, 226) : Color.BLACK);
        gc.setFont(Font.font(isHovered ? 12 : 10));
        gc.fillText(String.valueOf(v.getId()), px - 5, py + size + 15);

        // При наведении показываем дополнительную информацию
        if (isHovered) {
            gc.setFill(Color.rgb(180, 0, 255));
            gc.setFont(Font.font(11));
            gc.fillText(v.getName(), px - 20, py - size - 5);
        }
    }

    /**
     * Рисует форму в зависимости от типа транспортного средства.
     */
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

    // Примитивы
    private void drawCircle(double cx, double cy, double r) {
        gc.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        gc.strokeOval(cx - r, cy - r, 2 * r, 2 * r);
    }

    private void drawBoat(double cx, double cy, double size) {
        double[] xPoints = {cx - size, cx + size, cx};
        double[] yPoints = {cy + size/2, cy + size/2, cy - size};
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }

    private void drawHelicopter(double cx, double cy, double size) {
        gc.fillRect(cx - size/2, cy - size/2, size, size);
        gc.strokeRect(cx - size/2, cy - size/2, size, size);
        // Винт сверху
        gc.strokeLine(cx - size, cy - size/2 - 5, cx + size, cy - size/2 - 5);
    }

    private void drawHoverboard(double cx, double cy, double size) {
        gc.fillRoundRect(cx - size/2, cy - size/4, size, size/2, 10, 10);
        gc.strokeRoundRect(cx - size/2, cy - size/4, size, size/2, 10, 10);
    }

    private void drawPlane(double cx, double cy, double size) {
        double[] xPoints = {cx - size, cx + size, cx, cx - size/2, cx + size/2};
        double[] yPoints = {cy, cy, cy - size, cy + size/2, cy + size/2};
        gc.fillPolygon(xPoints, yPoints, 5);
        gc.strokePolygon(xPoints, yPoints, 5);
    }

    private void drawShip(double cx, double cy, double size) {
        double[] xPoints = {cx - size, cx + size, cx + size/2, cx - size/2};
        double[] yPoints = {cy + size/2, cy + size/2, cy - size, cy - size};
        gc.fillPolygon(xPoints, yPoints, 4);
        gc.strokePolygon(xPoints, yPoints, 4);
    }

    /**
     * Обработка движения мыши — поиск ближайшего объекта для наведения.
     */
    private void handleMouseMove(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.sqrt(Math.pow(clickX - vx, 2) + Math.pow(clickY - vy, 2));

            if (dist < 25 && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        // Если наведение изменилось
        if ((hoveredVehicle == null && closest != null) ||
                (hoveredVehicle != null && closest == null) ||
                (hoveredVehicle != null && closest != null && hoveredVehicle.getId() != closest.getId())) {

            hoveredVehicle = closest;

            if (hoveredVehicle != null) {
                // Анимация увеличения
                hoverScale = 1.0;
                if (hoverAnimation != null) {
                    hoverAnimation.stop();
                }
                hoverAnimation = new ScaleTransition(Duration.millis(200));
                hoverAnimation.setOnFinished(e -> {
                    hoverScale = 1.3;
                    drawAll();
                });
                hoverAnimation.play();
            } else {
                hoverScale = 1.0;
            }

            drawAll();
        }
    }

    /**
     * Обработка клика по Canvas — поиск ближайшего объекта.
     */
    private void handleMouseClick(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        Vehicle closest = null;
        double minDist = Double.MAX_VALUE;

        for (Vehicle v : vehicles) {
            double vx = toPixelX(v.getCoordinates().getX());
            double vy = toPixelY(v.getCoordinates().getY());
            double dist = Math.sqrt(Math.pow(clickX - vx, 2) + Math.pow(clickY - vy, 2));

            if (dist < 20 && dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }

        if (closest != null) {
            // Если есть callback — вызываем его (редактирование), иначе старый Alert
            if (onVehicleClicked != null) {
                onVehicleClicked.accept(closest);
            } else {
                showVehicleInfo(closest);
            }
        }
    }

    /**
     * Показывает информацию об объекте в Alert.
     */
    private void showVehicleInfo(Vehicle v) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(localization.get("vehicle.info"));
        alert.setHeaderText("ID: " + v.getId());
        alert.setContentText(
                "Name: " + v.getName() + "\n" +
                        "Owner: " + v.getOwnerLogin() + "\n" +
                        "Coords: (" + v.getCoordinates().getX() + ", " + v.getCoordinates().getY() + ")\n" +
                        "Type: " + v.getType() + "\n" +
                        "Fuel: " + v.getFuelType() + "\n" +
                        "Price: " + v.getPrice()
        );

        // Стилизация alert в фиолетовых тонах
        alert.getDialogPane().setStyle("-fx-background-color: #f0e6ff;");
        alert.showAndWait();
    }

    /**
     * Публичный метод для внешнего обновления данных.
     */
    public void setData(List<Vehicle> vehicles) {
        updateData(vehicles);
    }
}