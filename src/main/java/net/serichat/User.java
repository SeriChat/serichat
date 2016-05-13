package net.serichat;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class User {

    String nickName;
    private Map<String,Group> groups;
    //private Map<Number160,Admin> admins;
    //private Map<Number160,Root> roots;

    public User(String nickName) {
        this.nickName = nickName;
        groups = new HashMap<String, Group>();
    }
    //test
    public void join(String groupName, String password, Peer joiningPeer) {
        Number160 groupId = Number160.createHash(groupName);
        try {
            PeerAddress rootAddress = TomP2PExtras.findReference(groupId, joiningPeer);
            if (rootAddress != null) {
                joiningPeer.sendDirect(rootAddress).setObject("").start().awaitUninterruptibly();
            }
            else {
                System.out.println("Group does not exist!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //groups.put(groupId, new Group(groupId));
        //roots.put(groupId, new Root("",0));
    }

    public void leave() {

    }

    public void createGroup(String groupName, Peer ownerPeer, String password) {
        Number160 groupId = Number160.createHash(groupName);
        try {
            if (TomP2PExtras.findReference(groupId, ownerPeer) == null) {
                Group group = new Group(groupName, groupId, ownerPeer, password);
                groups.put(groupName, group);
                ownerPeer.put(groupId).setData(new Data(groupName)).start().awaitUninterruptibly();
            }
            else {
                System.out.println("Group name allready exists!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg, String groupName) {
       // SeriEvent message = new SeriEvent();
        //message.= msg;
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
