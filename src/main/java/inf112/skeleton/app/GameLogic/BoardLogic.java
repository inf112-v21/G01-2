package inf112.skeleton.app.GameLogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import inf112.skeleton.app.Cards.*;
import inf112.skeleton.app.Network.NetworkClient;
import inf112.skeleton.app.Packets.TurnPacket;

import java.util.ArrayList;
import java.util.HashMap;

public class BoardLogic implements IBoardLogic {

    private NetworkClient networkClient;

    public TiledMap tiledMap;
    ArrayList<IPlayer> players;
    IPlayer myPlayer;
    Boolean gameOver = false;
    private Integer nrOfPlayers;
    ArrayList<String> collectFlags = new ArrayList();
    ArrayList<Vector2> repairsites;
    ArrayList<Vector2> repairsites2;
    ArrayList<Vector2> flagList;
    ArrayList<Vector2> holes;
    ArrayList<Vector2> spawnpoints;
    private ArrayList<Sprite> lasers;
    private Texture laserTexture;
    ArrayList<Vector2> rotateClockwise;
    ArrayList<Vector2> rotateCounterClockwise;

    HashMap<Vector2, String> conveyorBelts;
    HashMap<Vector2, String> walls;

    private boolean readyForProgram = true;

    public BoardLogic(TiledMap tiledMap, String ip) throws InterruptedException {
        laserTexture = new Texture(Gdx.files.internal("src/main/Resources/Laser.png"));
        lasers = new ArrayList<>();
        this.tiledMap = tiledMap;

        try{
            this.networkClient = new NetworkClient(this, ip);
        }catch (Exception e){
            System.out.println(e);
        }

        while(nrOfPlayers == null){
            Thread.sleep(1000);
        }
        players = new ArrayList<>();
        for (int i = 1; i <= nrOfPlayers; i++) {
            IPlayer playerToAdd = new Player(i, new Sprite(new Texture(Gdx.files.internal("src/main/Resources/robot/robot" + i + ".png"))));
            this.players.add(playerToAdd);
        }
        myPlayer = players.get(networkClient.getId()-1);

        spawnpoints = getSpawnPoints();
        if(spawnpoints.size() >= players.size()){
            for (IPlayer player : players) {
                player.setLocation(new Vector2(spawnpoints.get(player.getID()-1).x, spawnpoints.get(player.getID()-1).y));
                player.setRoation(-90);
            }
        }


        //networkClient.sendPlayer(myPlayer);

        //setter første spawn point som lastSavePoint
        myPlayer.setLastSavePoint(myPlayer.getLocation());

        repairsites = getObjects("fix1");
        repairsites2 = getObjects("fix2");
        holes = getObjects("hole");
        flagList = getFlags();
        walls = getWalls();
        rotateClockwise = getObjects("rotateClockwise");
        rotateCounterClockwise = getObjects("rotateCounterClockwise");

        conveyorBelts = getConveyorBelts();
    }

    /**
     * Checks if player is inside map.
     *
     * Returns true if player is inside map.
     * Returns false if player is outside map.
     */
    @Override
    public boolean checkOutOfBounds(IPlayer player) {
        MapProperties prop = tiledMap.getProperties();

        // Get tiledMap width and height.
        int mapWidth = prop.get("width", Integer.class);
        int mapHeight = prop.get("height", Integer.class);
        // Get tile pixel width and height.
        int tilePixelWidth = prop.get("tilewidth", Integer.class);
        int tilePixelHeight = prop.get("tileheight", Integer.class);
        // Calculate map width and height
        int mapPixelWidth = (mapWidth * tilePixelWidth)-150;
        int mapPixelHeight = (mapHeight * tilePixelHeight)-150;

        Vector2 playerLoc = player.getLocation();

        if(playerLoc.x > mapPixelWidth || playerLoc.y > mapPixelHeight) {
            return false;
        }

        else return !(playerLoc.x < 0) && !(playerLoc.y < 0);
    }

    @Override
    public boolean checkWin(IPlayer player){
        if(collectedFlags(player) == 4){
            networkClient.sendWin();
        }
         return collectedFlags(player) == 4;
    }

    @Override
    public Integer collectedFlags(IPlayer player) {
        Vector2 playerLoc = player.getLocation();
        if(collectFlags.size()==0 && playerLoc.equals(flagList.get(0))){
                collectFlags.add("Flag 1 collected");
                player.setLastSavePoint(flagList.get(0));
        }
        if(collectFlags.size()==1 && playerLoc.equals(flagList.get(1))){
                collectFlags.add("Flag 2 collected");
                player.setLastSavePoint(flagList.get(1));
        }
        if(collectFlags.size()==2 && playerLoc.equals(flagList.get(2))){
                collectFlags.add("Flag 3 collected");
                player.setLastSavePoint(flagList.get(2));
        }
        if(collectFlags.size()==3 && playerLoc.equals(flagList.get(3))){
                collectFlags.add("Flag 4 collected");
        } return collectFlags.size();
    }

    @Override
    public ArrayList<Sprite> getLaser() {
        return lasers;
    }


    @Override
    public boolean checkMovement(IPlayer player) {
        robotFallHole(player);
        rotatePlayer();
        if(!checkOutOfBounds(player)){
            System.out.println("Player fell and died");
            player.changeLifeTokens(-1); //endre HP til spilleren

            if (myPlayer.getLifeTokens() <= 0){ //hvis han ikke har HP igjen avslutt spillet
                setGameOver(true);
            }
            else {
                player.setRoation(-90);
                player.setLocation(player.getLastSavePoint()); //ellers endre posisjonen til siste savepoint
                networkClient.sendPlayer(myPlayer);
            }

        }
        robotFullDamage(player);
        checkWin(player);
        return false;
    }


    //this contains a lot of bugs and needs fixing
    @Override
    public boolean checkMove(IPlayer player){
        if(walls.get(player.getLocation()) == "wallNorth"  && Math.abs(player.getSprite().getRotation() % 360) == 180){
            return false;
        }
        if(walls.get(player.getLocation()) == "wallSouth" && Math.abs(player.getSprite().getRotation() % 360) == 0){
            return false;
        }
        if(walls.get(player.getLocation()) == "wallWest" && Math.abs(player.getSprite().getRotation() % 360) == 90){
            return false;
        }
        if(walls.get(player.getLocation()) == "wallEast" && Math.abs(player.getSprite().getRotation() % 360) == 270){
            return false;
        }
        return true;
    }
    public void rotatePlayer(){
        for (IPlayer player : players) {
            for(Vector2 loc : rotateClockwise) {
                if(player.getLocation().equals(loc)){
                    player.rotatePlayer(90);
                }
            }
            for(Vector2 loc : rotateCounterClockwise) {
                if(player.getLocation().equals(loc)){
                    player.rotatePlayer(-90);
                }
            }
        }
    }

    @Override
    public void robotFallHole(IPlayer player) {
        for (Vector2 loc : holes) {
            if (player.getLocation().equals(loc)) {
                player.changeLifeTokens(-1);
                player.setLocation(myPlayer.getLastSavePoint());
            }
        }

    }
    @Override
    public void robotFullDamage(IPlayer player) {
        if (player.getDamageTokens()>= 9) {
            player.changeLifeTokens(-1);
            player.setX(myPlayer.getLastSavePoint().x);
            player.setY(myPlayer.getLastSavePoint().y);
        }
    }
    @Override
    public void repairRobot(IPlayer player){

        for (Vector2 loc : repairsites) {
            if(player.getLocation().equals(loc) && player.getDamageTokens()>0){
                player.changeDamageTokens(-1);
            }
        }
        for (Vector2 loc : repairsites2) {
            if(player.getLocation().equals(loc) && player.getDamageTokens()>=2){
                player.changeDamageTokens(-2);
            }
            else if(player.getLocation().equals(loc) && player.getDamageTokens()==1){
                player.changeDamageTokens(-1);
        }
        }
    }
    @Override
    public HashMap<Vector2, String> getWalls(){
        HashMap<Vector2, String> walls = new HashMap<>();
        ArrayList<String> wallNames = new ArrayList<>();
        wallNames.add("wallNorth");
        wallNames.add("wallSouth");
        wallNames.add("wallWest");
        wallNames.add("wallEast");
        for (String elem : wallNames) {
            Integer index = tiledMap.getLayers().getIndex(elem);
            MapLayer wallObject = tiledMap.getLayers().get(index);
            for (int i = 0; i < wallObject.getObjects().getCount(); i++) {
                Float x = Float.parseFloat(wallObject.getObjects().get(i).getProperties().get("x").toString());
                Float y = Float.parseFloat(wallObject.getObjects().get(i).getProperties().get("y").toString());
                Vector2 wallLocation = new Vector2(x, y);
                walls.put(wallLocation, elem);
            }
        } return walls;
    }
    @Override
    public ArrayList<Vector2> getFlags(){
        ArrayList<Vector2> flagList = new ArrayList<>();
        Integer index = tiledMap.getLayers().getIndex("flag");
        MapLayer flagObject;
        try{
            flagObject = tiledMap.getLayers().get(index);
        }catch(Exception e){
            System.out.println(e);
            return new ArrayList<>();
        }
        for (int i = 1; i <= 4; i++) {
            Float x = Float.parseFloat(flagObject.getObjects().get("Flag"+i).getProperties().get("x").toString());
            Float y = Float.parseFloat(flagObject.getObjects().get("Flag"+i).getProperties().get("y").toString());
            Vector2 flagLocation = new Vector2(x,y);
            flagList.add(flagLocation);
        } return flagList;
    }
    @Override
    public HashMap<Vector2, String> getConveyorBelts() {
        HashMap<Vector2, String> conveyorBelts = new HashMap<>();
        ArrayList<String> names = new ArrayList<>();
        names.add("twoArrowSouth");
        names.add("twoArrowWest");
        names.add("oneArrowNorth");
        names.add("oneArrowSouth");
        names.add("oneArrowWest");
        names.add("oneArrowEast");
        for (String elem : names) {
            Integer index = tiledMap.getLayers().getIndex(elem);
            MapLayer conveyorObject = tiledMap.getLayers().get(index);
            for (int i = 0; i < conveyorObject.getObjects().getCount(); i++) {
                Float x = Float.parseFloat(conveyorObject.getObjects().get(i).getProperties().get("x").toString());
                Float y = Float.parseFloat(conveyorObject.getObjects().get(i).getProperties().get("y").toString());
                Vector2 conveyorLocation = new Vector2(x, y);
                conveyorBelts.put(conveyorLocation, elem);
            }
        } return conveyorBelts;
    }
    @Override
    public ArrayList<Vector2> getSpawnPoints(){
        ArrayList<Vector2> spawnPoints = new ArrayList<>();
        Integer index = tiledMap.getLayers().getIndex("spawns");
        MapLayer spawnObject;
        try{
            spawnObject = tiledMap.getLayers().get(index);
        }catch(Exception e){
            System.out.println(e);
            return new ArrayList<>();
        }

        for (int i = 1; i < 7; i++) {
            Float x = Float.parseFloat(spawnObject.getObjects().get("Spawn"+i).getProperties().get("x").toString());
            Float y = Float.parseFloat(spawnObject.getObjects().get("Spawn"+i).getProperties().get("y").toString());
            Vector2 spawnLocation = new Vector2(x,y);
            spawnPoints.add(spawnLocation);
        } return spawnPoints;
    }
    @Override
    public ArrayList<Vector2> getObjects(String name){
        ArrayList<Vector2> objectList = new ArrayList<>();
        int index = tiledMap.getLayers().getIndex(name);
        MapLayer mapObject;
        try{
            mapObject = tiledMap.getLayers().get(index);
        }catch(Exception e){
            System.out.println(e);
            return new ArrayList<>();
        }
        for (int i = 0; i < mapObject.getObjects().getCount(); i++) {
            Float x = Float.parseFloat(mapObject.getObjects().get(i).getProperties().get("x").toString());
            Float y = Float.parseFloat(mapObject.getObjects().get(i).getProperties().get("y").toString());
            Vector2 objectLocation = new Vector2(x,y);
            objectList.add(objectLocation);
        } return objectList;
    }
    @Override
    public void convey() {
        for (IPlayer player: players) {
            if (conveyorBelts.get(player.getLocation()) == "oneArrowNorth") {
                player.getSprite().translate(0, 150);
            }
            else if (conveyorBelts.get(player.getLocation()) == "oneArrowSouth") {
                player.getSprite().translate(0, -150);
            }
            else if (conveyorBelts.get(player.getLocation()) == "oneArrowWest") {
                player.getSprite().translate(-150, 0);
            }
            else if (conveyorBelts.get(player.getLocation()) == "oneArrowEast") {
                player.getSprite().translate(150, 0);
            }
            else if (conveyorBelts.get(player.getLocation()) == "twoArrowSouth") {
                player.getSprite().translate(0, -300);
            }
            else if (conveyorBelts.get(player.getLocation()) == "twoArrowWest") {
                player.getSprite().translate(-300, 0);
            }
        }

    }
    //to be removed in future iteration. This is just used for moving manually for testing
    @Override
    public void changePlayer(float x, float y, int id, float rotation){
        IPlayer curPlayer = players.get(id-1);
        curPlayer.setX(x);
        curPlayer.setY(y);
        curPlayer.setRoation(rotation);
        players.set(id-1, curPlayer);
    }

    @Override
    public void gameOver(int id){
        System.out.println("Player with ID " + id + " has won");
        gameOver = true;
    }

    @Override
    public Boolean getGameOver(){
        return this.gameOver;
    }


    @Override
    public void setNrOfPlayers(Integer nr){
        this.nrOfPlayers = nr;
    }
    @Override
    public ArrayList<IPlayer> getPlayers(){
        return this.players;
    }
    @Override
    public IPlayer getMyPlayer(){
        return this.myPlayer;
    }
    @Override
    public void setGameOver(Boolean gameOverValue){
        this.gameOver = gameOverValue;
    }
    @Override
    public void sendWin(){
        networkClient.sendWin();
    }
    @Override
    public void sendPlayer(IPlayer player){
        networkClient.sendPlayer(player);
    }

    @Override
    public void sendProgramList(ArrayList<Card> cardArrayList){
        readyForProgram = false;
        networkClient.sendProgramCards(cardArrayList);
    }

    @Override
    public void doTurn(TurnPacket turnPacket){
        ArrayList<Card> cards = new ArrayList<>();
        CardTranslator cardGenerator = new CardTranslator();
        for (String cardName: turnPacket.cards) {
            System.out.println(cardName);
            Card card = cardGenerator.translateFromStringToCard(cardName);
            cards.add(card);
        }
        for (int i = 0; i < cards.size(); i++) {
            System.out.println(turnPacket.ID.get(i));
            IPlayer playerToMove = players.get(turnPacket.ID.get(i)-1);

            if(checkMove(playerToMove)){
                cards.get(i).action(playerToMove, this);
            }

        }
        try{
            Thread.sleep(500);
        }catch (Exception e){
            System.out.println(e);
        }

        networkClient.doneTurn();
    }

    @Override
    public void nextRound() {
        convey();
        rotatePlayer();
        for (IPlayer pl: players) {
            repairRobot(pl);
        }
        Laser laser = new Laser(this, laserTexture);
        lasers = laser.createLasers(tiledMap);
        readyForProgram = true;


        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                lasers = new ArrayList<>();
            }
        }, 2);
    }

    @Override
    public boolean isReadyForNextRound() {
        return readyForProgram;
    }

    @Override
    public TiledMap getTiledMap() {
        return this.tiledMap;
    }

}
