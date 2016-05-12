package net.serichat;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * Created by khaledsaied on 27/04/16.
 */

public class Group {

    private String groupName;
    private Number160 groupId;
    private Peer ownerPeer;
    private String password;
    private String groupKey;
    private Peer rightChild;
    private Peer leftChild;
    private Roles role;

    public Group(String groupName, Number160 groupId, Peer ownerPeer, String password ) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.ownerPeer = ownerPeer;
        this.password = password;
        role = Roles.OWNER;
    }

    public Group () {

    }

    public Number160 getGroupId() {
        return groupId;
    }

    public void setGroupId(Number160 groupId) {
        this.groupId = groupId;
    }

    public Peer getRightChild() {
        return rightChild;
    }

    public void setRightChild(Peer rightChild) {
        this.rightChild = rightChild;
    }

    public Peer getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Peer leftChild) {
        this.leftChild = leftChild;
    }
}
