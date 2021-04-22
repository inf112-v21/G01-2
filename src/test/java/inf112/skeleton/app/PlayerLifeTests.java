package inf112.skeleton.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import inf112.skeleton.app.GUI.RoboRallyGUI;
import inf112.skeleton.app.GUI.Screens.GameScreen;

import inf112.skeleton.app.GameLogic.IBoardLogic;
import inf112.skeleton.app.GameLogic.IPlayer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
/**
 * You must run ServerStart before running the tests. Max players 11.
 *
 * You have to run the test one by one with the maximum of eleven test before having to restart the server.
 *
 * For each test the application will run and open,
 * to let the tests run correctly push the start button to open the map and then just close the window
 * by pressing on the X in the top right corner.
 *
 */
public class PlayerLifeTests {

    private RoboRallyGUI game;
    private IBoardLogic board;
    private IPlayer myPlayer;
    private GameScreen gameScreen;

    Integer three = 3;
    Integer two = 2;

    @Before
    public void setUp(){
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        this.game = new RoboRallyGUI();
        cfg.setWindowedMode(1920, 1080);
        new Lwjgl3Application(this.game, cfg);

        this.gameScreen = this.game.getGameScreen();

        board = gameScreen.boardLogic;

        myPlayer = board.getMyPlayer();
    }
    /**
     * Test if the player have three lives in beginning of game.
     */
    @Test
    public void testIfRobotHasThreeLives(){

        assertEquals(three, myPlayer.getLifeTokens());
    }
    /**
     * Test if the player loses one life if he falls in a hole.
     */
    @Test
    public void testFallsInHole(){

        myPlayer.getSprite().setX(3450);
        myPlayer.getSprite().setY(1350);

        for(int i = 0; i < 2; i++){
            board.robotFallHole();
            myPlayer.moveForward();
        }
        assertEquals(two, myPlayer.getLifeTokens());
    }
    /**
     * Test if the player loses one life if he falls of the map.
     */
    @Test
    public void testFallsOutsideMap(){

        myPlayer.rotatePlayer(-180);
        myPlayer.moveForward();
        myPlayer.moveForward();
        board.robotFallOutsideMap();

        assertEquals(two, myPlayer.getLifeTokens());
    }
}
