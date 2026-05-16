package server.commands;

import common.ReturnCode;
import common.Vehicle;
import server.database.AuthService;
import server.service.NetworkResponseSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Invoker {
    private final Map<String, Command> commands = new HashMap<>();
    private final AuthService authService;  // ← НОВОЕ

    public Invoker(AuthService authService) {
        this.authService = authService;
    }

    public void registerCommand(String commandName, Command command) {
        commands.put(commandName, command);
    }

    // Обновлённый метод: добавлены login/password
    public ReturnCode executeCommand(
            String commandName,
            List<String> args,
            Vehicle vehicle,
            Boolean isLaud,
            NetworkResponseSender responseSender,
            String login,
            String password
    ) {
        // 1. Регистрация — единственная команда без авторизации
        if ("register".equals(commandName)) {
            if (args.size() < 3) {
                if (isLaud) responseSender.sendError("register: укажите логин и пароль");
                return ReturnCode.FAILED;
            }
            String regLogin = args.get(1);
            String regPassword = args.get(2);
            if (authService.register(regLogin, regPassword)) {
                if (isLaud) responseSender.send("Пользователь зарегистрирован");
                return ReturnCode.OK;
            } else {
                if (isLaud) responseSender.sendError("Ошибка регистрации (возможно, логин занят)");
                return ReturnCode.FAILED;
            }
        }

        // 2. Все остальные команды требуют авторизации
        if (!authService.authenticate(login, password)) {
            if (isLaud) responseSender.sendError("Неверный логин или пароль");
            return ReturnCode.FAILED;
        }

        Command command = commands.get(commandName);
        if (command == null) {
            if (isLaud) responseSender.sendError("Неизвестная команда: " + commandName);
            return ReturnCode.FAILED;
        }

        try {
            CommandParams params = new CommandParams(args, vehicle, isLaud, responseSender, login);
            return command.execute(params);
        } catch (IllegalArgumentException e) {
            if (isLaud) responseSender.sendError("Неверные аргументы: " + e.getMessage());
            return ReturnCode.FAILED;
        } catch (Exception e) {
            System.out.println("Ошибка выполнения " + commandName + ": " + e.getMessage());
            if (isLaud) responseSender.sendError("Внутренняя ошибка сервера");
            return ReturnCode.FAILED;
        }
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}