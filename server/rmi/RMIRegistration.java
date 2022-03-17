package server.rmi;

import shared.rmi.*;
import server.socialnetwork.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import server.socialnetwork.User;

public class RMIRegistration implements RMIRegistrationInterface {

    private ConcurrentHashMap<String, FollowersUpdateInterface> registeredClients;

    private ConcurrentHashMap<String, ArrayList<String>> pendingUpdates;
    
    private WinSomeNetwork network;

    public RMIRegistration(WinSomeNetwork network) {
        super();
        registeredClients = new ConcurrentHashMap<>();
        pendingUpdates = new ConcurrentHashMap<>();
        this.network = network;
    }

    @Override
    public void registerUser(String username, String password, List<String> tags) throws RemoteException {
        
        if (tags.size() > 5) throw new RemoteException("max 5 tags allowed");

        User newUser = new User(username, password, tags);

        synchronized (network) {

            if (network.getUsersMap().containsKey(username)) {
                throw new RemoteException("username already taken");
            }
    
            network.getUsersMap().put(username, newUser);
    
            for (String tag : tags) {
                network.getTagsMap().putIfAbsent(tag, new ArrayList<>());
                network.getTagsMap().get(tag).add(username);
            }
        }

        System.out.println("[REGISTRATION] user "+username+" registered");
    }

    @Override
    public synchronized void registerForCallaback(String client, FollowersUpdateInterface clientInterface) 
        throws RemoteException {
        
        if (!registeredClients.containsKey(client)) {
            registeredClients.put(client, clientInterface);
            System.out.println("[CALLBACK] client " +client+ " registered for callback");
        } 
    }

    @Override
    public synchronized void unregisterForCallback(String client, FollowersUpdateInterface clientInterface) 
        throws RemoteException {
        
        if (registeredClients.containsKey(client)) {
            registeredClients.remove(client);
            System.out.println("[CALLBACK] client " +client+ " unregistered");
        } else {
            System.out.println("[CALLBACK] client " +client+ " not found");
        }
    }

    public void followerUpdate(String client, String update) throws RemoteException {
        System.out.println("[CALLBACK] notifying client " +client+ " of update : " + update);
        
        if (registeredClients.containsKey(client)) { // Client is online
            registeredClients.get(client).notifyEvent(update);
            System.out.println("[CALLBACK] client " +client+ " notified");
        } else {

            pendingUpdates.putIfAbsent(client, new ArrayList<>());
            pendingUpdates.get(client).add(update);

            System.out.println("[CALLBACK] client offline, added pending update " + update);
        }
    }

    public List<String> pullUpdates(String client) throws RemoteException {

        List<String> newFollowers = new ArrayList<>();

        if (pendingUpdates.containsKey(client)) {

            Iterator<String> newUpdates = pendingUpdates.get(client).iterator();
            
            while (newUpdates.hasNext()) {
                String[] tokens = newUpdates.next().split(":");
                
                if (tokens[0].equals("add")) {
                    newFollowers.add(tokens[1]);
                }
            }
            System.out.println("[CALLBACK] client "+client+" pulled recent updates");
        }

        return newFollowers;
    }
}
