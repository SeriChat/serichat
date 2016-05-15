package net.serichat;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import javax.crypto.SecretKey;

/**
 * Created by khaledsaied on 27/04/16.
 */

public class Group {

    private String groupName;
    private Number160 groupId;
    private SecretKey grpAESKey;
    private String password;
    private String groupKey;
    private Peer rightChild;
    private Peer leftChild;
    private Role role;
    private PeerAddress rootAddress;
    private PeerAddress ownerAddreess;

    public Group(Role role, String groupName, Number160 groupId, SecretKey grpAESKey, PeerAddress rootAddress , String password ) {
        this.role = role;
        this.groupName = groupName;
        this.groupId = groupId;
        this.grpAESKey = grpAESKey;
        this.rootAddress = rootAddress;
        this.password = password;
    }

    public Group(Role role, String groupName, Number160 groupId, SecretKey grpAESKey, PeerAddress ownerAddreess, String ownerNickName, String password ) {
        this.role = role;
        this.groupName = groupName;
        this.groupId = groupId;
        this.grpAESKey = grpAESKey;
        this.ownerAddreess = ownerAddreess;
        this.password = password;
    }

    public Group () {

    }

    public PeerAddress getRootAddress() {
        return rootAddress;
    }

    public void setRootAddress(PeerAddress rootAddress) {
        this.rootAddress = rootAddress;
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

    public String getGroupName() {
        return groupName;
    }

    public String getPassword() {
        return password;
    }

    public SecretKey getGrpAESKey() {
        return grpAESKey;
    }
}
