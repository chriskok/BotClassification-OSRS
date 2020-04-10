import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.PathDirection;
import org.dreambot.api.methods.walking.path.impl.LocalPath;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

@ScriptManifest(
        category = Category.UTILITY, name = "Mining Scraper", author = "ChronicCoder", version = 0.1
)

public class Main extends AbstractScript {
    String hiscores_url = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws";
    HashSet<String> checked_players = new HashSet<String>();
    List<World> world_list = new Worlds().all(wo -> wo != null && wo.isNormal() && wo.isF2P() && wo.getWorld() < 400);

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

    public void addCheckedPlayers(String players){
        String[] lines = players.split(",");
        for (String line : lines) {
            checked_players.add(line);
        }
        log("Added " + checked_players.size() + " previous players");
    }

    @Override
    public void onStart() {
        try {
            socket = new Socket(host.getHostName(), 9876);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(
                    new InputStreamReader(System.in));

            String message = (String) in.readLine();
            addCheckedPlayers(message);
        } catch (UnknownHostException e) {
            System.err.println("Unknown Host.");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection.");
        }

//        changeArea(areaID);
//        areaID += 1;
//        if (areaID >= area.length) {
//            areaID = 0;
//        }
    }

    @Override
    public void onExit() {
        //Code here will execute after the script ends
        try {
            out.close();
            in.close();
            socket.close();
            log("TOTAL DATA COUNT: " + datacount);
            log("TOTAL CHECKED PLAYERS: " + checked_players.size());
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

    //Player Reporting Functionality - Added by SoWeGoOn
    public String reportPlayer(String reportedPlayerName)
    {
        log("Looking for " + reportedPlayerName);

        List<Player> reportedPlayer = getPlayers().all();
        reportedPlayer.forEach(p -> {
            if(p.getName().equals(reportedPlayerName))
            {
                log("Found " + reportedPlayerName + ", now following too not loose sight");
                while(getLocalPlayer().isInteracting(p) != true)
                {
                    p.interact("Follow");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                log("Following " + reportedPlayerName + ". Now reporting...");
                p.interact("Report");
                //Dont add the player to our ignore list
                try {
                    //Sleep between 1 to 3 seconds
                    Thread.sleep((1 + new Random().nextInt(3)) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getMouse().click(new Point(Calculations.random(271, 285), Calculations.random(124, 139)));
                //Report the player
                try {
                    //Sleep between 1 to 3 seconds
                    Thread.sleep((1 + new Random().nextInt(3)) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getMouse().click(new Point(Calculations.random(37, 164), Calculations.random(288, 302)));
                log("Reported " + reportedPlayerName + " successfully!");
            }
        });

        return "done";
    }

    public String sendMessage(String msg){
        try{
            out.print(msg);
            out.flush();
            String message = (String) in.readLine();
            return message;

        } catch (UnknownHostException e) {
            log("Unknown Host.");
        } catch (IOException e) {
            log("Couldn't get I/O for "
                    + "the connection.");
        }

        return "failed";
    }

    private Area[] area = {
            new Area(3284, 3366, 3288, 3363),
            new Area(3178, 3372, 3181, 3369),
            new Area(3226, 3147, 3229, 3145)
    };

    public void changeArea(int areaID){

        while(!area[areaID].contains(getLocalPlayer())) {
            getWalking().walk(area[areaID].getRandomTile());

            sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(4000, 8000));
        }
    }

    private int movement_int = 1;
    private int datacount = 0;
    private int maxdatacollected = 5000;
    private long startTime = System.currentTimeMillis();
    private int areaID = 0;

    @Override
    public int onLoop() {

        // if we somehow get lost or die
        if(!area[areaID].contains(getLocalPlayer())){
            log("Heading back to area #" + areaID);
            changeArea(areaID);
        }

        Players current_players = getPlayers();
        java.util.List<Player> current_list = current_players.all();

        int skip_count = 0;
        for (int i = 0; i < current_list.size(); i++) {
            Player current_player = current_list.get(i);
            String current_name = current_player.getName();

            // check if player's data has already been collected or if they are not animating
            if(checked_players.contains(current_name)){
                skip_count++;
                continue;
            }
//            else if (!current_player.isAnimating()){
//                // wait 30 seconds for current player to animate
//                boolean animated = sleepUntil(() -> current_player.getAnimation() != -1, 30000);
//
//                // if we had to wait, skip this person
//                if (!animated){
//                    log("Waited 30 seconds, skipping: " + current_name);
//                    checked_players.add(current_name);
//                    skip_count++;
//                    continue;
//                }
//            }

            // walk around once in awhile
            if (checked_players.size() % 5 == 0){
                log("Moving about..." + getLocalPlayer().getTile().translate(movement_int, 0));

                LocalPath path = new LocalPath(this);
                path.add(getLocalPlayer().getTile().translate(movement_int, 0));
                path.walk();
                sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
                movement_int = -movement_int; // reverse direction next time
            }

            checked_players.add(current_name);
            log("Added: " + current_name);

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
                log("Sending data for: " + current_name);
                data_string = data_string  + str_resp;
                String response = sendMessage(data_string);
                log("Response: " + response);
                if (response.contains("STOP")){
                    log("TRYING TO STOP");
                    System.exit(0);
                } else if (response.contains("OUT")) {
                    log("LOGGING OUT");
                    getTabs().logout();
                    getRandomManager().disableSolver("LOGIN");
                    sleep(3600 * 1000); // sleep for 3600 secs (1 hour)
                    getRandomManager().enableSolver("LOGIN");
                }

                datacount++;
            }
//            break;
        }

        // check since last time we switched worlds
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;

        // if time since last world hop is less than certain amount of time...
        if (timeElapsed < 60 * 1000){
//            log("Not enough time spent, sleeping for 10 more secs");
//            sleep(100 * 1000); // sleep for 100 secs
            return 10000;
        }

        startTime = System.currentTimeMillis();

        World w = world_list.remove(0);
        while (w.getMinimumLevel() > 0 || !w.isNormal()){
            w = world_list.remove(0);
        }
        if (world_list.size() == 0){
            world_list = new Worlds().all(wo -> wo != null && wo.isNormal() && wo.isF2P() && wo.getWorld() < 400);
            log("Went through all worlds, changing to area #" + areaID);
            changeArea(areaID);
            areaID += 1;
            if (areaID >= area.length) {
                areaID = 0;
            }
            return 8000;
        }
        log("Hopping to world: " + w.toString());
        getWorldHopper().hopWorld(w);

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
