package server.commands;
import common.CommandType;
import common.ReturnCode;
import server.collection.IVehicleManager;

public class ClearCommand implements Command {
    private final IVehicleManager manager;
    private final CommandType type = CommandType.NOARGS;
    public ClearCommand(IVehicleManager manager) { this.manager = manager; }
    @Override public ReturnCode execute(CommandParams params) {
        if (params.args().size() != 1) return ReturnCode.FAILED;
        manager.clearCollection(params.login());
        if (params.isLaud()) params.responseSender().send("Коллекция очищена (ваши объекты)");
        return ReturnCode.OK;
    }
    @Override public String getDescription() { return "очистить коллекцию"; }
    @Override public CommandType getType() { return this.type; }
}