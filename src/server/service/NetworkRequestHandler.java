package server.service;

import common.CommandRequest;
import common.CommandResponse;
import common.ReturnCode;
import common.Vehicle;
import server.commands.Invoker;
import java.nio.channels.SocketChannel;
import java.util.List;

public class NetworkRequestHandler {
    private final Invoker invoker;
    private final ServerNetworkService networkService;

    public NetworkRequestHandler(Invoker invoker, ServerNetworkService networkService) {
        this.invoker = invoker;
        this.networkService = networkService;
    }

    public void processRequest(SocketChannel clientChannel, CommandRequest request) {
        try {
            String commandName = request.getCommandName();
            var arguments = request.getArguments();
            var vehicle = request.getVehicle();
            var isLaud = request.getBoolean();
            String login = request.getLogin();
            String password = request.getPassword();

            System.out.printf("Запрос от %s: %s (user: %s)%n",
                    clientChannel.getRemoteAddress(), commandName, login);

            NetworkResponseSender networkSender = new NetworkResponseSender();

            ReturnCode statusCode = invoker.executeCommand(
                    commandName,
                    arguments,
                    vehicle,
                    isLaud,
                    networkSender,
                    login,
                    password
            );

            String commandOutput = networkSender.getOutput();
            List<Vehicle> collectionData = networkSender.getCollectionData();

            // Если есть данные коллекции, отправляем их в data
            CommandResponse response = new CommandResponse(
                    statusCode == ReturnCode.OK,
                    commandOutput.isEmpty() ? "Команда выполнена" : commandOutput,
                    collectionData.isEmpty() ? null : collectionData
            );

            networkService.queueResponse(clientChannel, response);
        } catch (Exception e) {
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
            CommandResponse error = new CommandResponse(
                    false,
                    "Ошибка сервера: " + e.getMessage(),
                    null
            );
            networkService.queueResponse(clientChannel, error);
        }
    }
}