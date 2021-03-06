package net.serichat;

import net.tomp2p.peers.PeerAddress;

import java.io.*;
import java.security.PublicKey;

/**
 * Created by bilalkais on 5/19/16.
 */
public class GroupInfo implements Serializable {
    public PeerAddress ownerAddress;
    public PublicKey ownerPublicKey;

    public GroupInfo(PeerAddress ownerAddress, PublicKey ownerPublicKey) {
        this.ownerAddress = ownerAddress;
        this.ownerPublicKey = ownerPublicKey;
    }

    public GroupInfo(byte[] serializedGroupKey) {
        try {
            GroupInfo groupInfo = (GroupInfo)(deserialize(serializedGroupKey));
            this.ownerAddress = groupInfo.getOwnerAddress();
            this.ownerPublicKey = groupInfo.getOwnerPublicKey();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PeerAddress getOwnerAddress() {
        return ownerAddress;
    }

    public PublicKey getOwnerPublicKey() {
        return ownerPublicKey;
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
}
