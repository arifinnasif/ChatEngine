import java.io.Serializable;

public class StatusMessage implements Serializable {
    private final String sender;
    private final String message;
    private final long currentTimeMillis;
    StatusMessage(String sender, String message)
    {
        this.sender=sender;
        this.message=message;
        this.currentTimeMillis = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    @Override
    public String toString() {
        return sender+" : "+message;
    }
}
