package inf112.skeleton.app.GUI.Screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import inf112.skeleton.app.GUI.HUD.Hud;
import inf112.skeleton.app.GUI.RoboRallyGUI;
import inf112.skeleton.app.GameLogic.*;

public class GameScreen {

    public TiledMap tiledMap;
    public IBoardLogic boardLogic;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    SpriteBatch sb;
    Hud hud;
    SpriteBatch sbHud;
    private IInputProcess IInputProcess;
    private RoboRallyGUI rgb;


    public GameScreen(RoboRallyGUI rgb, String ip){
        this.rgb = rgb;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

        camera.update();

        tiledMap = new TmxMapLoader().load("src/main/Resources/map/roboRallyMap.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        sb = new SpriteBatch();

        try {
            boardLogic = new BoardLogic(tiledMap, ip);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (boardLogic.getPlayers() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }





        IInputProcess = new InputProcess(camera, boardLogic.getMyPlayer(), boardLogic);
        sbHud = new SpriteBatch();
        hud = new Hud(sbHud, boardLogic);

    }

    public void stageRefresh(){

        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        sb.setProjectionMatrix(camera.combined);

        sb.begin();

        //draw players
        for (IPlayer player : boardLogic.getPlayers()) {
            player.getSprite().draw(sb);
        }

        for (Sprite lazer: boardLogic.getLaser()) {
            lazer.draw(sb);
        }



        sb.end();
        sbHud.setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
        //This is not the right way, but the MVP way
        if (boardLogic.getGameOver()) {
            rgb.setEndScreenVisible();
        }
    }

    public Stage getHudStage() {
        return hud.getStage();
    }

    public inf112.skeleton.app.GameLogic.IInputProcess getIInputProcess() {
        return IInputProcess;
    }
}
