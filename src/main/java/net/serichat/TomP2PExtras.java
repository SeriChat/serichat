package net.serichat;

import net.tomp2p.dht.FutureDHT;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.io.IOException;

/**
 * Created by bilalkais on 5/12/16.
 */
public final class TomP2PExtras {

    private TomP2PExtras() {
    }

    /**
     * Finds a reference and returns it.
     *
     * @param peerDHT
     *            The peer that searches for the reference.
     * @param key
     *            The key to search.
     * @return The reference to the keyword or null.
     * @throws ClassNotFoundException .
     * @throws IOException .
     */
    public static PeerAddress findData(final Number160 key, final PeerDHT peerDHT) throws ClassNotFoundException,
            IOException {
        FutureGet futureGet = peerDHT.get(key).start();
        futureGet.awaitUninterruptibly();
        if(futureGet.data() == null) {
            return null;
        }
        return (PeerAddress)futureGet.rawData().keySet().toArray()[0];
    }

    private Object findPeer(final Number160 peerId, final PeerDHT peerDHT) throws ClassNotFoundException, IOException {
        FutureGet futureGet = peerDHT.get(peerId).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            PeerAddress peerAddress = (PeerAddress)futureGet.rawData().keySet().toArray()[0];
            if(peerAddress.peerId() == peerId) {
                return futureGet.rawData().keySet().toArray()[0];
            }
        }
        return null;
    }

}
