import java.io.Serializable;

public class LoginMessage implements Serializable {
    private final String userName;
    private final String password; // must implement hashing

    LoginMessage(String userName, String password)
    {
        this.userName=userName;
        this.password=password;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }
}
