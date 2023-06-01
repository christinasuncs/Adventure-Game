package zork;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPrint extends Game{
    public static void main(String[] args) {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override

            public void run() {
                System.out.println("RINGGGGGGGGGGGGGG!!!!!");
            }
        };

        timer.schedule(task, 0, 5000);
    }
}