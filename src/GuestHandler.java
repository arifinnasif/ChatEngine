import java.io.*;
import java.net.Socket;

public class GuestHandler implements Runnable {
    private final static boolean dev = true;
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ServerCore core;
    private String username;

    GuestHandler(Socket socket, ServerCore core) throws IOException {
        this.socket = socket;
        this.core = core;
        this.username = null;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        //now read object and do things... like may be... login sending data etc

        /*
         <----------------------Start of Login------------------->
         */

        try {
            Object initialRequest = objectInputStream.readObject();

            if((initialRequest instanceof RequestType)&&((RequestType)initialRequest)==RequestType.LOGIN)
            {
                int t = 3;
                while(t-->0)
                {
                    Object obj = objectInputStream.readObject();
                    if((obj != null) && (obj instanceof LoginMessage))
                    {
                        if(isLoginValid((LoginMessage) obj))
                        {

                            if(dev) System.out.println(((LoginMessage) obj).getUserName()+" logged in");
                            objectOutputStream.writeObject(new StatusMessage("Server","LOGIN SUCCESSFUL"));
                            if(!core.isLoggedIn(((LoginMessage) obj).getUserName())) core.login(((LoginMessage) obj).getUserName(), objectOutputStream);
                            else objectOutputStream.writeObject(new StatusMessage("Server","You are already logged in as "+ ((LoginMessage) obj).getUserName()));
                            this.username=((LoginMessage) obj).getUserName();
                            break;
                        }
                        else
                        {
                            objectOutputStream.writeObject(new StatusMessage("Server","Username or Password incorrect. Try again. "+t+" trials remaining."));
                        }
                    }
                    else
                    {
                        t=0;
                        if(dev) System.out.println("Server : GuestHandler : Received message is not of type LoginMessage.");
                    }

                    if(t==0)
                    {
                        objectOutputStream.writeObject(new StatusMessage("Server", "ACCESS DENIED"));
                        objectOutputStream.close();
                        objectInputStream.close();
                        socket.close();
                        if(dev) System.out.println("Guest kicked out");
                        return;
                    }


                }
            }
            else if((initialRequest instanceof RequestType)&&((RequestType)initialRequest)==RequestType.SIGNUP)
            {
                int t = 5;
                Object usr = null;
                objectOutputStream.writeObject(new StatusMessage("Server", "Enter username")); //exploit alert : any user can tell you to enter password as server!
                                                                                                                            //possible solution : dont forward status messages without setting the actual sender!
                while (t-->0 && (usr = objectInputStream.readObject()) instanceof String && UsernamePasswordPairs.hasUser((String)usr))
                {
                    objectOutputStream.writeObject(new StatusMessage("Server", "Username '"+(String)usr+"' is already taken. Enter another one. "+t+" trials remaining before disconnect."));
                }

                if(!(usr instanceof String)) {
                    if(dev) System.out.println("Received object is not of type String");
                    cutoff();
                    return;
                }

                username = (String)usr;

                objectOutputStream.writeObject(new StatusMessage("Server", "Enter password"));
                Object pass1 = objectInputStream.readObject();

                if(!(pass1 instanceof String)) {
                    if(dev) System.out.println("Server : GuestHandler : Received object is not of type String");
                    cutoff();
                    return;
                }

                objectOutputStream.writeObject(new StatusMessage("Server", "Confirm password"));
                Object pass2 = objectInputStream.readObject();

                if(!(pass2 instanceof String)) {
                    if(dev) System.out.println("Server : GuestHandler : Received object is not of type String");
                    cutoff();
                    return;
                }

                if(!((String)pass1).equals((String)pass2))
                {
                    objectOutputStream.writeObject(new StatusMessage("Server", "PASSWORDS DID NOT MATCH"));
                    if(dev) System.out.println("Server : GuestHandler : Passwords didn't match while signing up");
                    cutoff();
                    return;
                }
                UsernamePasswordPairs.addNewUser(username, (String)pass1);
                if(!core.isLoggedIn(username)) core.login(username, objectOutputStream);
                else objectOutputStream.writeObject(new StatusMessage("Server","You are already logged in as "+ username));
            }
            else
            {
                if(dev) System.out.println("Server : GuestHandler : Request is not of type RequestType.SIGNUP or RequestType.LOGIN");
                cutoff();
                return;
            }
        }
        catch (NotSerializableException e)
        {
            System.out.println("SENDING FAILED : Invalid object");
            if(dev) {
                System.out.println("Server : GuestHandler : NonSerialized objects cannot be sent");
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            System.out.println("Unexpected error occurred.");
            if(dev) e.printStackTrace();
            if(dev) System.out.println("Guest kicked out");
            return;
        }

        /*
        <-------------------End of Login------------->
         */

        /*
        <------------------Start of Message Forwarding--------------->
         */
        try {
            while(true)
            {
                Object obj = objectInputStream.readObject();
                if(obj instanceof SimpleMessage)
                {
                    for(String recipient : ((SimpleMessage)obj).getRecipients())
                    {
                        if(!UsernamePasswordPairs.hasUser(recipient))
                        {
                            objectOutputStream.writeObject(new StatusMessage("Server","User '"+recipient+"' doesn't exist."));
                            if(dev) System.out.println("Server : GuestHandler : User '"+username+"' tried to send message to a non-existent client. Action aborted.");
                        }
                        else if(!core.isLoggedIn(recipient))
                        {
                            objectOutputStream.writeObject(new StatusMessage("Server", recipient+" is not active. Message could not be sent."));
                            if(dev) System.out.println("Server : GuestHandler : User '"+username+"' tried to send message to a non-active client. Message not sent.");
                        }
                        else
                        {
                            ((SimpleMessage)obj).setSender(username);
                            core.getStreamOf(recipient).writeObject(obj);
                        }
                    }
                }
                else if(obj instanceof RequestType)
                {
                    switch ((RequestType) obj)
                    {
                        case LOGOUT:
                            objectOutputStream.writeObject(new StatusMessage("Server", "You are being logged out"));
                            core.logout(username);
                            cutoff();
                            return;

                        case RESET_PASSWORD:
                            objectOutputStream.writeObject(new StatusMessage("Server", "Enter old password"));
                            Object oldPass = objectInputStream.readObject();
                            if(!(oldPass instanceof String))
                            {
                                if(dev) System.out.println("Server : GuestHandler : Received object is not of type String");
                                core.logout(username);
                                cutoff();
                                return;
                            }
                            if(!UsernamePasswordPairs.isPasswordCorrect(username, (String)oldPass))
                            {
                                objectOutputStream.writeObject(new StatusMessage("Server", "Password incorrect"));
                            }
                            else
                            {
                                objectOutputStream.writeObject(new StatusMessage("Server", "Enter new password"));
                                Object newPass1 = objectInputStream.readObject();
                                if(!(newPass1 instanceof String))
                                {
                                    if(dev) System.out.println("Server : GuestHandler : Received object is not of type String");
                                    core.logout(username);
                                    cutoff();
                                    return;
                                }
                                objectOutputStream.writeObject(new StatusMessage("Server", "Confirm new password"));
                                Object newPass2 = objectInputStream.readObject();
                                if(!(newPass2 instanceof String))
                                {
                                    if(dev) System.out.println("Server : GuestHandler : Received object is not of type String");
                                    core.logout(username);
                                    cutoff();
                                    return;
                                }
                                if(!((String)newPass1).equals((String)newPass2))
                                {
                                    if(dev) System.out.println("Server : GuestHandler : Password didn't match while resetting password for user '"+username+"'");
                                    objectOutputStream.writeObject(new StatusMessage("Server", "PASSWORDS DID NOT MATCH"));
                                }
                                else
                                {
                                    UsernamePasswordPairs.updatePassword(username, (String)oldPass, (String)newPass2);
                                    if(dev) System.out.println("Server : GuestHandler : User '"+username+"' reset their password.");
                                    objectOutputStream.writeObject(new StatusMessage("Server", "PASSWORD RESET SUCCESSFUL"));
                                }
                            }
                            break;
                    }
                }
            }
        }
        catch (NotSerializableException e)
        {
            System.out.println("SENDING FAILED : Invalid object");
            if(dev) {
                System.out.println("Server : GuestHandler : NonSerialized objects cannot be sent");
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            System.out.println("Unexpected error occurred");
            if(dev) e.printStackTrace();
        }
        finally {
            cutoff();
        }
    }

    private boolean isLoginValid(LoginMessage loginMessage)
    {
        //implement database query
        return UsernamePasswordPairs.isPasswordCorrect(loginMessage.getUserName(), loginMessage.getPassword());
    }

    private void cutoff()
    {
        try
        {
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
            if(dev) System.out.println("Guest kicked out");
        }
        catch (Exception e)
        {
            System.out.println("Guest cannot be disconnected");
            if(dev) e.printStackTrace();
        }
    }
}
