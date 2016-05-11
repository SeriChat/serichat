package net.serichat;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class Group {

    private Number160 groupId;
    private String groupKey;
    private Peer rightChild;
    private Peer leftChild;

    public Group(Number160 groupId) {
        this.groupId = groupId;
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
