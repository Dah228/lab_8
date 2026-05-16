package server.commands;

import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class SetPriceCommand implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGS;

    public SetPriceCommand(IVehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @Override
    public ReturnCode execute(CommandParams params) {
        // args: ["set_price", "id", "price"]
        if (params.args().size() != 3) {
            if (params.isLaud()) params.responseSender().sendError("Использование: set_price <id> <price>");
            return ReturnCode.FAILED;
        }

        try {
            long id = Long.parseLong(params.args().get(1));
            double price = Double.parseDouble(params.args().get(2));

            if (price < 0) {
                if (params.isLaud()) params.responseSender().sendError("Цена не может быть отрицательной");
                return ReturnCode.FAILED;
            }

            if (vehicleManager.setPrice(id, price, params.login())) {
                if (params.isLaud()) params.responseSender().send("Цена успешно установлена");
                return ReturnCode.OK;
            } else {
                if (params.isLaud()) params.responseSender().sendError("Не удалось изменить цену: объект не найден или нет прав");
                return ReturnCode.FAILED;
            }
        } catch (NumberFormatException e) {
            if (params.isLaud()) params.responseSender().sendError("Ошибка: ID и цена должны быть числами");
            return ReturnCode.FAILED;
        }
    }

    @Override
    public String getDescription() {
        return "установить цену на своё транспортное средство (set_price <id> <price>)";
    }

    @Override
    public CommandType getType() {
        return type;
    }
}