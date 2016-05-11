package net.serichat;

import net.tomp2p.peers.Number160;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class Admin {
    private Number160 groupId;
    private String groupPassword;

    public Admin(Number160 groupId, String groupPassword) {
        this.groupId = groupId;
        this.groupPassword = groupPassword;
    }

    public void kickUser() {

    }

    public void setModerator() {

    }

    public void handleJoin() {

    }
}
