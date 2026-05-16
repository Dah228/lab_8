package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class AddIfMax implements Command {
    private final IVehicleManager vehicleAdder;
    private final CommandType type = CommandType.WITHMODEL;
    public AddIfMax(IVehicleManager vehicleComparator) { this.vehicleAdder = vehicleComparator; }
    @Override public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 1) return ReturnCode.FAILED;
        params.vehicle().setOwnerLogin(params.login());
        if (vehicleAdder.addIfMax(params.vehicle())) {
            if (params.isLaud()) params.responseSender().send("Элемент добавлен (максимальная дистанция)");
            return ReturnCode.OK;
        } else {
            if (params.isLaud()) params.responseSender().send("Элемент не добавлен: дистанция не максимальная");
            return ReturnCode.FAILED;
        }
    }
    @Override public String getDescription() { return "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"; }
    @Override public CommandType getType() { return this.type; }
}