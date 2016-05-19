package net.serichat; /**
 * Created by khaledsaied on 20/04/16.
 */
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.corba.se.impl.orbutil.closure.Future;
import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import net.tomp2p.connection.*;
import net.tomp2p.dht.*;
import net.tomp2p.futures.*;
import net.tomp2p.message.CountConnectionOutboundHandler;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.builder.SendDirectBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;
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
    private static final CountConnectionOutboundHandler ccohTCP = new CountConnectionOutboundHandler();
    private static final CountConnectionOutboundHandler ccohUDP = new CountConnectionOutboundHandler();

    public Main(int peerId, String nickName) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(1024);
        keyPair = gen.generateKeyPair();

        seriChat = new SeriChat(nickName, keyPair);

        Bindings b = new Bindings().addAddress(InetAddress.getByName("localhost"));

        System.out.print(keyPair.getPublic().toString());
        ServerSocket s = new ServerSocket(0);
        int port = 0;
        if (peerId != 1) {
            port = s.getLocalPort();

        } else {
            port = 46459;
        }
        peer = new PeerBuilder(Number160.createHash(peerId))/*.keyPair(keyPair)*/.ports(port).bindings(b).start();

        InetAddress masterAddr = Inet4Address.getByName("localhost");
        int masterPort = 46459;

        FutureDiscover futureDiscover = peer.discover().expectManualForwarding().inetAddress(masterAddr).ports(masterPort).start();
        futureDiscover.awaitUninterruptibly();

        FutureBootstrap futureBootstrap = peer.bootstrap().inetAddress(masterAddr).ports(masterPort).start();
        futureBootstrap.awaitUninterruptibly();

        peerDHT = new PeerBuilderDHT(peer).start();

        peerDHT.storageLayer().protection(StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY);

        FutureChannelCreator fcc = peer.connectionBean().reservation().create(1, 1);
        fcc.awaitUninterruptibly();

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(final PeerAddress sender, final Object request) {

                LOG.info("Replying...");

                try {
                    SeriEvent seriEvent = new SeriEvent((byte[]) request);
                    LOG.info("YAAAAY, it is a Seri-event from " + seriEvent.getGroupName());
                    return seriChat.handleEvent(seriEvent, sender, peerDHT);
                } catch (Exception ex) {
                    LOG.info(request.toString());
                    return null;
                }

            }
        });

    }

    public static void main(String[] arg) throws NumberFormatException, Exception {
        Main dns = new Main(Integer.parseInt(arg[0]), arg[1]);
        if (arg.length == 4) {

        }
        if (arg.length == 3) {
            seriChat.createGroup("IHA", "123456", peerDHT);
        }
        if (arg.length == 2) {
            seriChat.join("IHA", "123456", peerDHT);
        }
    }
}