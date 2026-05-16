package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class UpdateElementID implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGSMODEL;
    public UpdateElementID(IVehicleManager vehicleManager) { this.vehicleManager = vehicleManager; }
    @Override public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 2) return ReturnCode.FAILED;
        try {
            long id = Long.parseLong(params.args().get(1));
            params.vehicle().setId(id);
            params.vehicle().setOwnerLogin(params.login());
            if (vehicleManager.updateElementByID(id, params.vehicle(), params.login())) {
                if (params.isLaud()) params.responseSender().send("Элемент успешно обновлен");
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
    @Override public String getDescription() { return " обновить значение элемента коллекции, id которого равен заданному"; }
    @Override public CommandType getType() { return this.type; }
}