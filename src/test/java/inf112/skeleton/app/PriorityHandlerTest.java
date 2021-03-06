package inf112.skeleton.app;

import com.esotericsoftware.kryonet.Server;
import inf112.skeleton.app.Server.CardNoTexture;
import inf112.skeleton.app.Server.PriorityHandler;
import org.junit.Before;
import org.junit.Test;
import java.util.Random;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class PriorityHandlerTest {

   private PriorityHandler priorityHandler;

    @Before
    public void setUp(){
        priorityHandler = new PriorityHandler(new Server());
    }
    /**
     * Test if the list is sorted correctly by the priority number for each card
     */
    @Test
    public void sortTest(){
        ArrayList<ArrayList<CardNoTexture>> turns = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            turns.add(new ArrayList<>());
        }
        for (ArrayList<CardNoTexture> cards: turns) {
            for (int i = 0; i < 6; i++) {
                cards.add(new CardNoTexture("Hello", new Random().nextInt(1001), 1));
            }
        }
        ArrayList<ArrayList<CardNoTexture>> sortedTurns = priorityHandler.sortTurnList(turns);

        boolean isSorted = true;

        //checks the sorting to see if the previus is smaller than the next
        for (ArrayList<CardNoTexture> turn: sortedTurns) {
            Integer lastPriority = 0;
            for (CardNoTexture card: turn) {
                if(card.getCardPriority() < lastPriority){
                    isSorted = false;
                }
                lastPriority = card.getCardPriority();
            }
        }
        assertEquals(isSorted, true);

    }

}
