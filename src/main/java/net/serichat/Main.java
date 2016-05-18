package net.serichat; /**
 * Created by khaledsaied on 20/04/16.
 */
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import com.sun.corba.se.impl.orbutil.closure.Future;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.*;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.builder.SendDirectBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static private Peer peer;
    static private PeerDHT peerDHT;
    static private SeriChat seriChat;
    static private KeyPair keyPair;
    static private Number160 peer1Owner;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public Main(int peerId, String nickName) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(1024);
        keyPair = gen.generateKeyPair();

        seriChat = new SeriChat(nickName, keyPair);

        Bindings b = new Bindings().listenAny();

        System.out.print(keyPair.getPublic().toString());
        peer = new PeerBuilder(Number160.createHash(peerId)).keyPair(keyPair).ports(4000 + peerId).bindings(b).start();

        InetAddress masterAddr = Inet4Address.getByName("localhost");
        int masterPort = 4001;

        FutureDiscover futureDiscover = peer.discover().expectManualForwarding().inetAddress(masterAddr).ports(masterPort).start();
        futureDiscover.awaitUninterruptibly();

        FutureBootstrap futureBootstrap = peer.bootstrap().inetAddress(masterAddr).ports(masterPort).start();
        futureBootstrap.awaitUninterruptibly();

        peerDHT = new PeerBuilderDHT(peer).start();

        peerDHT.storageLayer().protection(StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY);

        /*System.out.println("peer[0] knows: " + peer.peerBean().peerMap().all() + " unverified: "
                + peer.peerBean().peerMap().allOverflow());
        System.out.println("wait for maintenance ping");
        Thread.sleep(2000);
        System.out.println("peer[0] knows: " + peer.peerBean().peerMap().all() + " unverified: "
                + peer.peerBean().peerMap().allOverflow());*/

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(final PeerAddress sender, final Object request) throws Exception {

                LOG.info("Replying...");

                SeriEvent seriEvent = new SeriEvent((byte[])request);

                LOG.info("YAAAAY, it is an event from " + seriEvent.getGroupName());

                return seriChat.handleEvent(seriEvent, sender, peerDHT);
            }
        });

    }

    public static void main(String[] arg) throws NumberFormatException, Exception {
        Main dns = new Main(Integer.parseInt(arg[0]), arg[1]);
        if (arg.length == 3) {
            seriChat.createGroup("IHA", "123456", peerDHT);
            //PeerAddress adr = (PeerAddress)dns.get(arg[1]);
            //System.out.print(adr.toString());
        }
        if (arg.length == 2) {
            seriChat.join("IHA", "123456", peerDHT);
            //dns.store(arg[1], "Second");
            //System.out.println("Key: " + arg[1] + " is at the node with this ID: " + dns.get(arg[1]).toString());
            //Thread.sleep(4000);
            //System.out.println("Key: " + arg[1] + " is at the node with this ID: " + dns.get(arg[1]).toString());
            //FutureDirect direct = dns.peerDHT.peer().sendDirect((PeerAddress)dns.get(arg[1])).object(new SeriEvent("Test","").serialize()).start();
            //direct.awaitUninterruptibly();
            //System.out.print( ((PublicKey)direct.futureResponse().responseMessage().buffer(0).object()).toString() );
        }
    }

    private Object get(String name) throws ClassNotFoundException, IOException {
        FutureGet futureGet = peerDHT.get(Number160.createHash(name)).start();
        futureGet.awaitUninterruptibly();
        //System.out.print(Number160.createHash(name));
        if (futureGet.isSuccess() && futureGet.data() != null) {
            System.out.print(futureGet.data().object().toString() + " " + futureGet.data().toString() + "\n"
            + futureGet.data().publicKey().toString());
            return futureGet.rawData().keySet().toArray()[0];
        }
        return null;
    }

    private void store(String name, String data) throws IOException, NoSuchAlgorithmException {
        System.out.print(Number160.createHash(name));
        FuturePut futurePut = peerDHT.put(Number160.createHash(name)).data(new Data(data)
                .protectEntry(keyPair)).sign()
                /*.setProtectDomain().setDomainKey(peer1Owner)*/.start().awaitUninterruptibly();
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