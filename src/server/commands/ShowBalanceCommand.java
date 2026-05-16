package server.commands;

import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

/**
 * Команда показа баланса текущего пользователя.
 * БЕЗОПАСНОСТЬ: не принимает логин как аргумент,
 * использует только аутентифицированный login из параметров.
 */
public class ShowBalanceCommand implements Command {
    private final IVehicleManager vehicleManager;
    private final CommandType type = CommandType.NOARGS;

    public ShowBalanceCommand(IVehicleManager vehicleManager) {
        this.vehicleManager = vehicleManager;
    }

    @Override
    public ReturnCode execute(CommandParams params) {
        // Проверяем, что команда вызвана без лишних аргументов
        if (params.args().size() != 1) {
            if (params.isLaud()) {
                params.responseSender().sendError("Использование: show_balance");
            }
            return ReturnCode.FAILED;
        }

        double balance = vehicleManager.getBalance(params.login());

        if (params.isLaud()) {
            params.responseSender().send("Ваш баланс: " + balance);
        }
        return ReturnCode.OK;
    }

    @Override
    public String getDescription() {
        return "показать баланс текущего пользователя";
    }

    @Override
    public CommandType getType() {
        return type;
    }
}