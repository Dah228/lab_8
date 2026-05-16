package server.commands;

import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class BuyCommand implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGS;

    public BuyCommand(IVehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @Override
    public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 2) { // ["buy", "id"]
            if (params.isLaud()) params.responseSender().sendError("Использование: buy <id>");
            return ReturnCode.FAILED;
        }

        try {
            long id = Long.parseLong(params.args().get(1));
            if (vehicleManager.buyVehicle(id, params.login())) {
                if (params.isLaud()) params.responseSender().send("Транспортное средство успешно куплено");
                return ReturnCode.OK;
            } else {
                if (params.isLaud()) params.responseSender().sendError("Не удалось купить: нет средств, машина ваша или не найдена");
                return ReturnCode.FAILED;
            }
        } catch (NumberFormatException e) {
            if (params.isLaud()) params.responseSender().sendError("Ошибка: ID должен быть числом");
            return ReturnCode.FAILED;
        }
    }

    @Override public String getDescription() { return "купить транспортное средство по ID (buy <id>)"; }
    @Override public CommandType getType() { return type; }
}