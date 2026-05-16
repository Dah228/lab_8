package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class RemoveByID implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGS;
    public RemoveByID(IVehicleManager vehicleManager) { this.vehicleManager = vehicleManager; }
    @Override public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 2) return ReturnCode.FAILED;
        try {
            long id = Long.parseLong(params.args().get(1));
            if (vehicleManager.rmByID(id, params.login())) {
                if (params.isLaud()) params.responseSender().send("Успешно удалено");
                return ReturnCode.OK;
            } else {
                if (params.isLaud()) params.responseSender().sendError("Объект не найден или нет прав");
                return ReturnCode.FAILED;
            }
        } catch (NumberFormatException e) {
            if (params.isLaud()) params.responseSender().sendError("Ошибка: введите корректный ID (число)");
            return ReturnCode.FAILED;
        }
    }
    @Override public String getDescription() { return " удалить элемент из коллекции по его id"; }
    @Override public CommandType getType() { return this.type; }
}