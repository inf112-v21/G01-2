package inf112.skeleton.app;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import inf112.skeleton.app.GUI.RoboRallyGUI;
import inf112.skeleton.app.GUI.Screens.GameScreen;
import inf112.skeleton.app.GameLogic.IBoardLogic;
import inf112.skeleton.app.GameLogic.IPlayer;
import inf112.skeleton.app.Server.ServerConnect;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**

 * For each test the application will run and open,
 * to let the tests run correctly push the start button to open the map and then just close the window
 * by pressing on the X in the top right corner.
 *
 */
public class MapTests {

    private RoboRallyGUI game;
    private IBoardLogic board;
    private IPlayer myPlayer;
    private GameScreen gameScreen;
    Integer one = 1;
    @Before
    public void setUp() {
        ServerConnect connection = new ServerConnect();
        try{
            connection.start(4, 7979, 7878);
        }catch (Exception e){
            System.out.println(e);
        }

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        this.game = new RoboRallyGUI(true);
        cfg.setWindowedMode(1920, 1080);
        new Lwjgl3Application(this.game, cfg);

        this.gameScreen = this.game.getGameScreen();

        board = gameScreen.boardLogic;

        myPlayer = board.getMyPlayer();

        connection.stop();
    }
    /**
     * Test if map exist.
     */
    @Test
    public void testMap(){
        assertNotNull(board.getTiledMap());
    }
    /**
     * Test If player is inside map.
     */
    @Test
    public void testIfPlayerIsInsideMap(){
        assertTrue(board.checkOutOfBounds(myPlayer));
    }
    /**
     * Test If player touched a flag.
     */
    @Test
    public void testIfPlayerTouchedFlag(){

        myPlayer.getSprite().setX(2700);
        myPlayer.getSprite().setY(900);
        myPlayer.moveForward();

        boolean isTrue = false;
        for (Vector2 loc : board.getFlags()) {
            if(myPlayer.getLocation().equals(loc)){
                isTrue = true;
                break;
            }
        }
        assertTrue(isTrue);
    }
    /**
     * Test If player wins if he has visited the flags in the appropriate order.
     */
    @Test
    public void testPlayerWin(){

        ArrayList<Vector2> flagList = board.getFlags();

        myPlayer.setX(flagList.get(0).x);
        myPlayer.setY(flagList.get(0).y);

        System.out.println("flags collected: "+board.collectedFlags(myPlayer));

        myPlayer.setX(flagList.get(1).x);
        myPlayer.setY(flagList.get(1).y);

        System.out.println("flags collected: "+board.collectedFlags(myPlayer));

        myPlayer.setX(flagList.get(2).x);
        myPlayer.setY(flagList.get(2).y);

        System.out.println("flags collected: "+board.collectedFlags(myPlayer));

        myPlayer.setX(flagList.get(3).x);
        myPlayer.setY(flagList.get(3).y);

        System.out.println("flags collected: "+board.collectedFlags(myPlayer));

        assertTrue(board.checkWin(myPlayer));
    }
    /**
     * Test If player respawns after he falls off the map.
     *
     * Manuel test
     * Chose some cards that will send the player off the map and close the window.
     */
    @Test
    public void testIfPlayerRespawns(){

        assertEquals(myPlayer.getLocation(), myPlayer.getLastSavePoint());
    }
    /**
     * Test if the player takes one damage from laser.
     *
     * Manuel test
     * When the map is loaded choose some cards that will send the player in front of a laser and close the window.
     */
    @Test
    public void testRobotLaserDamage(){

        assertEquals(one, myPlayer.getDamageTokens());
    }
}