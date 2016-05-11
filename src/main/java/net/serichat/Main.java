package net.serichat; /**
 * Created by khaledsaied on 20/04/16.
 */
import java.io.IOException;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.rpc.RawDataReply;
import net.tomp2p.storage.Data;

public class Main {

    final private Peer peer;

    public Main(int peerId) throws Exception {
        peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000 + peerId).makeAndListen();
        FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(4001).start();
        fb.awaitUninterruptibly();
        if (fb.getBootstrapTo() != null) {
            peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        peer.setRawDataReply(new RawDataReply() {
            public org.jboss.netty.buffer.ChannelBuffer reply(PeerAddress sender, org.jboss.netty.buffer.ChannelBuffer requestBuffer) throws Exception {
                //if(!peer.getPeerID().toString().equals(sender.getID().toString())) { // Workaround!!
                    System.out.println("RawData says Msg: " + requestBuffer.toString() + ", from: " + sender.getID());
                //}
                return requestBuffer;
            }
        });

        peer.setObjectDataReply(new ObjectDataReply() {

            public Object reply(final PeerAddress sender, final Object request) throws Exception {
                if(!peer.getPeerID().toString().equals(sender.getID().toString())) { // Workaround!!
                    System.out.println("ObjectData says Msg: " + request.toString() + ", from: " + sender.getID());
                }
                return "world!";
            }
        });

    }

    public static void main(String[] arg) throws NumberFormatException, Exception {
        Main dns = new Main(Integer.parseInt(arg[0]));
        if (arg.length == 3) {
            dns.store(arg[1], arg[2]);
        }
        if (arg.length == 2) {
            //System.out.println("Key: " + arg[1] + " is at the node with this ID: " + dns.get(arg[1]));
            dns.peer.send(Number160.createHash(arg[1])).setObject("Hello").start();
        }
    }

    private String get(String name) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            return futureDHT.getData().getPeerId().toString();
        }
        return "not found";
    }

    private void store(String name, String ip) throws IOException {
        peer.put(Number160.createHash(name)).setData(new Data(ip)).start().awaitUninterruptibly();
    }
}
/*
public class MyStorageMemory extends StorageMemory {
    public Collection<Number160> put(Number160 locationKey, Number160 domainKey, PublicKey publicKey,
                                     Map<Number160, Data> contentMap, boolean putIfAbsent, boolean domainProtection)
    {
        if (true) {
            //here we could get an int from the data and sum it up
        } else {
            super.put()
            super.put(locationKey, domainKey, null, publicKey, putIfAbsent, domainProtection);
        }
    }
}*/