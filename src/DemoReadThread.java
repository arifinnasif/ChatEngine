import java.io.IOException;
import java.io.ObjectInputStream;

public class DemoReadThread implements Runnable {
    private ObjectInputStream objectInputStream;
    private Thread t;
    public DemoReadThread(ObjectInputStream objectInputStream)
    {
        this.objectInputStream=objectInputStream;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while(true)
        {
            try {
                Object obj = objectInputStream.readObject();
                System.out.println(obj);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
