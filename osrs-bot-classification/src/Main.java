import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;

/**
 * Created by User on 03/16/2020.
 */
@ScriptManifest(
        category = Category.UTILITY, name = "Bot Classification", author = "ChronicCoder", version = 0.1
)
public class Main extends AbstractScript {

    public void onStart() {
        log("Welcome to Bot Detection Data Collection Script.");
    }

    @Override
    public int onLoop() {
        log("Checking players:");
        Players current_players = getPlayers();
        java.util.List<Player> current_list = current_players.all();

        for (int i = 0; i < current_list.size(); i++) {
            System.out.println(current_list.get(i));
        }
        return 5000;
    }
}
