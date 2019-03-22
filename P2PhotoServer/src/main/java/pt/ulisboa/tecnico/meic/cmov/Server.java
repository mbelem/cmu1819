package pt.ulisboa.tecnico.meic.cmov;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    private List<User> users;
    private List<Album> albums;
    private List<Pair<String, String>> loggedInUsers;

    /** Socket related **/
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private static final int SERVER_PORT = 10000;

    public Server() {
        this.users = new ArrayList<>();
        this.loggedInUsers = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    /**
     * Checks if a given username exists
     * @param username to be checked
     * @return true if username exists false otherwise
     */
    public boolean usernameExists(String username) {
        for (User user: users) {
            if (user.getUsername().equals(username))
                return true;
        }
        return false;
    }

    /**
     * Check if a given user (by username) is currently logged in the system.
     * @param username to check
     * @return the sessionID if the user is logged in and null otherwise
     */
    public String usernameIsLoggedOn(String username) {
        for (Pair<String, String> user: this.loggedInUsers) {
            if (user.getKey().equals(username))
                return user.getValue();
        }
        return null;
    }

    /**
     * Given a sessionID returns the corresponding username
     * @param sessionID to be checked
     * @return the username if was a match and null otherwise
     */
    public String getUserNameBySessionID(String sessionID) {
        for (Pair<String, String> user: this.loggedInUsers) {
            if (user.getValue().equals(sessionID))
                return user.getKey();
        }
        return null;
    }

    /**
     * Given a username returns the correspondent User
     * @param username
     * @return the User instance that matches the username
     */
    public User getUserByUsername(String username) {
        for (User user: users) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    /**
     * Given a pattern return all usernames that matches it
     * @param pattern to look for
     * @return a list of all usernames that matches that pattern
     */
    public List<String> findUserNameByPattern(String pattern) {
        List<String> matches = new ArrayList<>();

        for (User user: users) {
            if (user.getUsername().matches(pattern))
                matches.add(user.getUsername());
        }

        return matches;
    }

    /**
     * Given a list of usernames represent all items as a string of type <user1, user2 , ... , userN>
     * @param matches the list to be represented
     * @return a string with matches representation
     */
    public String representListOfUserNames(List<String> matches) {
        String rep = "<";
        int len = matches.size();

        for (int i = 0; i < len; i++) {
            rep += matches.get(i);

            if (i != (len - 1))
                rep += " , ";
        }

        rep += ">";
        return rep;
    }


    /** ======================================= SOCKET RELATED ======================================= **/

    /**
     * Function to start receiving request from the clients.
     */
    public void initSocket() {
        try {
            this.serverSocket = new ServerSocket(SERVER_PORT);
            this.clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            List<String> args;

            while(true) {
                message = in.readLine();

                args = parseInstruction(message);

                out.println(processInstruction(args));

            }

        } catch(IOException e) {
            System.err.println("IOException!");
        }
    }

    /**
     * Stop any listen on the channel for client's requests.
     */
    public void stopSocket() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch(IOException e) {
            System.err.println("IOException!");
        }
    }

    /**
     * Given an instruction (String) parses it according to the several criteria.
     * @param instruction
     * @return A list of string with the arguments
     */
    private List<String> parseInstruction(String instruction) {
        List<String> args = null;
        if (instruction.startsWith("LOGIN") || instruction.startsWith("SIGNUP") || (instruction.startsWith("LOGOUT")) || (instruction.startsWith("ALB-AUP") || (instruction.startsWith("USR-FND")))) {
            args = Arrays.asList(instruction.split(" "));
        } else if (instruction.startsWith("ALB")) {
            //Split by space ignoring spaces inside quotes
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(instruction);

            args = new ArrayList<>();
            while (m.find())
                args.add(m.group(1).replace("\"", ""));
        }
        return args;
    }

    /**
     * Given a list of arguments and a function execute it
     * @param args list of arguments
     * @return the output of the execution
     */
    private String processInstruction(List<String> args) {

        try {
            String instruction = args.get(0);
            String username;
            String password;
            String sessionId;
            String albumTitle;
            String pattern;
            User user;

            switch (instruction) {
                case "LOGIN":
                    username = args.get(1);
                    password = args.get(2);

                    if (!usernameExists(username)) {
                        System.out.println("** LOGIN: User " + username + " does not exists");
                        return "NOK 1";
                    } else {
                        user = getUserByUsername(username);
                        if (!user.getPassword().equals(password)) {
                            System.out.println("** LOGIN: User " + username + " failed!");
                            return "NOK 2";
                        } else {

                            sessionId = usernameIsLoggedOn(username);

                            if (sessionId == null) {
                                sessionId = Long.toHexString(Double.doubleToLongBits(Math.random()));
                                this.loggedInUsers.add(new Pair<>(username, sessionId));
                            }
                            return "OK " + sessionId;


                        }

                    }

                case "SIGNUP":
                    username = args.get(1);
                    password = args.get(2);


                    //Check if user exists
                    if (usernameExists(username)) {
                        System.out.println("** SIGNUP: User " + username + " already exists");
                        return "NOK 3";
                    } else {
                        //Adds user
                        users.add(new User(username, password));
                        System.out.println("** SIGNUP: Successfully added user " + username);
                        return "OK";
                    }

                case "LOGOUT":
                    username = args.get(1);
                    sessionId = usernameIsLoggedOn(username);

                    if (!usernameExists(username)) {
                        System.out.println("** LOGIN: User " + username + " does not exists");
                        return "NOK 1";
                    } else if (sessionId == null) {
                        System.out.println("** LOGOFF: User " + username + " is not currently logged in!");
                        return "NOK 4";
                    } else {
                        loggedInUsers.remove(new Pair<>(username, sessionId));
                        return "OK";
                    }

                // === ALBUM RELATED OPERATIONS ===
                case "ALB-CR8":
                    sessionId = args.get(1);
                    albumTitle = args.get(2);
                    User owner = getUserByUsername(getUserNameBySessionID(sessionId));

                    if (owner == null) {
                        System.out.println("** ALB-CR8: Invalid sessionID!");
                        return "NOK 4";
                    }
                    else {

                        this.albums.add(new Album(Album.CounterID, albumTitle, owner));

                        System.out.println("** ALB-CR8: User " + owner.getUsername() + " just created one album with title: '" + albumTitle + "'");
                        return "OK " + Album.CounterID++;
                    }

                    // === FIND USERS ===
                case "USR-FND":
                    sessionId = args.get(1);
                    pattern = args.get(2);
                    List<String> matches;

                    if (getUserNameBySessionID(sessionId) == null) {
                        System.out.println("** USR-FND: Invalid sessionID!");
                        return "NOK 4";
                    }
                    else {

                        if (pattern.contains("*")) {
                            matches = findUserNameByPattern("\\b(\\w*" + pattern.replace("*", "") + "\\w*)\\b");
                        } else matches = findUserNameByPattern("\\b(\\w*" + pattern + "\\w*)\\b");

                        return "OK " + representListOfUserNames(matches);
                    }

            }
        } catch(Exception e) {
            return "ERR";
        }

        return null;
    }


}
