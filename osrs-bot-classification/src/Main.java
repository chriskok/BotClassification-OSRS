import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by User on 03/16/2020.
 */


@ScriptManifest(
        category = Category.UTILITY, name = "Bot Classification", author = "ChronicCoder", version = 0.1
)

public class Main extends AbstractScript {
    String hiscores_url = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws";
    HashSet<String> checked_players = new HashSet<String>();
    Worlds worlds_obj = new Worlds();
    List<World> world_list = worlds_obj.f2p();
    int datacount = 0;
    int maxdatacollected = 100;

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

    //Snippet Source: https://dreambot.org/forums/index.php?/topic/10595-get-other-players-equipment/
    public static int[] getEquipped(Player p){
        int[] appearanceData = p.getComposite().getApperance();
        int[] equipment = new int[appearanceData.length];
        for(int i = 0; i < equipment.length; i++){
            equipment[i] = appearanceData[i] > 512 ? appearanceData[i]-512 : -1;
        }
        return equipment;
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

        int skip_count = 0;
        for (int i = 0; i < current_list.size(); i++) {
            Player current_player = current_list.get(i);
            String current_name = current_player.getName();

            // check if player's data has already been collected
            if(checked_players.contains(current_name)){
//                log("skipping: " + current_name);
                skip_count++;
                continue;
            }
            checked_players.add(current_name);
            log("added: " + current_name);

            // put together string of username, level, gear, location and animation
            String data_string = current_name + "\r\n" +
                    Arrays.toString(getEquipped(current_player)) + "\r\n" +
                    current_player.getTile().toString() + "\r\n" +
                    String.valueOf(current_player.getAnimation()) + "\r\n";


            // finally put together hiscore data and send message
            String str_resp = executePost(hiscores_url,"player="+current_name);
            if (str_resp == null){
                log("No hiscores data available");
            } else{
                log("sending data for: " + current_name);
                data_string = data_string  + str_resp;
                sendMessage(data_string);
            }

            datacount++;
            break;
        }

        // if there are no more players to collect data from here, we change worlds
        if (current_list.size() == 0 || current_list.size() == skip_count){
            World new_world = world_list.remove(0);
            while (new_world.getMinimumLevel() > 0 || new_world.isHighRisk() || new_world.isPVP()){
                new_world = world_list.remove(0);
            }
        }

        // stop script if we've got the data we need
        if(datacount > maxdatacollected){
            stop();
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
