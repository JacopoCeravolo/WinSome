package client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.StringTokenizer;

import shared.rmi.FollowersUpdateInterface;

public class FollowersUpdate extends RemoteObject implements FollowersUpdateInterface {

    public FollowersUpdate() {
        super();
    }

    @Override
    public void notifyEvent(String update) throws RemoteException {

        //System.out.println(update);

        String DELIMITER = ":";
        StringTokenizer parseUpdate = new StringTokenizer(update, DELIMITER);
        
        String action = parseUpdate.nextToken();
        String user = parseUpdate.nextToken();

        if (parseUpdate.hasMoreTokens()) throw new RemoteException("invalid callback message");

        switch (action) {
            
            case "add": ClientMain.followersList.add(user); break;
            case "remove": ClientMain.followersList.remove(user); break;
            default: throw new RemoteException("invalid callback message"); 

        }
    }
}
