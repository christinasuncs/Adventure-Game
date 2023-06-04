package zork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Parser {
  private CommandWords commands; // holds all valid command words
  private Scanner in;
  private ArrayList<String> filler = new ArrayList<String> (Arrays.asList("an", "a", "the", "this", "these", "those", "his", "her", "their", "its"));

  public Parser() {
    commands = new CommandWords();
    in = new Scanner(System.in);
  }

  public Command getCommand() throws java.io.IOException {
    String inputLine = "";
    ArrayList<String> words;

    System.out.print("> "); // print prompt

    inputLine = in.nextLine().toLowerCase(); //delete additional spaces and set all letters to lowercase

    //splits the user input based on the delimeter (i.e. space) and assigns it to words
    words = new ArrayList<String>(Arrays.asList(inputLine.split(" "))); 

    //get rid of articles (i.e. an, a and the)
    for(int i = words.size() - 1; i >= 0; i--){
      for(int j = 0; j < filler.size(); j++){
        if(words.get(i).equals(filler.get(j))){
          words.remove(i);
        }
      }
    }

    String word1 = words.get(0);
    String word2 = null;

    //find the command and set it to the first word
    for(int i = 0; i < words.size(); i++){
      if(commands.isCommand(words.get(i))){
        word1 = words.get(i);
        break;
      }
    }

    //find the noun and set it to the second word
    if (words.size() > 1) {
      word2 = null; //assume no second word

      int index = checkNoun(words); //check if valid noun inputed

      if(index > 0) {
        word2 = words.get(index);
      }
      else {
        for(String str: words){
          word2 += str;
        }
      }
    }

      if (words.size() == 0){
        return new Command(null, null);
      }else if(commands.isCommand(word1)) {
        return new Command(word1, word2);
      }else 
        return new Command(null, word2); 
  }

  //check to see if the user inputted a valid noun and return its index
  private int checkNoun(ArrayList<String> list) {
    ArrayList<String> items = new ArrayList<String>(Arrays.asList("cookie", "key", "extinguisher", "button", "wrapper", "note", "chips", "dumbbell", "book", "rag", "card", "trophy", "bear", "paint", "skull", "gum"));
    ArrayList<String> direction = new ArrayList<String>(Arrays.asList("north", "south", "east", "west", "up", "down"));
    for(int i = 0; i < list.size(); i++){
      if(items.contains(list.get(i)) || direction.contains(list.get(i)))
        return i;
    }
    return -1;
}

  // Print out a list of valid command words.
 
  public void showCommands() {
    commands.showAll();
  }
}
