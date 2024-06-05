package Utilities;

public class AlreadyLoggedInException extends Exception{
    public AlreadyLoggedInException(String msg){
        super (msg);
    }
}
