import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;
import java.util.HashMap;

public class ServerCore {
    private final static int PORT = 4440;
    private ServerSocket serverSocket;
    private static boolean dev = true;
    private HashMap<String, ObjectOutputStream> activeUsers = new HashMap<>();

    ServerCore()
    {
        UsernamePasswordPairs.init();
        try {
            serverSocket = new ServerSocket(PORT);

            if(dev) System.out.println("Server : Core : Core is ready to start up");
        }
        catch(Exception e)
        {
            System.out.println("Core unable to start");
            if(dev) e.printStackTrace();
        }
    }

    void run()
    {
        if(serverSocket == null)
        {
            if(dev) System.out.println("Server : Core : Cannot run");
            return;
        }
        if(dev) System.out.println("Server : Core : Core started running");
        while(true)
        {
            try {
                Socket guestSocket = serverSocket.accept();
                if(dev) System.out.println("Server : Core : new guest found");
                Thread t;
                try{
                    t =new Thread(new GuestHandler(guestSocket, this));
                }
                catch (IOException e)
                {
                    System.out.println("Guest may have been disconnected while getting IO streams");
                    guestSocket.close();
                    continue;
                }

                t.start();
            } catch (IOException | SecurityException | IllegalBlockingModeException e) {
                System.out.println("Unexpected error occurred");
                if(dev) e.printStackTrace();
            }
            catch (NullPointerException e)
            {
                System.out.println("ServerSocket not initiated. Exiting...");
                if(dev) e.printStackTrace();
                break;
            }
        }
        UsernamePasswordPairs.flush();
    }

    synchronized boolean isLoggedIn(String username)
    {
        return activeUsers.containsKey(username);
    }

    synchronized void login(String username, ObjectOutputStream objectOutputStream)
    {
        if(!UsernamePasswordPairs.hasUser(username)) throw new IllegalArgumentException("'"+username+"' not registered");
        if(isLoggedIn(username)) throw new IllegalArgumentException("'"+username+"' already logged in");
        activeUsers.put(username, objectOutputStream);
    }

    synchronized void logout(String username)
    {
        if(!UsernamePasswordPairs.hasUser(username)) throw new IllegalArgumentException(username+" not registered");
        if(!isLoggedIn(username)) throw new IllegalArgumentException(username+" not logged in");
        activeUsers.remove(username);
    }

    ObjectOutputStream getStreamOf(String username)
    {
        if(activeUsers.containsKey(username)) return activeUsers.get(username);
        else return null;
    }

    public static void main(String[] args) throws Exception {

        FileOutputStream fos = new FileOutputStream("passwords");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        ServerCore core = new ServerCore();
        oos.writeObject(new HashMap<String, String>());
        core.run();
        UsernamePasswordPairs.init();
        UsernamePasswordPairs.addNewUser("nemo","daremo");
        UsernamePasswordPairs.flush();
    }
}
