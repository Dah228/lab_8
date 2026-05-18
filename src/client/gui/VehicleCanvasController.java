package client.gui;

import common.Vehicle;
import common.VehicleType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Контроллер Canvas для визуализации объектов Vehicle.
 */
public class VehicleCanvasController {

    private final LocalizationManager localization;
    private Canvas canvas;
    private GraphicsContext gc;

    // Параметры отображения
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    // Кэш цветов по владельцу
    private final Map<String, Color> ownerColors = new ConcurrentHashMap<>();

    // Текущие данные
    private List<Vehicle> vehicles = List.of();

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

        return canvas;
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
        if (vehicles.isEmpty()) return;

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

        // Добавляем отступы
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
        return canvas.getHeight() - (logicalY * scaleY + offsetY); // Y инвертирован!
    }

    /**
     * Получает цвет для владельца (хеш → HSB).
     */
    private Color getOwnerColor(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.isEmpty()) {
            return Color.GRAY;
        }
        return ownerColors.computeIfAbsent(ownerLogin, login -> {
            int hash = login.hashCode();
            double hue = Math.abs(hash % 360);
            return Color.hsb(hue, 0.7, 0.9, 0.8);
        });
    }

    /**
     * Отрисовывает все объекты.
     */
    private void drawAll() {
        if (gc == null) return;

        // Очистка
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Рисуем сетку (опционально)
        drawGrid();

        // Рисуем объекты
        for (Vehicle v : vehicles) {
            drawVehicle(v);
        }
    }

    private void drawGrid() {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (double x = 0; x < canvas.getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (double y = 0; y < canvas.getHeight(); y += 50) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }

    /**
     * Отрисовывает один объект в зависимости от типа.
     */
    private void drawVehicle(Vehicle v) {
        double px = toPixelX(v.getCoordinates().getX());
        double py = toPixelY(v.getCoordinates().getY());
        double size = 20; // Размер фигуры

        Color color = getOwnerColor(v.getOwnerLogin());
        gc.setFill(color);
        gc.setStroke(color.darker());
        gc.setLineWidth(2);

        switch (v.getType()) {
            case BOAT -> drawBoat(px, py, size);
            case HELICOPTER -> drawHelicopter(px, py, size);
            case HOVERBOARD -> drawHoverboard(px, py, size);
            case PLANE -> drawPlane(px, py, size);
            case SHIP -> drawShip(px, py, size);
            default -> drawCircle(px, py, size); // fallback
        }

        // Подпись ID
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText(String.valueOf(v.getId()), px - 5, py + size + 15);
    }

    // === Примитивы ===

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

            if (dist < 20 && dist < minDist) { // радиус попадания 20px
                minDist = dist;
                closest = v;
            }
        }

        if (closest != null) {
            showVehicleInfo(closest);
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
        alert.showAndWait();
    }

    /**
     * Публичный метод для внешнего обновления данных.
     */
    public void setData(List<Vehicle> vehicles) {
        updateData(vehicles);
    }
}