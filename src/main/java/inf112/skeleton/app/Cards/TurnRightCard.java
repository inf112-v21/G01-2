package inf112.skeleton.app.Cards;

import inf112.skeleton.app.GameLogic.Player;

public class TurnRightCard extends Card{


    public void action(Player player) {
        player.rotatePlayer(-90);
    }
}
