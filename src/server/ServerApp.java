package server;

import common.Vehicle;
import server.service.*;

import java.util.ArrayList;

public class ServerApp {
    public static void main(String[] args) {
        System.out.println("Инициализация сервера...");

        String xmlFilePath = (args.length > 0) ? args[0] : "dummy";

        // 1. Создание контекста (инициализирует БД-компоненты)
        ServerContext context = new ServerContext(7301, xmlFilePath);
        if (!context.startNetwork()) {
            System.err.println("Не удалось запустить сервер");
            return;
        }

        System.out.println("Сервер запущен. Команд: " +
                context.getCommandsList().getCommandList().size());

        // 2. Загрузка данных из БД в память при старте
        System.out.println("Загрузка коллекции из базы данных...");
        ArrayList<Vehicle> dbVehicles = (ArrayList<Vehicle>) new server.database.VehicleDao().loadAll();
        for (Vehicle v : dbVehicles) {
            // addElementManually — только в память, без повторной вставки в БД
            context.getVehicleManager().addElementManually(v);
        }
        System.out.println("Загружено объектов: " + dbVehicles.size());

        // 3. Подготовка обработчиков
        NetworkRequestHandler requestHandler = new NetworkRequestHandler(
                context.getInvoker(),
                context.getNetworkService()
        );

        ServerLoop serverLoop = new ServerLoop(context, requestHandler);

        // 4. Запуск главного цикла
        System.out.println("Сервер готов к работе. Нажмите Ctrl+C для остановки.");
        serverLoop.run();
    }
}