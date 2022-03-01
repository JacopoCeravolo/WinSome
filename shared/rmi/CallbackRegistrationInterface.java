package shared.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackRegistrationInterface extends Remote {
    
    public void registerForCallaback(String client, FollowersUpdateInterface clientInterface)
        throws RemoteException;

    public void unregisterForCallback(String client, FollowersUpdateInterface clientInterface)
        throws RemoteException;
}
