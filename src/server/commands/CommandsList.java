package server.commands;

import server.collection.IVehicleManager;
import java.util.HashMap;
import java.util.Map;

public class CommandsList {
    private final Map<String, Command> commandList = new HashMap<>();
    private final Invoker invoker;

    public CommandsList(IVehicleManager manager, Invoker invoker) {
        this.invoker = invoker;
        registerCommands(manager);
    }

    private void registerCommands(IVehicleManager manager) {
        register("clear", new ClearCommand(manager));
        register("info", new InfoCommand(manager));
        register("show", new ShowCommand(manager));
        register("print_descending", new PrintDescendingCommand(manager));
        register("shuffle", new ShuffleCommand(manager));
        register("sort", new SortCommand(manager));
        register("help", new HelpCommand(invoker.getCommands()));
        register("group_by", new GroupByCommand(manager));
        register("filter_greater_than_engine_power", new CompareByEnginePowerCommand(manager));
        register("remove_by_id", new RemoveByID(manager));
        register("filter_less_than_type", new FilterLessThatType(manager));
        register("add", new AddCommand(manager));
        register("add_if_max", new AddIfMax(manager));
        register("update", new UpdateElementID(manager));
        register("buy", new BuyCommand(manager));
        register("show_balance", new ShowBalanceCommand(manager));
        register("deposit", new DepositCommand(manager));
        register("set_price", new SetPriceCommand(manager));
    }

    private void register(String name, Command command) {
        commandList.put(name, command);
        invoker.registerCommand(name, command);
    }

    public Map<String, Command> getCommandList() { return commandList; }
    public Invoker getInvoker() { return invoker; }
}