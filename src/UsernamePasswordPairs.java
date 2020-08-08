import java.io.*;
import java.util.HashMap;

public class UsernamePasswordPairs {
    private static HashMap<String, String> hashMap = new HashMap<>();
    private static final boolean dev = true;

    static synchronized void init()
    {
        try {
            FileReader fr1 = new FileReader("users");
            FileReader fr2 = new FileReader("passwords");
            BufferedReader br1 = new BufferedReader(fr1);
            BufferedReader br2 = new BufferedReader(fr2);
            //hashMap = new HashMap<>();
            while(true)
            {
                String k = br1.readLine();
                String v = br2.readLine();
                if(k==null||v==null) break;
                hashMap.put(k, v);
            }
        } catch (Exception e) {

            if(dev) e.printStackTrace();
        }
    }

    static synchronized void flush()
    {
        try
        {
            FileWriter fw1 = new FileWriter("users");
            FileWriter fw2 = new FileWriter("passwords");
            BufferedWriter bw1 = new BufferedWriter(fw1);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            for(String k : hashMap.keySet())
            {
                System.out.println(k);
                bw1.write(k);
                bw1.write('\n');

                bw2.write(hashMap.get(k));
                bw2.newLine();
            }
            bw1.flush();
            bw2.flush();
        }
        catch (Exception e)
        {
            if(dev) e.printStackTrace();
        }
    }

    static synchronized boolean hasUser(String username)
    {
        return hashMap.containsKey(username);
    }

    static synchronized boolean isPasswordCorrect(String username, String password)
    {
        if(!hasUser(username)) return false;
        return hashMap.get(username).equals(password);
    }

    static synchronized void addNewUser(String username, String password)
    {
        if(hasUser(username)) throw new IllegalArgumentException("'"+username+"' already exists");
        hashMap.put(username, password);
        System.out.println("New user ("+username+") added");
    }

    static synchronized void updatePassword(String username, String oldPassword, String newPassword)
    {
        if(!isPasswordCorrect(username, oldPassword))
        {
            if(dev) System.err.println("Server : UsernamePasswordPairs : oldPassword is not correct for '"+username+"'. Try using isPasswordCorrect method before using this one");
            return;
        }
        hashMap.replace(username, oldPassword, newPassword);
        System.out.println("Password for user '"+username+"' updated successfully");
    }

    static synchronized void removeUser(String username)
    {
        if(!hasUser(username)) throw new IllegalArgumentException("'"+username+"' doesn't exists");
        hashMap.remove(username);
    }
}
