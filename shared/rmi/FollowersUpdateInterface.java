package shared.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FollowersUpdateInterface extends Remote {
    
    public void notifyEvent(String update) throws RemoteException;
}
