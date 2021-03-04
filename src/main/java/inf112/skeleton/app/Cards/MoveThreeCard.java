package inf112.skeleton.app.Cards;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import inf112.skeleton.app.GameLogic.IPlayer;

public class MoveThreeCard extends Card {


    public MoveThreeCard(){
        textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(new Texture("src/main/Resources/moveThree.png")));
    }
    public void action(IPlayer player) {
        for (int i = 0; i < 3; i++) {
            player.moveForward();
        }
    }
}