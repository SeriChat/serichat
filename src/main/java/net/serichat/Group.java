package net.serichat;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

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
    private Role role;
    private PeerAddress root;

    public Group(String groupName, Number160 groupId, Peer ownerPeer, PeerAddress root , String password ) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.ownerPeer = ownerPeer;
        this.root = root;
        this.password = password;
        role = Role.OWNER;
    }

    public Group () {

    }

    public PeerAddress getRoot() {
        return root;
    }

    public void setRoot(PeerAddress root) {
        this.root = root;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
