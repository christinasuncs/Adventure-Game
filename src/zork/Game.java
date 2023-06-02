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
  private Inventory inventory = new Inventory(10);
  private ArrayList<Item> validItems = inventory.getInventory();
  public static ArrayList<Item> itemsMap = new ArrayList<Item>();
  private static int points = 0;
  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");

      currentRoom = roomMap.get("106");

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

  //We need to initialize the items here once we create the items.json
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
        if(points > 50){
          endSequence();
        }
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    System.out.println("Thank you for playing. Good bye.");
  }

  private void endSequence() {
    if(points > 50){
      currentRoom = roomMap.get("106");
      currentRoom.longDescription();
      System.out.println("Would you like to eat the cookie?");
      Scanner scanner = new Scanner(System.in);
      String answer = scanner.nextLine().toLowerCase();
      if(answer.equals("yes")){
        System.out.println("The cookie tastes funny. You see Krisha standing over you...");
        System.out.println("your vision becomes hazy, until it all goes black.");
        endGame();
      }
      else if(answer.equals("no")){
        System.out.println("You instead decide to smell the cookie.");
        System.out.println("The strong smell of rotten eggs burns your nose and eyes");
        System.out.println("You see Krisha standing outside the door...she looks oddly dissapointed");
        System.out.println();
        System.out.println("Do you want to play again? (yes/no):"); 
        String playAgain = scanner.nextLine();

        if(playAgain.equalsIgnoreCase("yes")){
          resetGame();
          play();
        }
        else {
          endGame();
        }
      }
      scanner.close();
      
    }
  }

  private void resetGame() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");

      currentRoom = roomMap.get("106");

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

  private void endGame() {
    System.out.println("Thank you for playing AfterSchool at BVG!");
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
      eat(command);
    } else if (commandWord.equals("run")){
      System.out.println("Do you think you can run with a knee injury?!");
    } else if(commandWord.equals("use")){
      use(command);
    } else if(commandWord.equals("i") || commandWord.equals("inventory")){
      inventory.display();
    } else if(commandWord.equals("drop")){
      drop(command);
    } else if(commandWord.equals("take")){
        take(command);
    } else if(commandWord.equals("give")){
        give(command);
    } else if(commandWord.equals("find")){
        find(command);
    } else if (commandWord.equals("sing")){
      System.out.println("lalalalala");
    } else if (commandWord.equals("scream")){
      System.out.println("AAAAAAHHHHHHHHHHHHH");
    } else if (commandWord.equals("cry")) {
      System.out.println("Crying won't help you =)");
    }else if (commandWord.equals("look")){
      look(command);
    }else if (commandWord.equals("fight")){
      System.out.println("You're not a good fighter Christina =)");
    }else if(commandWord.equals("throw")){
      System.out.println("Remember you aren't good at throwing");
    }
      return false;
  }

  // implementations of user commands:


  private void drop(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you want to drop?");
    } else {
      String item = command.getSecondWord();
      validItems = inventory.getInventory();
  
      for(Item i : validItems){
        if(i.getName().equals(item)){
          currentRoom.removeItem(i);
        }
      }
      inventory.removeItem(item);
    }
  }

  private void find(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you want to find?");
      return;
    }
    String item = command.getSecondWord();
    Item currItem = null;
    for(Item i: currentRoom.getItems()){
      if(i.getName().equals(item)){
        currItem = i;
      }
    }
    if(currItem != null){
      inventory.addItem(currItem);
      System.out.println("You found " + item + "!");
      currentRoom.removeItem(currItem);
    } else {
      System.out.println("There is no" + item + "in this room");
    }

    
  }

  private void give(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you want to give?");
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
    if(currItem == null){
      System.out.println("You don't have this item to give.");
      return;
    } 
    if(currItem.isTask()){
      if(currItem.getTaskRoom().equalsIgnoreCase(currentRoom.getRoomName())){
        System.out.println("You give the " + currItem.getName());
        incrementPoints(10);
        currItem.setTask(false);
        System.out.println(currentRoom.getCompletionStatement());
      }
      else {
        System.out.println("The" + currItem.getName() + "should be given in" + currItem.getTaskRoom() + ".");
      }
    }
  }
   
  private void take(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you want to take?");
      return;
    }

    String item = command.getSecondWord();
    Item currItem = null;

    for (Item validItem : currentRoom.getItems()) {
      if (validItem.getName().equalsIgnoreCase(item)) {
        currItem = validItem;
      }
    }
      if (currItem == null){
        System.out.println("This item is not available in the room");
      } else if(inventory.addItem(currItem)){
        currentRoom.removeItem(currItem);
        System.out.println("You have taken the " + currItem.getName());
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
    if(currItem == null){
      System.out.println("You don't have this item in your backpack");
      return;
    } else if(currItem != null && currItem.canEat()){
      ArrayList<String> responsesEat = new ArrayList<String>(Arrays.asList("That had a weird aftertaste... ", "That was tasty", "Your stomach growls...you must still be hungry"));
      int index = (int) (Math.random()*responsesEat.size());  //generate a random response from the list
      System.out.println(responsesEat.get(index));
        if("cookie".equals(currItem.getName())){  //if the item is the cookie, should give user key
          System.out.println("You bite into something hard, almost chipping your tooth.");
          System.out.println("Inside the cookie is a key!");
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
    if(!command.hasSecondWord()){
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
    if(currItem == null){
      System.out.println("You do not have this item.");
      return;
    }
    if(currItem.canEat()){  //assume that if the item is a food, the player wants to eat it.
      eat(command);
    } else if(currItem.getName().equals("key")){
      unlock(command);
    } else if(currItem.isTask()){ //if it is a task increment points and display completion statement
      incrementPoints(5);
      currItem.setTask(false);
      System.out.println(currentRoom.getCompletionStatement());
    } else {//assume that by asking to "use" an item, the player wants to open it. 
      open(command);
    }
    inventory.removeItem(name);
  }

  private void unlock(Command command) {
      ArrayList<Exit> exits = currentRoom.getExits();
      for(Exit e: exits){
        e.setLocked(false);
      }
      System.out.println("You have unlocked the door.");
      inventory.removeItem("key");
      incrementPoints(5);
  }



  private void open(Command command) {  //will need to find a way to check if the object is in the room
    if(!command.hasSecondWord()){
      System.out.println("What do you want to open?");
      return;
    }
    String name = command.getSecondWord();
    Item currItem = null;
    int n = 0;

    for (int i = 0; i < itemsMap.size(); i++) {
      Item curr = itemsMap.get(i);      

      if(curr.getName().equals(name)){
        currItem = curr;
        n = i;
      }
    }

    boolean isopen = currItem.isOpenable();

    if(isopen == false){
      System.out.println("This object is not openable.");
    }

    else{
      if (currItem.getName().equals("chips")){
        System.out.println("You open the bag of chips and find some delicous sunchips to munch on.");
        currItem.setOpenable(false);
        Item chips = currItem;
        itemsMap.set(n, chips);
      }

      else if(currItem.getName().equals("wrapper")){
        System.out.println("You open the wrapper and finds some moldy, 1-year-old mentos that are as hard as rock.");
        currItem.setOpenable(false);
        Item wrapper = currItem;
        itemsMap.set(n, wrapper);
      }

      else {
        System.out.println("You open the book and find a diagram of reeds being crushed by rocks.");
        currItem.setOpenable(false);
        Item book = currItem;
        itemsMap.set(n, book);
      }


    }
    //get rooms current items    
  }

  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println("You're getting tired. It would be nice to be home right now...");
    System.out.println("Your command words are:");
    parser.showCommands();
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

    if (exit == null)
      System.out.println("You walk straight into a wall.");
    else if(exit.isLocked()){
      System.out.println("The door is locked. You cannot go this way...unless you have a key.");
    }
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
      System.out.println(currentRoom.getDialogue());
    }

  }
  private void look(Command command){
    System.out.println("You're looking around the room.");
    System.out.println(currentRoom.longDescription());
    ArrayList<Item> roomItems = currentRoom.getItems();
    System.out.println("You scan the room and see: ");
    for(Item item: roomItems){
      System.out.println("-->" + item);
    }
  }


private void incrementPoints(int i) {
    points += i;
    System.out.println("You completed a task and earned 10 points!");
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
  String savePath = "path/to/save/music/sample.mp3";
  downloadMusic(musicUrl, savePath);

  // Play the downloaded music
  Game game = new Game();
  game.playMusic(savePath);
}
}
