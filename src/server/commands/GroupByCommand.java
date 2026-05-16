package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;
import java.util.Map;

public class GroupByCommand implements Command{
    private final IVehicleManager vehicleAdder;
    private final CommandType type = CommandType.DETECTPARAM;
    public GroupByCommand(IVehicleManager vehicleAdder){ this.vehicleAdder = vehicleAdder; }
    @Override public ReturnCode execute(CommandParams params) {
        Map<Comparable<?>, Long> grouped = vehicleAdder.groupByParam(params.args());
        String fieldName = params.args().get(0);
        VehicleFormatter.printGroupedResult(fieldName, grouped, params.responseSender());
        return ReturnCode.OK;
    }
    @Override public String getDescription() { return "сгруппировать элементы по заданному типу"; }
    @Override public CommandType getType() { return this.type; }
}