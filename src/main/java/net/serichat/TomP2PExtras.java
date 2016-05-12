package net.serichat;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

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
     * @param peer
     *            The peer that searches for the reference.
     * @param key
     *            The key to search.
     * @return The reference to the keyword or null.
     * @throws ClassNotFoundException .
     * @throws IOException .
     */
    public static Number160 findReference(final Peer peer, final Number160 key) throws ClassNotFoundException,
            IOException {
        FutureDHT futureDHT = peer.get(key).start();
        futureDHT.awaitUninterruptibly();
        if(futureDHT.getData() == null) {
            return null;
        }
        Number160 termKey = (Number160) futureDHT.getData().getObject();
        return termKey;
    }

    private Object get(String name, Peer peer) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            return futureDHT.getRawData().keySet().toArray()[0];
        }
        return null;
    }

}
