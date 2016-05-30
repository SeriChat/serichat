package net.serichat;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePeerConnection;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class SeriChat {

    private static final Logger LOG = LoggerFactory.getLogger(SeriChat.class);
    private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
    private String nickName;
    private Map<String,Group> groups;
    private KeyPair keyPair;
    private Direction direction;
    private SimpleDateFormat timeStampFormat;


    public SeriChat(String nickName, KeyPair keyPair) {
        this.nickName = nickName;
        this.groups = new HashMap<String, Group>();
        this.keyPair = keyPair;
        this.direction = Direction.RGIHT;
        timeStampFormat = new SimpleDateFormat("[HH:mm:ss:SSS]");
    }

    public void createGroup(String groupName, String password, PeerDHT ownerPeerDHT) {
        Number160 groupId = Number160.createHash(groupName);
        try {
            if (TomP2PExtras.findData(groupId, ownerPeerDHT) == null) {
                ownerPeerDHT.put(groupId).data( new Data(new GroupInfo(ownerPeerDHT.peerAddress(), keyPair.getPublic()))/*.protectEntry(keyPair)).sign(*/).start().awaitUninterruptibly();

                SecretKey grpAESKey = KeyGenerator.getInstance("AES").generateKey();
                groups.put(groupName, new Group(Role.OWNER, groupName, groupId, grpAESKey, password));

                System.out.println("Welcome to " + groupName + " chat-group");
                System.out.println("Your are the boss ~");
            }
            else {
                System.out.println(groupName + " chat-group allready exists!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public void joinGroup(String groupName, String password, PeerDHT joiningPeerDHT) {
        LOG.debug("Joining: " + groupName);
        Number160 groupId = Number160.createHash(groupName);
        try {
            FutureGet futureGet = null;
            int attempts = 0;
            do {
                futureGet = joiningPeerDHT.get(groupId).start();
                futureGet.awaitUninterruptibly();
                attempts++;
            } while (futureGet.data() == null && attempts < MAX_NUMBER_OF_ATTEMPTS);
            if (futureGet.data() != null) {
                LOG.debug("joiningPeerDHT got: " + futureGet.data());
                PeerAddress groupKeyHolderAddr = (PeerAddress) futureGet.rawData().keySet().toArray()[0];
                GroupInfo groupInfo = (GroupInfo) futureGet.data().object();
                LOG.debug("Owner for " + groupName + "'s chat group has this addr: " + groupInfo.getOwnerAddress().peerSocketAddress());
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, groupInfo.getOwnerPublicKey());
                byte[] cipheredPassword = cipher.doFinal(password.getBytes());
                SeriEvent joinEvent = new SeriEvent(EventType.JOIN, groupName, cipheredPassword, nickName, keyPair.getPublic());
                FuturePeerConnection pcOwner = joiningPeerDHT.peer().createPeerConnection(groupInfo.getOwnerAddress());
                FutureResponse futureResponse = joiningPeerDHT.peer().sendDirect(pcOwner).object(joinEvent.serialize()).start().futureResponse();
                futureResponse.awaitUninterruptibly();
                if (futureResponse.isSuccess()) {
                    byte[] response = (byte[]) futureResponse.responseMessage().buffer(0).object();
                    LOG.debug("grpSecretKey is received");
                    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                    byte[] grpSecretKeyBytes = cipher.doFinal(response);
                    SecretKey grpSecretKey = new SecretKeySpec(grpSecretKeyBytes, 0, grpSecretKeyBytes.length, "AES");
                    Group joinedGroup = new Group(Role.MEMBER, groupName, groupId, grpSecretKey, groupKeyHolderAddr, password, groupInfo.getOwnerAddress());
                    groups.put(groupName, joinedGroup);
                    System.out.println("Welcome to " + groupName + " chat-group");
                }
                else {
                    System.out.println("Failed to joinGroup " + groupName + " chat-group");
                }
            }
            else {
                System.out.println(groupName + " chat-group does not exist!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public void leaveGroup() {

    }

    public void sendMsg(String groupName, PeerDHT sendingPeerDHT, String msg) {

        Group group = groups.get(groupName);
        if(group != null){
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, group.getGrpAESKey());
                byte[] msgBytes = cipher.doFinal(msg.getBytes());
                SeriEvent seriEvent = new SeriEvent(EventType.FORWARD_MESSAGE, groupName, msgBytes, nickName);

                if (group.getRole() != Role.OWNER) {
                    PeerAddress root = group.getRootAddress();
                    sendingPeerDHT.peer().sendDirect(root).object(seriEvent.serialize()).start().awaitUninterruptibly();
                }
                else {
                    System.out.println(timeStampFormat.format(Calendar.getInstance().getTime()) + " "
                            + nickName + ": " + msg);
                    if (group.getRightChild() != null) {
                        sendingPeerDHT.peer().sendDirect(group.getRightChild()).object(seriEvent.serialize()).start().awaitUninterruptibly();
                    }
                    if (group.getLeftChild() != null) {
                        sendingPeerDHT.peer().sendDirect(group.getLeftChild()).object(seriEvent.serialize()).start().awaitUninterruptibly();
                    }
                }


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
        }
    }

    public void stabilization() {

    }

    public Object handleEvent(SeriEvent event, PeerAddress sender, PeerDHT receiverPeerDHT) {
        Cipher cipher = null;
        Group group = groups.get(event.getGroupName());
        LOG.debug("handling Seri Event...");
        switch (event.getType()) {
            case JOIN:
                try {
                    if (group != null && (group.getRole() == Role.OWNER)) {
                        cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                        String password = new String(cipher.doFinal(event.getPassword()));
                        LOG.debug("Handling joinGroup event: Group name=" + group.getGroupName() + " pass=" + password);
                        if (group.getPassword().equals(password)) {
                            cipher.init(Cipher.ENCRYPT_MODE, event.getPublicKey());
                            if(!group.setChild(sender)) {
                                SeriEvent forwardJoinEvent = new SeriEvent(EventType.FORWARD_JOIN, group.getGroupName(), sender);
                                if(direction == Direction.RGIHT) {
                                    LOG.debug("Forwarding peer(" + sender.peerId() + ") to the right child(" + group.getRightChild().peerId() + ")");
                                    receiverPeerDHT.peer().sendDirect(group.getRightChild()).object(forwardJoinEvent.serialize())
                                            .start();//.awaitUninterruptibly();
                                    direction = Direction.LEFT;
                                }
                                else if (direction == Direction.LEFT){
                                    LOG.debug("Forwarding peer(" + sender.peerId() + ") to the left child(" + group.getLeftChild().peerId() + ")");
                                    receiverPeerDHT.peer().sendDirect(group.getLeftChild()).object(forwardJoinEvent.serialize())
                                            .start();//.awaitUninterruptibly();
                                    direction = Direction.RGIHT;
                                }
                            }
                            return cipher.doFinal(group.getGrpAESKey().getEncoded());
                        }
                        return "";
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            case FORWARD_JOIN:
                if(!group.setChild(event.getJoinedPeer())) {
                    try {
                        if(direction == Direction.RGIHT) {
                            LOG.debug("Forwarding peer(" + event.getJoinedPeer().peerId() + ") to the right");
                                receiverPeerDHT.peer().sendDirect(group.getRightChild()).object(event.serialize())
                                        .start();//.awaitUninterruptibly();
                            direction = Direction.LEFT;
                        }
                        else if (direction == Direction.LEFT){
                            LOG.debug("Forwarding peer(" + event.getJoinedPeer().peerId() + ") to the left");
                            receiverPeerDHT.peer().sendDirect(group.getLeftChild()).object(event.serialize())
                                    .start();//.awaitUninterruptibly();
                            direction = Direction.RGIHT;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    LOG.debug("Peer (" + event.getJoinedPeer().peerId() + ") is now my child");
                }
                return "";

            case FORWARD_MESSAGE:
                if(group != null) {
                    byte[] enccryptedMsg = event.getChatMsg();
                    try {
                        cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.DECRYPT_MODE, group.getGrpAESKey());

                        String theMsg = new String(cipher.doFinal(enccryptedMsg));
                        System.out.println(timeStampFormat.format(Calendar.getInstance().getTime()) + " "
                                + event.getSenderNickName() + ": " + theMsg);

                        if(group.getRightChild() != null) {
                            receiverPeerDHT.peer().sendDirect(group.getRightChild()).object(event.serialize()).start();//.awaitUninterruptibly();
                        }
                        if(group.getLeftChild() != null) {
                            receiverPeerDHT.peer().sendDirect(group.getLeftChild()).object(event.serialize()).start();//.awaitUninterruptibly();
                        }

                        return "";

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            default:
                return "";
        }
    }

    public Map<String, Group> getGroups() {
        return groups;
    }
}
