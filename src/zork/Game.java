package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();

  private Parser parser;
  private Room currentRoom;
  private Inventory inventory = new Inventory(100);
  private ArrayList<Item> validItems = inventory.getInventory();
  public static ArrayList<Item> itemsMap = new ArrayList<Item>();
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
      room.setDescription(roomDescription);
      room.setRoomName(roomName);

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
      int itemWeight = (int)((long)((JSONObject) itemObj).get("weight"));
      boolean itemCanEat = (boolean) ((JSONObject) itemObj).get("canEat");
      boolean itemIsTask = (boolean) ((JSONObject) itemObj).get("isTask");
      item = new Item(itemWeight, itemName,false, itemCanEat, itemIsTask, itemRoom, itemDescription);
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
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    System.out.println("Thank you for playing.  Good bye.");
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
      if(!command.hasSecondWord()){
        System.out.println("What do you want to drop?");
      } else {
          inventory.removeItem(command.getSecondWord());
      }
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
    }
      return false;
  }

  // implementations of user commands:


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
    // else if(currItem.isTask()){
    //   //increment points
    //   //set isTask to false so cannot complete task more than once
    //   //only items that we need to give are tasks

    // }
  }

  private void take(Command command) {
    if(!command.hasSecondWord()){
      System.out.println("What do you want to take?");
      return;
    }
    String item = command.getSecondWord();
    Item currItem = null;

    //assuming getValidItems() returns a valid list of items in the room

    //List<Item> validItems = getValidItems();

    for (Item validItem : validItems) {
      if (validItem.getName().equalsIgnoreCase(item)) {
        currItem = validItem;
        validItems.remove(validItem);
      }
      if (currItem==null){
        System.out.println("This item doesn't exist or it isn't here");
      }
      if(inventory.addItem(currItem)){
        //currentRoom.removeItem(currItem);
        System.out.println("You have taken the " + currItem.getName());
      }
    }

      //need way to get all the valid items in the room and check if secondWord matches
      //once item is added remove it from list of items in room

  }

  private void eat(Command command) {
    if(!command.hasSecondWord()){ //need an item to be able to eat
      System.out.println("You can't eat air, can you?");
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
          //inventory.addItem(key);
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
    } else{ //assume that by asking to "use" an item, the player wants to open it. 
      open(command);
    }

    
  }

  private void open(Command command) {  //will need to find a way to check if the object is in the room
    if(!command.hasSecondWord()){
      System.out.println("What do you want to open?");
      return;
    }
    String name = command.getSecondWord();
    Item currItem = null;
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
    }
  }
}
