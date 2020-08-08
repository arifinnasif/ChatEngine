public class CustomException extends Exception {
    private String details;

    public CustomException()
    {
        this.details="";
    }

    public CustomException(String details)
    {
        this.details=details;
    }

    @Override
    public String toString() {
        return "CustomException : "+details;
    }
}
