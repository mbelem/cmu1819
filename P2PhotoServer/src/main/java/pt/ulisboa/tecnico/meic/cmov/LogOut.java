package pt.ulisboa.tecnico.meic.cmov;

import java.util.List;

public class LogOut extends Instruction {

    LogOut(List<String> args, Server server) {
        super("LOGOUT", args, server);
    }

    @Override
    public String execute() {

        try {

            String sessionId = args.get(1);
            String username = server.getUserNameBySessionID(sessionId);

            if (sessionId == null)
                return "ERR";

            if (username == null) {
                displayDebug(NOK4);
                return "NOK 4";
            } else {
                server.removeLoggedUser(username, sessionId);
                return "OK";
            }
        } catch(NullPointerException | IndexOutOfBoundsException e) {
            return "ERR";
        }
    }
}
