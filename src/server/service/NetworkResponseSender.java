package server.service;

import common.ResponseSender;
import common.Vehicle;
import java.util.List;
import java.util.ArrayList;

public class NetworkResponseSender implements ResponseSender {
    private final StringBuilder output = new StringBuilder();
    private List<Vehicle> collectionData = new ArrayList<>();

    @Override
    public void send(String message) {
        output.append(message).append("\n");
    }

    @Override
    public void sendError(String error) {
        output.append("ERROR: ").append(error).append("\n");
    }

    @Override
    public void sendCollection(List<Vehicle> vehicles) {
        this.collectionData = vehicles != null ? new ArrayList<>(vehicles) : new ArrayList<>();
    }

    @Override
    public String getOutput() {
        String result = output.toString();
        clear();
        return result;
    }

    public List<Vehicle> getCollectionData() {
        List<Vehicle> data = new ArrayList<>(collectionData);
        collectionData.clear();
        return data;
    }

    @Override
    public void clear() {
        output.setLength(0);
        collectionData.clear();
    }
}