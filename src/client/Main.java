package client;

import common.CommandResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    private static final String HANDSHAKE_EXPECTED = "connected";

    public static void main(String[] args) {
        // 1. Конфигурация
        ClientConfig config = ClientConfig.defaultConfig();

        // 2. Подключение + рукопожатие
        NetworkService network = new NetworkService(config.host(), config.port());
        ConnectionInitializer initializer = new ConnectionInitializer(network, HANDSHAKE_EXPECTED);
        CommandResponse initResponse = initializer.initialize();
        if (initResponse == null) return;

        // 3. Загрузка команд
        CommandRegistryLoader registryLoader = new CommandRegistryLoader(network);
        AllCommands allCommands = registryLoader.loadCommands(initResponse);
        if (allCommands == null) return;

        // 4. Авторизация/регистрация
        String login = null, password = null;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                System.out.print("Введите логин (или 'register' для регистрации): ");
                String input = console.readLine();
                if (input == null) return;
                input = input.trim();

                if ("register".equalsIgnoreCase(input)) {
                    System.out.print("Придумайте логин: ");
                    String newLogin = console.readLine();
                    System.out.print("Придумайте пароль: ");
                    String newPassword = console.readLine();

                    // Отправляем команду register
                    var regRequest = new common.CommandRequest(
                            "register",
                            java.util.List.of("register", newLogin, newPassword),
                            null,
                            true,
                            "", "" // для register auth не проверяется
                    );
                    network.send(regRequest);
                    var regResponse = network.receive();
                    if (regResponse != null && regResponse.isSuccess()) {
                        System.out.println(regResponse.getMessage());
                        login = newLogin;
                        password = newPassword;
                        break;
                    } else {
                        System.out.println("Ошибка: " + (regResponse != null ? regResponse.getMessage() : "нет ответа"));
                    }
                } else {
                    login = input;
                    System.out.print("Пароль: ");
                    password = console.readLine();
                    // Проверяем авторизацию через простую команду (например, info)
                    var testRequest = new common.CommandRequest(
                            "info",
                            java.util.List.of("info"),
                            null,
                            true,
                            login, password
                    );
                    network.send(testRequest);
                    var testResponse = network.receive();
                    if (testResponse != null && testResponse.isSuccess()) {
                        System.out.println("Авторизация успешна");
                        break;
                    } else {
                        System.out.println("Неверный логин или пароль");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка ввода: " + e.getMessage());
            return;
        }

        // 5. Бизнес-логика
        DataValidator validator = new DataValidator(System.in, true);
        Executor executor = new Executor(network, allCommands, validator);
        executor.setAuthCredentials(login, password);

        // 6. Запуск
        System.out.println("Клиент готов. Введите команду (или 'help'):");
        executor.run(System.in);

        // 7. Завершение
        network.disconnect();
        System.out.println("Клиент завершил работу");
    }
}