package zork;

public class Item extends OpenableObject {
  private int weight;
  private String name;
  private boolean isOpenable;
  private boolean canEat;
  private boolean isTask;
  private String room;
  private String description;

  public Item(){
    //default 
  }
  public Item(int weight, String name, boolean isOpenable, boolean canEat, boolean isTask, String room, String description) {
    this.weight = weight;
    this.name = name;
    this.isOpenable = isOpenable;
    this.canEat = canEat;
    this.isTask = isTask;
    this.room = room;
    this.description = description;
  }

  public void open() {
    if (!isOpenable)
      System.out.println("The " + name + " cannot be opened.");

  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOpenable() {
    return isOpenable;
  }

  public void setOpenable(boolean isOpenable) {
    this.isOpenable = isOpenable;
  }

  public String getRoom(){
    return room;
  }

  public boolean canEat(){
    return canEat;
  }

  public boolean isTask(){
    return isTask;
  }

  public void setTask(boolean b){
    isTask = b;
  }

  public String getDescription(){
    return description;
  }

}
