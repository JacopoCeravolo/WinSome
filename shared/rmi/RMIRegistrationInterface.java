package shared.rmi; 

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIRegistrationInterface  extends Remote {
    
    public void registerUser(String username, String password, List<String> tags) 
        throws RemoteException;

    public void registerForCallaback(String client, FollowersUpdateInterface clientInterface)
        throws RemoteException;

    public void unregisterForCallback(String client, FollowersUpdateInterface clientInterface)
        throws RemoteException;

    public List<String> pullUpdates(String client) throws RemoteException;

}