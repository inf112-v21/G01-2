package inf112.skeleton.app.Cards;

import java.util.Random;

import inf112.skeleton.app.GameLogic.IBoardLogic;
import inf112.skeleton.app.GameLogic.IPlayer;

public class Card {

    Integer priorityNumber = new Random().nextInt(1001);

    public void action(IPlayer player, IBoardLogic boardLogic) {
    }

    public Integer getPriorityNumber() {
        return priorityNumber;
    }
}
