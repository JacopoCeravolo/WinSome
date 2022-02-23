import java.rmi.RemoteException;
import java.util.List;

public class RMIRegistration implements RMIRegistrationInterface {

    WinSomeNetwork network;

    public RMIRegistration(WinSomeNetwork network) {
        this.network = network;
    }

    @Override
    public void registerUser(String username, String password, List<String> tags) throws RemoteException {
        
        if (tags.size() > 5) throw new RemoteException("max 5 tags allowed");

        WinSomeUser newUser = new WinSomeUser(username, password, tags);

        for (String tag : tags) {
            network.addUserToTagList(newUser, tag);
        }

        if (network.getUsersMap().containsKey(username)) {
            throw new RemoteException("username already taken");
        }

        network.getUsersMap().put(username, newUser);
        
    }
    
}
