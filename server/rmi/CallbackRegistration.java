package server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shared.rmi.CallbackRegistrationInterface;
import shared.rmi.FollowersUpdateInterface;

public class CallbackRegistration extends RemoteObject implements CallbackRegistrationInterface {

    HashMap<String, FollowersUpdateInterface> clients;
    
    public CallbackRegistration() {
        super();
        clients = new HashMap<>();
    }

    @Override
    public synchronized void registerForCallaback(String client, FollowersUpdateInterface clientInterface) 
        throws RemoteException {
        
        if (!clients.containsKey(client)) {
            clients.put(client, clientInterface);
        }

        System.out.println("New registration");
    }

    @Override
    public synchronized void unregisterForCallback(String client, FollowersUpdateInterface clientInterface) 
        throws RemoteException {
        
        if (clients.containsKey(client)) {
            clients.remove(client);
            System.out.println("Client unregistered");
        } else {
            System.out.println("Client not found");
        }
    }

    public void followerUpdate(String client, String update) throws RemoteException {
        clients.get(client).notifyEvent(update);
    }
}
