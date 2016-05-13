package net.serichat; /**
 * Created by khaledsaied on 20/04/16.
 */
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Utils;

public class SeriChat {

    final private Peer peer;

    public SeriChat(int peerId) throws Exception {
        peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000 + peerId).makeAndListen();
        FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(4001).start();
        fb.awaitUninterruptibly();
        if (fb.getBootstrapTo() != null) {
            peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        peer.setObjectDataReply(new ObjectDataReply() {

            public Object reply(final PeerAddress sender, final Object request) throws Exception {
                //if(!peer.getPeerID().toString().equals(sender.getID().toString())) { // Workaround!!

                    //System.out.println("ObjectData says Msg: " + request.toString() + ", from: " + sender.getID());
                //}
                return "world!";
            }
        });

    }

    public static void main(String[] arg) throws NumberFormatException, Exception {
        SeriChat dns = new SeriChat(Integer.parseInt(arg[0]));
        if (arg.length == 3) {
            dns.store(arg[1], "First");
        }
        if (arg.length == 2) {
            //dns.store(arg[1], "Second");
            System.out.println("Key: " + arg[1] + " is at the node with this ID: " + dns.get(arg[1]).toString());
            //dns.peer.sendDirect((PeerAddress) dns.get(arg[1])).setObject("Hello").start();
        }
    }

    private Object get(String name) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            //return futureDHT.getRawData().keySet().toArray()[0];
            return futureDHT.getData().getObject();
        }
        return null;
    }

    private void store(String name, String data) throws IOException, NoSuchAlgorithmException {
        Data dataObj = new Data(data);
        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
        KeyPair keyPair = gen.generateKeyPair();

        final Number160 peer2Owner = Utils.makeSHAHash( keyPair.getPublic().getEncoded()); // results in 0
        FutureDHT futureDHT = peer.put(Number160.createHash(name)).setData(new Data(data))
                .setProtectDomain().setDomainKey(peer2Owner).start();
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
}

	private static void exampleSendOne(PeerDHT peer) {
		RequestP2PConfiguration requestP2PConfiguration = new RequestP2PConfiguration(1, 10, 0);
		FutureSend futureSend = peer.send(Number160.createHash("key")).object("hello")
		        .requestP2PConfiguration(requestP2PConfiguration).start();
		futureSend.awaitUninterruptibly();
		for (Object object : futureSend.rawDirectData2().values()) {
			System.err.println("got:" + object);
		}
	}

*/