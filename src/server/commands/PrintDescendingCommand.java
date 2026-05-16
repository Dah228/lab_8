package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;
import static server.commands.VehicleFormatter.printVehicleList;

public class PrintDescendingCommand implements Command{
    private final IVehicleManager vehicleRandom;
    private final CommandType type = CommandType.NOARGS;
    public PrintDescendingCommand(IVehicleManager vehicleRandom){ this.vehicleRandom = vehicleRandom; }
    @Override public ReturnCode execute(CommandParams params){
        if (params.args().size() != 1) return ReturnCode.FAILED;
        if (params.isLaud()) printVehicleList(vehicleRandom.sortByIDDescending(), params.responseSender());
        return ReturnCode.OK;
    }
    @Override public String getDescription(){ return "вывести элементы коллекции в порядке убывания"; }
    @Override public CommandType getType() { return this.type; }
}