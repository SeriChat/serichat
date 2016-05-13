package net.serichat;

import net.tomp2p.peers.PeerAddress;

/**
 * Created by bilalkais on 5/13/16.
 */
public class SeriEvent {
    EventType eventType;
    String password;
    String senderNickName;
    String chatMsg;
    String groupName;

    public SeriEvent(EventType eventType, String password, String senderNickName) {
        this.eventType = eventType;
        this.password = password;
        this.senderNickName = senderNickName;
    }

    public SeriEvent(String groupName, String chatMsg) {
        this.groupName = groupName;
        this.chatMsg = chatMsg;
    }
}
