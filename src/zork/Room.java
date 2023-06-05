package zork;

import java.util.ArrayList;

public class Room {

  private String roomName;
  private String description;
  private String dialogue;
  private String completionStatement;
  private boolean isTaskComplete;
  private ArrayList<Exit> exits;
  private ArrayList<Item> items;

  public ArrayList<Exit> getExits() {
    return exits;
  }

  public void setExits(ArrayList<Exit> exits) {
    this.exits = exits;
  }

  /**
   * Create a room described "description". Initially, it has no exits.
   * "description" is something like "a kitchen" or "an open court yard".
   */
  public Room(String description) {
    this.description = description;
    isTaskComplete = false;
    exits = new ArrayList<Exit>();
    items = new ArrayList<Item>();
  }

  public Room() {
    roomName = "DEFAULT ROOM";
    description = "DEFAULT DESCRIPTION";
    exits = new ArrayList<Exit>();
    items = new ArrayList<Item>();
    isTaskComplete = false;
  }

  public void addExit(Exit exit) throws Exception {
    exits.add(exit);
  }

  /**
   * Return the description of the room (the one that was defined in the
   * constructor).
   */
  public String shortDescription() {
    return exitString();
  }

  /**
   * Return a long description of this room, on the form: You are in the kitchen.
   * Exits: north west
   */
  public String longDescription() {

    return "\n" + "Room: " + roomName + "\n" + description + "\n" + exitString();
  }

  /**
   * Return a string describing the room's exits, for example "Exits: north west
   * ".
   */
  private String exitString() {
    String returnString = "Exits: ";
    for (Exit exit : exits) {
      returnString += exit.getDirection() + " ";
    }

    return returnString;
  }

  /**
   * Return the room that is reached if we go from this room in direction
   * "direction". If there is no room in that direction, return null.
   */
  public Room nextRoom(String direction) {
    try {
      for (Exit exit : exits) {

        if (exit.getDirection().equalsIgnoreCase(direction)) {
          String adjacentRoom = exit.getAdjacentRoom();

          return Game.roomMap.get(adjacentRoom);
        }

      }
    } catch (IllegalArgumentException ex) {
      System.out.println(direction + " is not a valid direction.");
      return null;
    }
    return null;
  }

  public void addItem(Item item) {
    items.add(item);
  }

  public void removeItem(Item item){
    items.remove(item);
  }

  public ArrayList<Item> getItems(){
    return items;
  }

  /*
   * private int getDirectionIndex(String direction) { int dirIndex = 0; for
   * (String dir : directions) { if (dir.equals(direction)) return dirIndex; else
   * dirIndex++; }
   * 
   * throw new IllegalArgumentException("Invalid Direction"); }
   */
  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDialogue(String dialogue) {
    this.dialogue = dialogue;
  }

  public String getDialogue(){
    return dialogue;
  }

  public void setCompletionStatement(String completionStatement){
    this.completionStatement = completionStatement;
  }

  public String getCompletionStatement(){
    return completionStatement;
  }

  public void setIsTaskComplete(boolean bool){
    isTaskComplete = bool;
  }

  public boolean isTaskComplete() {
    return isTaskComplete;
  }

}