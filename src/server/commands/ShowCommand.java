package server.commands;

import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class ShowCommand implements Command {
    private final IVehicleManager manager;
    private final CommandType type = CommandType.NOARGS;

    public ShowCommand(IVehicleManager manager) {
        this.manager = manager;
    }

    @Override
    public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 1) return ReturnCode.FAILED;

        // Отправляем объекты в data, а не как текст
        params.responseSender().sendCollection(manager.showCollection());
        return ReturnCode.OK;
    }

    @Override
    public String getDescription() {
        return " вывести все элементы";
    }

    @Override
    public CommandType getType() {
        return this.type;
    }
}