package zork;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPrint{
    public TimerPrint() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override

            public void run() {
                System.out.println(" ");
                System.out.println("RINGGGGGGGGGGGGGG!!!!!");
                System.out.println(" ");
                System.out.print(">");
            }
        };

        timer.schedule(task, 0, 180000);
    }
}