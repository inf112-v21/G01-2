package inf112.skeleton.app.Cards;

import inf112.skeleton.app.Player;

public class Card {
    int cost;
    String type;
    public Card(){
        this.cost = 150;
        type= "MoveOne";
    }

    public void action(Player player) {
        for (int i = 0; i < 2; i++) {
            player.moveForward();
        }
    }

}
