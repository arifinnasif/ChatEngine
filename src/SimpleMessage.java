import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class SimpleMessage implements Serializable {
    private String sender;
    private String[] recipients;
    private long timeMillis;
    private String message;

    SimpleMessage(String recipient, String message)
    {
        recipients = new String[1];
        this.sender=null;
        this.recipients[0]=recipient;
        this.timeMillis=System.currentTimeMillis();
        this.message=message;
    }

    SimpleMessage(String[] recipients, String message)
    {
        this.sender=null;
        this.recipients=recipients;
        this.timeMillis=System.currentTimeMillis();
        this.message=message;
    }

    void setSender(String sender) {
        this.sender = sender;
    }

    public String[] getRecipients() {
        return recipients;
    }

    @Override
    public String toString() {
        return "[ "+sender+" | "+new Date(timeMillis)+" ] : "+message;
    }
}
