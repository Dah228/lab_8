package server.commands;

import common.ResponseSender;
import common.Vehicle;
import java.util.List;

public record CommandParams(
        List<String> args,
        Vehicle vehicle,
        Boolean isLaud,
        ResponseSender responseSender,
        String login  //  логин авторизованного пользователя
) {}