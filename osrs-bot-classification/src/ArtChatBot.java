import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;

import java.util.Random;

@ScriptManifest(author = "ChronicCoder", name = "ASCII Art Bot", version = 0.1, description = "Shitty chat bot for the sake of art", category = Category.UTILITY)
public class ArtChatBot extends AbstractScript {

    public void onStart() {

    }

    public void onExit() {

    }

    // Art received from https://textart.io/art/tag/small/1
    private String[] pig = new String[]{"     ,--.   ,--.\n", "     \\  /~\\  /\n", "      )' a a `(\n","     (  ,---.  )\n","      ( o_o)'\n","        )`-'( hjw"};
    private String[] duck = new String[]{"         ,~~.\n","          (  9 )-_,\n","(\\__ )='-'\n"," \\ .    )  )\n","  \\ `-' /\n","   `~-'   hjw\n"};
    private String[] cat = new String[]{"  A_A\n"," (-.-)\n","  |-|\n"," /   \\\n","|     |   __\n","|  || |  |  \\__\n"," \\_||_/_/"};
    private String[] owl = new String[]{" /\\_/\\\n","((@v@))\n","():::()\n"," VV-VV"};

    public void printAnimal(int animalIndex) {
        log("animal chosen: " + animalIndex);
        for (String strTemp : owl){
            getKeyboard().type(strTemp, true);
            sleep(Calculations.random(1500, 2000));
        }
    }

    private Random RandomObj = new Random();
    @Override
    public int onLoop() {
        printAnimal(RandomObj.nextInt(3));
        return Calculations.random(10000, 20000);
    }
}
