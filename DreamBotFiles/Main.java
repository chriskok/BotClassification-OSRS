import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.*;
import java.util.List;
import java.util.HashMap;

import java.io.IOException;

@ScriptManifest(category = Category.MISC , name = "Bot Collector" , author = "SoWeGoOn", version = 0.01 )
public class Main extends AbstractScript
{

    //Snippet Source: https://dreambot.org/forums/index.php?/topic/10595-get-other-players-equipment/
    public static int[] getEquipped(Player p){
        int[] appearanceData = p.getComposite().getApperance();
        int[] equipment = new int[appearanceData.length];
        for(int i = 0; i < equipment.length; i++){
            equipment[i] = appearanceData[i] > 512 ? appearanceData[i]-512 : -1;
        }
        return equipment;
    }

    /*
    Dreambot Functionality Below
     */
    @Override
    public void onStart() {
        log("Script Started");
    }

    @Override
    public int onLoop()  {
        /*
        Set up the area we are monitoring, for example, the mining area west of the Dark Wizards
        south of Varrock
         */
        Area miningarea1 = new Area(3173, 3380, 3184, 3363, 0);
        //Set up a list to store our players
        ArrayList<String> playersCollected = new ArrayList<String>();
        //Get all players in the area
        List<Player> playerList = getPlayers().all();

        //Loop through each player
        playerList.forEach(p -> {

            //Set up a temporary Hashmap to store data in
            HashMap<String, String> currPlayer = new HashMap<String, String>();
            /*
            We want players that are doing something e.g. Mining an ore (isAnimating does not include walking)
            check to be sure they are NOT in combat so we dont collect data on people fighting NPCs
            [remember to remove that check if thats the bots were collecting though]
            */
            if(p != null && p.isAnimating() == true && p.isInCombat() == false && miningarea1.contains((p))) {
                currPlayer.put("username", p.getName());
                currPlayer.put("gear", Arrays.toString(getEquipped(p))); // Array = [hat, cape, amulet, weapon, body, shield, ??, legs, ??, gloves, boots]
                currPlayer.put("location", p.getTile().toString()); //(X, Y, Z) coordinates
                currPlayer.put("animation", String.valueOf(p.getAnimation())); //Animation ID of current player
                //Add that player to the list
                playersCollected.add(currPlayer.toString());
                //then clear the temporary hashmap for the next player
                currPlayer.clear();
            }

        });

        //Log the output for testing
        log(playersCollected.toString());
        /*
        Add Ability to send POST request of the list to a server
         */

        //Clear the List for the next batch of players
        playersCollected.clear();

        return 15000; //Amount of milliseconds the onLoop should loop
    }

    @Override
    public void onExit() {
        super.onExit();
    }

}
