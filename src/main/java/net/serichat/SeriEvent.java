package net.serichat;

import net.tomp2p.peers.PeerAddress;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.PublicKey;

/**
 * Created by bilalkais on 5/13/16.
 */
public class SeriEvent implements Serializable {

    private EventType type;
    private byte[] password;
    private String senderNickName;
    private String chatMsg;
    private String groupName;
    private String ownerNickName;
    private byte[] grpSecretKey;
    private PublicKey publicKey;
    private PeerAddress joinedPeer;


    public SeriEvent(EventType type, String groupName, String ownerNickName, byte[] password, byte[] grpSecretKey) {
        this.type = type;
        this.groupName = groupName;
        this.ownerNickName = ownerNickName;
        this.password = password;
        this.grpSecretKey = grpSecretKey;
    }

    public SeriEvent(EventType type, String groupName, byte[] password, String senderNickName, PublicKey publicKey) {
        this.type = type;
        this.groupName = groupName;
        this.password = password;
        this.senderNickName = senderNickName;
        this.publicKey = publicKey;
    }

    public SeriEvent(String groupName, String chatMsg, String senderNickName) {
        this.groupName = groupName;
        this.chatMsg = chatMsg;
        this.senderNickName = senderNickName;
    }

    public SeriEvent(EventType type, String groupName, PeerAddress joinedPeer) {
        this.type = type;
        this.joinedPeer = joinedPeer;
    }

    public SeriEvent(byte[] serializedEvent) {
        try {
            SeriEvent seriEvent = (SeriEvent)(deserialize(serializedEvent));
            this.type = seriEvent.getType();
            this.password = seriEvent.getPassword();
            this.senderNickName = seriEvent.getSenderNickName();
            this.chatMsg = getChatMsg();
            this.groupName = seriEvent.getGroupName();
            this.ownerNickName = seriEvent.getOwnerNickName();
            this.grpSecretKey = seriEvent.getGrpSecretKey();
            this.publicKey = seriEvent.getPublicKey();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public SeriEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public byte[] getPassword() {
        return password;
    }

    public String getSenderNickName() {
        return senderNickName;
    }

    public String getChatMsg() {
        return chatMsg;
    }

    public String getGroupName() {
        return groupName;
    }

    public byte[] getGrpSecretKey() {
        return grpSecretKey;
    }

    public String getOwnerNickName() {
        return ownerNickName;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        return out.toByteArray();
    }

    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PeerAddress getJoinedPeer() {
        return joinedPeer;
    }
}
