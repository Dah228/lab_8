package server.commands;
import common.CommandType;
import common.ReturnCode;
import common.Vehicle;
import common.VehicleType;
import server.collection.IVehicleManager;
import static server.commands.VehicleFormatter.printVehicleList;
import java.util.ArrayList;

public class FilterLessThatType implements Command{
    private final CommandType type = CommandType.WITHARGS;
    private final IVehicleManager vehicleManager;
    public FilterLessThatType(IVehicleManager vehicleCollection){ this.vehicleManager = vehicleCollection; }
    @Override public ReturnCode execute(CommandParams params) throws IllegalArgumentException{
        if (params.args().size() != 2) return ReturnCode.FAILED;
        VehicleType type = VehicleType.valueOf(params.args().get(1).toUpperCase());
        ArrayList<Vehicle> veh = vehicleManager.filterLessThanType(type);
        if (params.isLaud()) printVehicleList(veh, params.responseSender());
        return ReturnCode.OK;
    }
    @Override public String getDescription() { return "вывести элементы, значение поля enginePower которых больше заданного"; }
    @Override public CommandType getType() { return this.type; }
}