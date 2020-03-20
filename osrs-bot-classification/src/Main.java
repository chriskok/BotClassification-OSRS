import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by User on 03/16/2020.
 */


@ScriptManifest(
        category = Category.UTILITY, name = "Bot Classification", author = "ChronicCoder", version = 0.1
)

public class Main extends AbstractScript {
    String hiscores_url = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws";
    HashSet<String> checked_players = new HashSet<String>();

    Player currentTarget;

    //get the localhost IP address, if server is running on some other IP, you need to use that
    InetAddress host;
    {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    Socket socket = null;
    PrintWriter out =  null;
    BufferedReader in = null;
    Scanner scanner = new Scanner(System.in);


    @Override
    public void onStart() {
        try {
            socket = new Socket(host.getHostName(), 9876);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(
                    new InputStreamReader(System.in));

        } catch (UnknownHostException e) {
            System.err.println("Unknown Host.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection.");
        }
    }

    @Override
    public void onExit() {
        //Code here will execute after the script ends
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg){
        try{
            out.print(msg);
            out.flush();
            String message = (String) in.readLine();
            log("Response: " + message);
            return message;

        } catch (UnknownHostException e) {
            log("Unknown Host.");
        } catch (IOException e) {
            log("Couldn't get I/O for "
                    + "the connection.");
        }

        return "failed";
    }

    @Override
    public int onLoop() {
        Players current_players = getPlayers();
        java.util.List<Player> current_list = current_players.all();

        for (int i = 0; i < current_list.size(); i++) {
            String current_name = current_list.get(i).getName();
            if(checked_players.contains(current_name)){
//                log("skipping: " + current_name);
                continue;
            }
            checked_players.add(current_name);
            log("added: " + current_name);
            String str_resp = executePost(hiscores_url,"player="+current_name);
            if (str_resp == null){
                log("No hiscores data available");
            } else{
                log(str_resp);
                sendMessage(current_name);
            }
            break;
        }
        return 1000;
    }

    public static String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
//                log(line);
                response.append(line);
                response.append("\r\n");
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
