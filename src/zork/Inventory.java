package zork;

import java.util.ArrayList;

public class Inventory {
  private ArrayList<Item> items;
  private int maxWeight;
  private int currentWeight;

  public Inventory(int maxWeight) {
    this.items = new ArrayList<Item>();
    this.maxWeight = maxWeight;
    this.currentWeight = 0;
  }

  public int getMaxWeight() {
    return maxWeight;
  }

  public int getCurrentWeight() {
    return currentWeight;
  }

  public ArrayList<Item> getInventory(){
    return items;
  }

  public boolean addItem(Item item) {
    if (item.getWeight() + currentWeight <= maxWeight) {
      currentWeight += item.getWeight();
      return items.add(item);
    }
    else {
      System.out.println("Sorry...your backpack is full");
      return false;
    }
  }

  public void removeItem(String itemName){
    Item currItem = null;
    if(hasItem(itemName)){
      for(Item item: items){
        if(item.getName().equals(itemName))
          currItem = item;
      }
      for(int i = items.size() - 1; i >= 0; i--){
        if(items.get(i).getName().equals(itemName)){
          items.remove(i);
          currentWeight -= currItem.getWeight();
          if(!currItem.canEat()){
            System.out.println("You dropped a " + itemName);
          }
        }
      }

    }
  }
  
  public void display(){
    System.out.println("       ");
    System.out.println("Inventory:");
    for(Item i: items){
      System.out.println("-->" + i.getName());
    }
    double ratio = (double)getCurrentWeight()/maxWeight;
    double rounded = Math.round(ratio*100.0)/100.0;
    System.out.println("-->" + "Your backpack is " + rounded + "% full");
  }

  public boolean hasItem(String itemName){
    for(int i = 0; i < items.size(); i++){
      if(itemName.equals(items.get(i).getName())){
        return true;  //found in inventory
      }
    }
    return false; //not found
  }

}
