package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import shared.rmi.FollowersUpdateInterface;

public class FollowersUpdate implements FollowersUpdateInterface {

    ArrayList<String> followersList;

    public FollowersUpdate(ArrayList<String> followersList) {
        super();
        this.followersList = followersList;
    }

    @Override
    public void notifyEvent(String update) throws RemoteException {

        String DELIMITER = ":";
        StringTokenizer parseUpdate = new StringTokenizer(update, DELIMITER);
        
        String action = parseUpdate.nextToken();
        String user = parseUpdate.nextToken();

        if (parseUpdate.hasMoreTokens()) throw new RemoteException("invalid callback message");

        switch (action) {
            
            case "add": {
                if (!followersList.contains(user)) {
                    followersList.add(user);
                } 

                break;
            }
        
            case "remove": {
                if (followersList.contains(user)) {
                    followersList.remove(user);
                }

                break;
            }

            default: throw new RemoteException("invalid callback message");
        }
    }
}
