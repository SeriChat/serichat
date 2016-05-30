package net.serichat; /**
 * Created by khaledsaied on 20/04/16.
 */
import java.io.Console;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Random;
import java.util.Scanner;

import net.tomp2p.connection.*;
import net.tomp2p.dht.*;
import net.tomp2p.futures.*;
import net.tomp2p.message.CountConnectionOutboundHandler;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static private Peer peer;
    static private PeerDHT peerDHT;
    static private SeriChat seriChat;
    static private KeyPair keyPair;
    static private Number160 peer1Owner;
    static private int loadCounter;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final CountConnectionOutboundHandler ccohTCP = new CountConnectionOutboundHandler();
    private static final CountConnectionOutboundHandler ccohUDP = new CountConnectionOutboundHandler();

    public static void initSeri(boolean isBootstrapper) throws Exception {

        System.out.print("\n");
        System.out.println("  _____           _  _____ _           _");
        System.out.println(" / ____|         (_)/ ____| |         | |");
        System.out.println("| (___   ___ _ __ _| |    | |__   __ _| |_");
        System.out.println(" \\___ \\ / _ \\ '__| | |    | '_ \\ / _` | __|");
        System.out.println(" ____) |  __/ |  | | |____| | | | (_| | |_");
        System.out.println("|_____/ \\___|_|  |_|\\_____|_| |_|\\__,_|\\__|");
        System.out.print("\n\n");

        loadCounter = 0;
        class MonitorStorage extends StorageMemory {
            public Data put(Number640 key, Data value)
            {
                loadCounter++;
                //System.out.println("load=" + loadCounter);
                return super.put(key, value);
            }
            public Data get(Number640 key) {
                loadCounter++;
                //System.out.println("load=" + loadCounter);
                return super.get(key);
            }
        }

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(1024);
        keyPair = gen.generateKeyPair();

        Bindings b = new Bindings().addAddress(InetAddress.getByName("localhost"));

        int peerId = 1;
        ServerSocket s = new ServerSocket(0);
        int port = 0;
        if (isBootstrapper) {
            port = 46459;

        } else {
            peerId = new Random().nextInt(1000);
            port = 46459 + peerId;
        }
        peer = new PeerBuilder(Number160.createHash(peerId))/*.keyPair(keyPair)*/.ports(port).bindings(b).start();

        InetAddress masterAddr = Inet4Address.getByName("localhost");
        int masterPort = 46459;

        FutureDiscover futureDiscover = peer.discover().expectManualForwarding().inetAddress(masterAddr).ports(masterPort).start();
        futureDiscover.awaitUninterruptibly();

        FutureBootstrap futureBootstrap = peer.bootstrap().inetAddress(masterAddr).ports(masterPort).start();
        futureBootstrap.awaitUninterruptibly();

        peerDHT = new PeerBuilderDHT(peer).storage(new MonitorStorage()).start();

        peerDHT.storageLayer().protection(StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY);

        FutureChannelCreator fcc = peer.connectionBean().reservation().create(1, 1);
        fcc.awaitUninterruptibly();

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(final PeerAddress sender, final Object request) {

                LOG.debug("Replying...");

                loadCounter++;
                //System.out.println("load=" + loadCounter);

                try {
                    SeriEvent seriEvent = new SeriEvent((byte[]) request);
                    return seriChat.handleEvent(seriEvent, sender, peerDHT);
                } catch (Exception ex) {
                    LOG.debug(request.toString());
                    return null;
                }

            }
        });

    }

    public static void main(String[] arg) throws NumberFormatException, Exception {
        Console console = System.console();
        if (arg.length == 0) {
            initSeri(true);
        }
        else if(arg.length == 1) {
            initSeri(false);
            seriChat = new SeriChat(arg[0], keyPair);
        }
        else if (arg.length == 4 && arg[1].equals("-j")) {
            initSeri(false);
            seriChat = new SeriChat(arg[0], keyPair);
            seriChat.joinGroup(arg[2], arg[3], peerDHT);
            while(true) {

                seriChat.sendMsg(arg[2], peerDHT, console.readLine());
            }
        }

        else if (arg.length == 4 && arg[1].equals("-c")) {
            initSeri(false);
            seriChat = new SeriChat(arg[0], keyPair);
            seriChat.createGroup(arg[2], arg[3], peerDHT);
            int count = 0;
            while(true) {
                seriChat.sendMsg(arg[2], peerDHT, console.readLine());
            }
        }
        else {
            System.out.println("Missing parameter!");
            System.out.println("Seri is shutting down...");
        }



    }
}