package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;
import common.Vehicle;
import static server.commands.VehicleFormatter.printVehicleList;
import java.util.ArrayList;

public class CompareByEnginePowerCommand implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGS;
    public CompareByEnginePowerCommand(IVehicleManager vehicleCollection) { this.vehicleManager = vehicleCollection; }
    @Override public ReturnCode execute(CommandParams params) throws IllegalArgumentException {
        if (params.args().size() != 2) return ReturnCode.FAILED;
        Float number = Float.parseFloat(String.valueOf(params.args().get(1)));
        ArrayList<Vehicle> veh = vehicleManager.filterByEnginePower(number);
        if (params.isLaud()) printVehicleList(veh, params.responseSender());
        return ReturnCode.OK;
    }
    @Override public String getDescription() { return "вывести элементы, значение поля enginePower которых больше заданного"; }
    @Override public CommandType getType() { return this.type; }
}