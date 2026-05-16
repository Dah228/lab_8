package server.commands;

import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

/**
 * Команда пополнения баланса текущего пользователя.
 * БЕЗОПАСНОСТЬ: не принимает логин как аргумент,
 * использует только аутентифицированный login из параметров.
 */
public class DepositCommand implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.WITHARGS;

    public DepositCommand(IVehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @Override
    public ReturnCode execute(CommandParams params) {
        // args: ["deposit", "amount"]
        if (params.args().size() != 2) {
            if (params.isLaud()) {
                params.responseSender().sendError("Использование: deposit <сумма>");
            }
            return ReturnCode.FAILED;
        }

        try {
            double amount = Double.parseDouble(params.args().get(1));

            if (amount <= 0) {
                if (params.isLaud()) {
                    params.responseSender().sendError("Сумма пополнения должна быть положительной");
                }
                return ReturnCode.FAILED;
            }

            if (vehicleManager.deposit(params.login(), amount)) {
                if (params.isLaud()) {
                    params.responseSender().send("Баланс успешно пополнен на " + amount);
                }
                return ReturnCode.OK;
            } else {
                if (params.isLaud()) {
                    params.responseSender().sendError("Не удалось пополнить баланс");
                }
                return ReturnCode.FAILED;
            }
        } catch (NumberFormatException e) {
            if (params.isLaud()) {
                params.responseSender().sendError("Ошибка: сумма должна быть числом");
            }
            return ReturnCode.FAILED;
        }
    }

    @Override
    public String getDescription() {
        return "пополнить баланс текущего пользователя (deposit <сумма>)";
    }

    @Override
    public CommandType getType() {
        return type;
    }
}