import java.util.Scanner;

public class DemoClient2 {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        DemoClient dc = new DemoClient(in.nextLine(),in.nextLine(),in.nextLine());
        dc.objectOutputStream.writeObject(RequestType.SIGNUP);
        dc.objectOutputStream.writeObject(dc.username);
        dc.objectOutputStream.writeObject(dc.password);
        dc.objectOutputStream.writeObject(dc.password);
        dc.objectOutputStream.writeObject(new SimpleMessage(in.nextLine(), in.nextLine()));
    }
}
