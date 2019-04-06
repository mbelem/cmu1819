package pt.ulisboa.tecnico.meic.cmov;

import java.util.List;

public class ListAlbum extends Instruction {

    ListAlbum(List<String> args, Server server) {
        super("ALB-LST", args, server);
    }

    @Override
    public String execute() {
        try {

            if (args.size() != 2)
                return "ERR";

            String sessionId = args.get(1);
            String username = server.getUserNameBySessionID(sessionId);

            if (username == null) {
                displayDebug(VERBOSE_NOK4);
                return NOK_4;
            } else {
                List<Integer> albums = server.getAlbunsOfGivenUser(username);

                displayDebug("User %s requested a list album", username);
                return OK_PLUS + server.representIntegerList(albums);

            }
        } catch(NullPointerException | IndexOutOfBoundsException e) {
            return ERR;
        }
    }
}
