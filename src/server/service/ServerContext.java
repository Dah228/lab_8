package server.service;

import server.collection.IVehicleManager;
import server.collection.VehicleCollection;
import server.collection.VehicleManager;
import server.collection.VehicleManagerProxy;
import server.commands.CommandsList;
import server.commands.Invoker;
import server.database.AuthService;
import server.database.UserDao;
import server.database.VehicleDao;

public class ServerContext {
    private final CommandsList commandsList;
    private final Invoker invoker;
    private final ServerNetworkService networkService;
    private final IVehicleManager vehicleManager;
    private final String xmlFilePath;
    private final int port;


    public ServerContext(int port, String xmlFilePath) {
        this.port = port;
        this.xmlFilePath = xmlFilePath;
        AuthService authService = new AuthService();
        VehicleDao vehicleDao = new VehicleDao();
        VehicleCollection collection = new VehicleCollection();
        UserDao userDao = new UserDao();

        this.invoker = new Invoker(authService);

        IVehicleManager realManager = new VehicleManager(collection, vehicleDao, userDao);
        this.vehicleManager = new VehicleManagerProxy(realManager);

        this.commandsList = new CommandsList(vehicleManager, invoker);
        this.networkService = new ServerNetworkService(port, commandsList);
    }

    public boolean startNetwork() { return networkService.start(); }

    public CommandsList getCommandsList() { return commandsList; }
    public Invoker getInvoker() { return invoker; }
    public ServerNetworkService getNetworkService() { return networkService; }
    public IVehicleManager getVehicleManager() { return vehicleManager; }
    public String getXmlFilePath() { return xmlFilePath; }
    public int getPort() { return port; }

    public void stop() {
        if (networkService != null) networkService.stop();
    }
}