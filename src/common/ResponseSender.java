package common;

import java.util.List;

public interface ResponseSender {
    void send(String message);
    void sendError(String error);
    void sendCollection(List<Vehicle> vehicles);
    String getOutput();
    void clear();
}