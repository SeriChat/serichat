package net.serichat;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class User {

    private Map<Number160,Group> groups;
    private Map<Number160,Admin> admins;
    private Map<Number160,Root> roots;

    public User() {
        groups = new HashMap<Number160, Group>();
        admins = new HashMap<Number160, Admin>();
        roots = new HashMap<Number160, Root>();
    }
    //test
    public void join(Peer myPeer, Number160 groupId, String password) {
        //Look root up and contact him...
        FutureDHT futureDHT = myPeer.get(groupId).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
             //futureDHT.getData().getObject().toString();
        }

        groups.put(groupId, new Group(groupId));
        roots.put(groupId, new Root("",0));
    }

    public void leave() {

    }

   /* public void createGroup(String groupName, Number160 ownerPeerId, String password) {
        net.serichat.Group group = new net.serichat.Group(groupName, ownerPeerId, password);
        groups.put(ownerPeerId, group);

        net.serichat.Admin admin = new net.serichat.Admin(ownerPeerId, password);
        admins.put(ownerPeerId, admin);

        //diffie-hellman
        //KeyAgreement keyAgreement;
        //keyAgreement.getAlgorithm("Diffie-Hellman");

    }
*/
    public void sendMsg(String msg, int groupId) {
        if(!admins.containsKey(groupId)) {

        }
        else {

        }
    }

    public void stabilization() {

    }

    public void forwardMsg() {

    }

    public void changeRole() {

    }

    public void handleLeave() {

    }

    public void handleKick() {

    }

}
