package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.sound.sampled.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;


public class Game {

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();

  private Parser parser;
  private Room currentRoom;
  private Inventory inventory = new Inventory(20);
  private ArrayList<Item> validItems = inventory.getInventory();
  public static ArrayList<Item> itemsMap = new ArrayList<Item>();
  private ArrayList<Room> tasks = new ArrayList<Room>();
  private static int points = 0;

  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");

      currentRoom = roomMap.get("106"); //player starts in room 106
      new TimerPrint();
      for(Item item: itemsMap){ //place all of the items in the correct starting room
        String itemRoom = item.getRoom();
        Room room = roomMap.get(itemRoom);
        room.addItem(item);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
  }
  
  //Initialize all of the rooms
  private void initRooms(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonRooms = (JSONArray) json.get("rooms");

    for (Object roomObj : jsonRooms) {
      Room room = new Room();
      String roomName = (String) ((JSONObject) roomObj).get("name");
      String roomId = (String) ((JSONObject) roomObj).get("id");
      String roomDescription = (String) ((JSONObject) roomObj).get("description");
      String roomDialogue = (String) ((JSONObject) roomObj).get("dialogue");
      String roomCompletionStatement = (String) ((JSONObject) roomObj).get("completionStatement");
      room.setDescription(roomDescription);
      room.setRoomName(roomName);
      room.setDialogue(roomDialogue);
      room.setCompletionStatement(roomCompletionStatement);

      JSONArray jsonExits = (JSONArray) ((JSONObject) roomObj).get("exits");
      ArrayList<Exit> exits = new ArrayList<Exit>();
      for (Object exitObj : jsonExits) {
        String direction = (String) ((JSONObject) exitObj).get("direction");
        String adjacentRoom = (String) ((JSONObject) exitObj).get("adjacentRoom");
        String keyId = (String) ((JSONObject) exitObj).get("keyId");
        Boolean isLocked = (Boolean) ((JSONObject) exitObj).get("isLocked");
        Boolean isOpen = (Boolean) ((JSONObject) exitObj).get("isOpen");
        Exit exit = new Exit(direction, adjacentRoom, isLocked, keyId, isOpen);
        exits.add(exit);
      }
      room.setExits(exits);
      roomMap.put(roomId, room);
    }
  }

  //Initialize all of the items
  private void initItems(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonItems = (JSONArray) json.get("items");

    for(Object itemObj : jsonItems) {
      Item item;
      String itemName = (String) ((JSONObject) itemObj).get("name");
      String itemId = (String) ((JSONObject) itemObj).get("id");
      String itemDescription = (String) ((JSONObject) itemObj).get("description");
      String itemRoom = (String) ((JSONObject) itemObj).get("room");
      String itemTaskRoom = (String) ((JSONObject) itemObj).get("taskRoom");
      int itemWeight = (int)((long)((JSONObject) itemObj).get("weight"));
      boolean itemCanEat = (boolean) ((JSONObject) itemObj).get("canEat");
      boolean itemIsTask = (boolean) ((JSONObject) itemObj).get("isTask");
      boolean itemIsOpenable = (boolean) ((JSONObject)itemObj).get("isOpenable");
      item = new Item(itemWeight, itemName,itemIsOpenable, itemCanEat, itemIsTask, itemRoom, itemDescription, itemTaskRoom);
      itemsMap.add(item);
    }
  }

  /**
   * Main play routine. Loops until end of play.
   */
  public void play() {
    printWelcome();

    boolean finished = false;
    while (!finished) {
      Command command;
      try {
        if(points > 75){  //max amount of points to earn is 80
          endSequence();
        }
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  //If the player completes all the tasks, reaches end sequence
  private void endSequence() {
      Room room = roomMap.get("G12CA"); //Get room associated with ping pong ball
      currentRoom = roomMap.get("106"); //Player will restart in room 106
      System.out.println("------------------------------------");
      System.out.println("You see Alan walking towards you from across the hall.");
      System.out.println(room.getCompletionStatement());  //get Alan's completion statement about ping pong ball
      System.out.println("------------------------------------");
      System.out.println(currentRoom.longDescription());  //appear to restart game
      System.out.println("Would you like to eat the cookie? (yes/no)");
      Scanner scanner = new Scanner(System.in);
      String answer = scanner.nextLine().toLowerCase();
      if(answer.equalsIgnoreCase("yes")){ //player loses the game
        System.out.println("The cookie tastes funny. You see Krrisha standing over you smiling.");
        System.out.println("...your vision becomes hazy, until it all goes black.");
        endGame();
      }
      else if(answer.equalsIgnoreCase("no")){ //player wins the game
        System.out.println("You instead decide to smell the cookie.");
        System.out.println("The strong smell of rotten eggs burns your nose and eyes.");
        System.out.println("You see Krrisha standing outside the door...she looks oddly dissapointed.");
        System.out.println("You have won!");
        System.out.println();
        System.out.println("Do you want to play again? (yes/no):");   //player has the option to play again or end the game
        System.out.print(">");
        String playAgain = scanner.nextLine();

        if(playAgain.equalsIgnoreCase("yes")){
          resetGame();
          play();
        }
        else {
          endGame();
        }
        
      scanner.close();
      
    }
  }


private void resetGame() {
  try {
    initRooms("src\\zork\\data\\rooms.json");
    initItems("src\\zork\\data\\items.json");

    currentRoom = roomMap.get("106");
    points = 0;
    tasks = new ArrayList<Room>();
    inventory = new Inventory(20);

    for(Item item: itemsMap){
      String itemRoom = item.getRoom();
      Room room = roomMap.get(itemRoom);
      room.addItem(item);
    }

  } catch (Exception e) {
    e.printStackTrace();
  }
  parser = new Parser();
}

  //Print ending message and end the game
  private void endGame() {
    System.out.println("-------------------------------------------");
    System.out.println("Thank you for playing AfterSchool at BVG!");
    System.out.println("We hope you enjoyed the game!");
    System.out.println("And learned to never eat suspicious cookies...");
    System.out.println("--------------------------------------------");
    System.exit(0);
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    System.out.println();
    System.out.println("Welcome to Afterschool at BVG!");
    System.out.println("You will play as Christina and help her complete tasks around the school");
    System.out.println("As task are completed, you will earn points to boost your grade");
    System.out.println("Type 'help' to see all your available actions");
    System.out.println("Type 'task' to see the tasks left to complete.");
    System.out.println();
    System.out.println(currentRoom.longDescription());
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   */
  private boolean processCommand(Command command) {
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }
    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("go") || commandWord.equals("walk"))  //allow both go and walk (e.g. walk north or go north is the same thing)
      goRoom(command);
    else if (commandWord.equals("jump")){
      System.out.println("You scream in pain. How did you forget about your injured knee?");
    }
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    } else if (commandWord.equals("eat")) {
      eat(command); // allows player to eat an item (e.x eat cookie)
    } else if (commandWord.equals("run")){
      System.out.println("Do you think you can run with a knee injury?!");
    } else if(commandWord.equals("use")){
      use(command); // allows player to use an item (e.x use key)
    } else if(commandWord.equals("i") || commandWord.equals("inventory")){
      inventory.display(); // displays players inventory
    } else if(commandWord.equals("drop")){
      drop(command); // drops an item from players inventory
    } else if(commandWord.equals("take")){
      take(command); // allows player to take an item and add it to their inventory
    } else if(commandWord.equals("give")){
      give(command); // allows player to give an object in their inventory
    } else if(commandWord.equals("find")){
      find(command); // allows player to find an item (e.x find cookie)
    } else if(commandWord.equals("open")){
      open(command); // allows player to open objects like books and doors
    } else if (commandWord.equals("sing")){
      System.out.println("lalalalala");
    } else if (commandWord.equals("scream")){
      System.out.println("AAAAAAHHHHHHHHHHHHH");
    } else if (commandWord.equals("cry")) {
      System.out.println("Crying won't help you =)");
    }else if (commandWord.equals("look")){
      look(command); // allows player to see what items are in the room they're in
    } else if (commandWord.equals("fight")){
      System.out.println("You're not a good fighter Christina =)");
    } else if(commandWord.equals("throw")){
      System.out.println("Remember you aren't good at throwing");
    } else if(commandWord.equals("task")){
      displayTasks(); // displays the missions that the player must complete
    }
      return false;
  }

  // implementations of user commands:


  private void drop(Command command) {
    if(!command.hasSecondWord()){ //need an item to drop
      System.out.println("What do you want to drop?");
    } else {
      String item = command.getSecondWord(); 
      validItems = inventory.getInventory(); // checks if the item is in their inventory to drop

      //add item to room inventory when dropped
      for(Item i : validItems){
        if(i.getName().equals(item)){ // finds the item that the player wants to drop
          currentRoom.addItem(i);
        }
      }
      inventory.removeItem(item); // takes item out of inventory
      System.out.println("You dropped a " + item);
    }
  }

  private void find(Command command) {
    if(!command.hasSecondWord()){ // player needs to say what item they want to find 
      System.out.println("What do you want to find?");
      return;
    }
    String item = command.getSecondWord();
    Item currItem = null;
    for(Item i: currentRoom.getItems()){  // checks if the item is in the current room
      if(i.getName().equals(item)){
        currItem = i;
      }
    }
    if(currItem != null){ // if the item is in the room, the item is given to the player 
      inventory.addItem(currItem); // adds item to player inventory
      System.out.println("You found " + item + "!");
      currentRoom.removeItem(currItem); // removes item from the item list of the current room
    } else {  // if there is no such item, tell the player 
      System.out.println("There is no " + item + " in this room");
    }

    
  }

  private void give(Command command) {
    if(!command.hasSecondWord()){ // the player needs to say what item they want to give
      System.out.println("What do you want to give?");
      return;
    }
    String item = command.getSecondWord();
    Item currItem = null;
    validItems = inventory.getInventory();
    // item must be in player's inventory in order to give
    validItems.contains(currItem);for(Item i : validItems){
      if(i.getName().equals(item)){
        currItem = i;
      }
    }
    if(currItem == null){ //if not in inventory, player doesn't have item
      System.out.println("You don't have this item to give.");
      return;
    } else if (!(currItem.isTask())){ //if the item isn't part of a task, cannot give it 
      System.out.println("There is no one who wants the " + currItem.getName() + ".");
    } 
    if(currItem.isTask()){ 
      if(currItem.getTaskRoom().equalsIgnoreCase(currentRoom.getRoomName())){ //item must be given in the correct room
        System.out.println("You give the " + currItem.getName());
        incrementPoints(10);  //player earns 10 points
        currItem.setTask(false);  //item is no longer a task
        currentRoom.setIsTaskComplete(true);  //room's task is complete
        System.out.println(currentRoom.getCompletionStatement());
        tasks.remove(currentRoom);
        inventory.removeItem(currItem.getName());
      }
      else { 
        System.out.println("The " + currItem.getName() + " should be given in " + currItem.getTaskRoom() + ".");
      }
    }
  }
   
  private void take(Command command) {
    if(!command.hasSecondWord()){ //need an item to take
      System.out.println("What do you want to take?");
      return;
    }

    String item = command.getSecondWord();
    Item currItem = null;
    //check that item exists in the room
    for (Item validItem : currentRoom.getItems()) {
      if (validItem.getName().equalsIgnoreCase(item)) {
        currItem = validItem;
      }
    }
      if (currItem == null){  //check if the item is already in the player's inventory
        for(Item i: inventory.getInventory()){
          if(i.getName().equals(item)){
            System.out.println("You already have this item in your backpack.");
            return;
          }
        }
        System.out.println("This item is not available in the room");
      } else if(inventory.addItem(currItem)){
        currentRoom.removeItem(currItem); //take the item out of the room's inventory
        System.out.println("You have taken the " + currItem.getName());
        System.out.println("-->" + currItem.getName() + ": " + currItem.getDescription());
      }
  }

  private void eat(Command command) {
    if(!command.hasSecondWord()){ //need an item to be able to eat
      System.out.println("You can't eat something you don't have, can you?");
      return;
    }
    String item = command.getSecondWord();
    Item currItem = null;
    validItems = inventory.getInventory();

    for(Item i : validItems){
      if(i.getName().equals(item)){
        currItem = i;
      }
    }
    if(currItem == null){ //item must be in the player's inventory
      System.out.println("You don't have this item in your backpack");
      return;
    } else if(currItem.canEat()){
      ArrayList<String> responsesEat = new ArrayList<String>(Arrays.asList("That had a weird aftertaste... ", "That was tasty", "Your stomach growls...you must still be hungry"));
      int index = (int) (Math.random()*responsesEat.size());  //generate a random response from the list
      System.out.println(responsesEat.get(index));
        if("cookie".equals(currItem.getName())){  //if the item is the cookie, should give user key
          System.out.println("You bite into something hard, almost chipping your tooth.");
          System.out.println("Inside the cookie is a key!");
          System.out.println("You put it in your backpack.");
          incrementPoints(5);
          currItem.setTask(false);  //cookie item is no longer a task
          Item key = new Item();
          for(Item i: itemsMap){
            if(i.getName().equals("key")){
              key = i;
            }
          }
          if(key!= null){ //adds the key to inventory
            inventory.addItem(key);
            currentRoom.removeItem(key);
          }
        }
      inventory.removeItem(currItem.getName()); //take out item from inventory bc can only eat once
    }
    else{
      System.out.println("I don't think you can eat that.");
    }

  }

  private void use(Command command) {
    if(!command.hasSecondWord()){ //needs something to use
      System.out.println("What do you want to use?");
      return;
    }
    String name = command.getSecondWord();
    Item currItem = null;
    validItems = inventory.getInventory();

    //check if item is in inventory and set it to currItem if it is
    if(inventory.hasItem(name)){
      for(Item i: validItems){
        if(name.equals(i.getName())){
          currItem = i;
          break;
        }
      }
    }
    if(currItem == null){ //item must be inventory to use
      System.out.println("You do not have this item.");
      return;
    } else if(currItem.canEat()){  //assume that if the item is a food, the player wants to eat it.
      eat(command);
      inventory.removeItem(name);
    } else if(currItem.getName().equals("key")){  //if key, assume player wants to unlock door
      unlock(command);
    } else if(currItem.isTask()){ //if a task, assume player wants to give it away
      give(command);
    } else if(currItem.isOpenable()){//assume that by asking to "use" an item, the player wants to open it. 
      open(command);
    } else {  //item cannot be used
      System.out.println("You cannot use this to do anything.");
    }
  }

  private void unlock(Command command) { 
      ArrayList<Exit> exits = currentRoom.getExits();
      for(Exit e: exits){ //unlock all exits
        e.setLocked(false);
      }
      System.out.println("You have unlocked the door.");
      inventory.removeItem("key");  //can only use key once
      incrementPoints(5);
  }



  private void open(Command command) { 
    if(!command.hasSecondWord()){ //checks if there is an object to open
      System.out.println("What do you want to open?");
      return;
    }
    String name = command.getSecondWord(); //stores the item that the player wants
    Item currItem = null;
    int n = 0; //keeps track of the index of the item in the ArrayList

    for (int i = 0; i < itemsMap.size(); i++) { //goes through the ArrayList to check for the item
      Item curr = itemsMap.get(i);      

      if(curr.getName().equals(name)){ //checks if the item is equal to the item the player wants
        currItem = curr;
        n = i; //assigns the index of the item in the ArrayList
      }
    }
    if(currItem == null){ //returns nothing if the item is not found
      return;
    }

    boolean isopen = currItem.isOpenable(); //boolean variable that checks if the object is open

    if(isopen == false){ // the item cannot be opened, tell the player
      System.out.println("You can't open the " + currItem.getName());
      return;
    }
      // if the item is one of the openable items, display the appropriate message for the item
    else{
      if (currItem.getName().equals("chips")){
        System.out.println("You open the bag of chips and find some delicous sunchips to munch on.");
        currItem.setOpenable(false); // the item is no longer openeable 
        Item chips = currItem;
        itemsMap.set(n, chips); //sets the item in the array list with the new attributes
      }

      else if(currItem.getName().equals("wrapper")){
        System.out.println("You open the wrapper and find some moldy, 1-year-old mentos that are as hard as rock.");
        currItem.setOpenable(false); // the item is no longer openable 
        Item wrapper = currItem;
        itemsMap.set(n, wrapper); //sets the item in the array list with the new attributes
      }

      else if(currItem.getName().equals("paint")){
        System.out.println("You open the paint and it squirts all over you! Now you're sticky and look like a knock-off Megamind.");
        currItem.setOpenable(false); // the item is no longer openable 
        Item paint = currItem;
        itemsMap.set(n, paint); //sets the item in the array list with the new attributes
      }
      else if(currItem.getName().equals("book")){
        System.out.println("You open the book and find a diagram of reeds being crushed by rocks.");
        currItem.setOpenable(false); // the item is no longer openable 
        Item book = currItem;
        itemsMap.set(n, book); //sets the item in the array list with the new attributes
      }

    } 
  }


  private void printHelp() {  //give list of command words
    System.out.println("You're getting tired. It would be nice to be home right now...");
    System.out.println("Your command words are:");
    parser.showCommands(); // shows player all their available command words 
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    if (!command.hasSecondWord()) {
      // if there is no second word, we don't know where to go...
      System.out.println("Go where?");
      return;
    }

    String direction = command.getSecondWord();

    
    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction);

    //Check that the direction is a valid exit
    Exit exit = null;
    ArrayList<Exit> validExits = currentRoom.getExits();
    for(int i = 0; i < validExits.size(); i++){
      if(validExits.get(i).getDirection().equalsIgnoreCase(direction)){
          exit = validExits.get(i);
      }
    }

    if (exit == null) // there is no exit 
      System.out.println("You walk straight into a wall.");
    else if(exit.isLocked()){
      System.out.println("The door is locked. You cannot go this way...unless you have a key.");
    }
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
      if(currentRoom.isTaskComplete() == false){
      System.out.println(currentRoom.getDialogue());  //only print dialogue if task isn't complete
        if(currentRoom.getDialogue().length() > 1 && !(tasks.contains(currentRoom))){
          tasks.add(currentRoom);
        }
      }
    }
  }
  private void displayTasks() { // shows player all their pending missions/tasks
    System.out.println("Pending Tasks: ");
    if(tasks.size() == 0){
      System.out.println("There are no pending tasks");
    }
    else {
      for(Room task: tasks){  //get the name of the person associated with task and print it with room name
      String display = task.getRoomName() + ":" + " help " + task.getDialogue().substring(0,task.getDialogue().indexOf(":"));
      System.out.println(display);
      }
    }

  }

  private void look(Command command){
    System.out.println(" ");
    System.out.println("You're looking around Room: " + currentRoom.getRoomName()); 
    // displays name of the room the player is currently in 
    System.out.println(currentRoom.shortDescription());
    // displays a description of the room the player is in
    ArrayList<Item> roomItems = currentRoom.getItems();
    System.out.println("After a full 360 scan you find: ");
    if(roomItems.size() == 0){ // if there is no item in the rooms 
      System.out.println("The room is empty. Nothing of value in here...");
    }
    for(Item item: roomItems){  //give a list of items in the room
      if(!(item.getName().equals("key"))){
        System.out.println("-->" + item.getName());
      }
    }
  }


private void incrementPoints(int i) { // gives player points according to the mission they completed 
    points += i;
    System.out.println("You completed a task and earned " + i + " points!");
    System.out.println("Total points: " + points);
}

   private Clip musicClip;

   public void playMusic(String filePath) {
     try {
       File musicFile = new File(filePath);
       AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
       AudioFormat format = audioStream.getFormat();
       DataLine.Info info = new DataLine.Info(Clip.class, format);
       musicClip = (Clip) AudioSystem.getLine(info);

       musicClip.open(audioStream);
       musicClip.start();
   } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
       e.printStackTrace();
   }
 }

 public void stopMusic() {
   if (musicClip != null && musicClip.isRunning()) {
       musicClip.stop();
       musicClip.close();
   }
 }

 public static void downloadMusic(String musicUrl, String savePath) {
   try {
       URL url = new URL(musicUrl);
       InputStream in = new BufferedInputStream(url.openStream());
       FileOutputStream fos = new FileOutputStream(savePath);

       byte[] buffer = new byte[1024];
       int bytesRead;
       while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
           fos.write(buffer, 0, bytesRead);
       }

       fos.close();
       in.close();
   } catch (IOException e) {
       e.printStackTrace();
   }
 }

 public static void main(String[] args) {
   String musicUrl = "https://www.youtube.com/watch?v=E-6zrzmAh2s";
   String savePath = "path/to/save/music/videoplayback.mp3";
   downloadMusic(musicUrl, savePath);

 // Play the downloaded music
   Game game = new Game();
   game.playMusic(savePath);
 }
}