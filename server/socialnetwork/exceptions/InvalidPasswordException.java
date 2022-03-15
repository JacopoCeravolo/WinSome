package server.socialnetwork.exceptions;

public class InvalidPasswordException extends Exception{
    
    public InvalidPasswordException() {
        super();
    }

    public InvalidPasswordException(String m) {
        super(m);
    }
}
