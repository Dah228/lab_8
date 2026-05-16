package common;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class CommandRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String commandname;
    private List<String> args;
    private Vehicle vehicle;
    private boolean islaud;
    private String login;      // ← НОВОЕ
    private String password;   // ← НОВОЕ

    // Обновлённый конструктор
    public CommandRequest(String commandname, List<String> args, Vehicle vehicle, boolean islaud, String login, String password) {
        this.commandname = commandname;
        this.args = args;
        this.vehicle = vehicle;
        this.islaud = islaud;
        this.login = login;
        this.password = password;
    }

    // Геттеры
    public String getCommandName() { return commandname; }
    public List<String> getArguments() { return args; }
    public boolean getBoolean() { return islaud; }
    public Vehicle getVehicle() { return vehicle; }
    public String getLogin() { return login; }           // ← НОВЫЙ
    public String getPassword() { return password; }     // ← НОВЫЙ
}