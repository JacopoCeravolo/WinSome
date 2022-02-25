package shared.rmi; 

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIRegistrationInterface  extends Remote {
    
    public void registerUser(String username, String password, List<String> tags) 
        throws RemoteException;


}