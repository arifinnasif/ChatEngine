import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class DemoClient {
    Socket socket;
    String message, username, password;
    ObjectOutputStream objectOutputStream;

    public DemoClient(String username, String password, String message)
    {
        this.username=username;
        this.password=password;
        this.message=message;

        try
        {
            socket=new Socket("127.0.0.1", 4440);
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            new DemoReadThread(new ObjectInputStream(socket.getInputStream()));
        }
        catch (Exception e)
        {
            //its just demo idiot!
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        DemoClient dc = new DemoClient(in.nextLine(),in.nextLine(),in.nextLine());
        dc.objectOutputStream.writeObject(RequestType.SIGNUP);
        dc.objectOutputStream.writeObject(dc.username);
        dc.objectOutputStream.writeObject(dc.password);
        dc.objectOutputStream.writeObject(dc.password);
        dc.objectOutputStream.writeObject(new SimpleMessage(in.nextLine(), in.nextLine()));
    }
}
