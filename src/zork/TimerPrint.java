package zork;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPrint{
    public TimerPrint() { //creates a new method within the class to activate the times
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override

            public void run() { //everytime the timer is run, it prints "RINGGGGGGGGGGGGGG!!!!!" to symbolize the school bell
                System.out.println(" ");
                System.out.println("RINGGGGGGGGGGGGGG!!!!!");
                System.out.println(" ");
                System.out.print(">"); //prints this to reformat the player's screen
            }
        };

        timer.schedule(task, 0, 180000); //schedules a timer performing the "run" task, every 3 minutes, with the fist bell printing as soon as the game starts
    }
}