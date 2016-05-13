package net.serichat;

import net.tomp2p.peers.PeerAddress;

/**
 * Created by bilalkais on 5/13/16.
 */
public class SeriEvent {
    EventType eventType;
    String password;
    String senderNickName;
    PeerAddress senderAddress;

    public SeriEvent(EventType eventType, String password, String senderNickName, PeerAddress senderAddress) {
        this.eventType = eventType;
        this.password = password;
        this.senderNickName = senderNickName;
        this.senderAddress = senderAddress;
    }
}
