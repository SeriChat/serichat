package net.serichat;

import com.sun.corba.se.impl.orbutil.closure.Future;
import net.tomp2p.connection.PeerBean;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by khaledsaied on 27/04/16.
 */
public class SeriChat implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SeriChat.class);
    private String nickName;
    private Map<String,Group> groups;
    private KeyPair keyPair;


    public SeriChat(String nickName, KeyPair keyPair) {
        this.nickName = nickName;
        groups = new HashMap<String, Group>();
        this.keyPair = keyPair;
    }
    //test
    public void join(String groupName, String password, PeerDHT joiningPeerDHT) {
        Number160 groupId = Number160.createHash(groupName);
        try {
            FutureGet futureGet = joiningPeerDHT.get(groupId).start();
            futureGet.awaitUninterruptibly();
            PeerAddress rootAddress = (PeerAddress) futureGet.rawData().keySet().toArray()[0];
            PublicKey rootPublicKey = (PublicKey) futureGet.data().object();
            if (rootAddress != null) {
                LOG.debug("Root for " + groupName + "'s chat group: " + rootAddress.peerId() + " peerId");
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, rootPublicKey);
                byte[] cipheredPassword = cipher.doFinal(password.getBytes());
                SeriEvent joinEvent = new SeriEvent(EventType.JOIN, cipheredPassword, nickName, keyPair.getPublic());
                FutureResponse futureResponse = joiningPeerDHT.peer().sendDirect(rootAddress).object(joinEvent.serialize()).start().futureResponse();
                futureResponse.awaitUninterruptibly();
                byte[] response = (byte[]) futureResponse.responseMessage().buffer(0).object();
                if (response != null) {
                    LOG.debug("Public key is received");
                    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                    byte[] grpSecretKeyBytes = cipher.doFinal(response);
                    SecretKey grpSecretKey = new SecretKeySpec(grpSecretKeyBytes, 0, grpSecretKeyBytes.length, "AES");
                    Group joinedGroup = new Group(Role.MEMBER, groupName, groupId, grpSecretKey, rootAddress, password);
                    groups.put(groupName, joinedGroup);
                    LOG.info("You are now member of " + groupName + " chat-group");
                }
                else {
                    LOG.info("Failed to join the group!");
                }
            }
            else {
                LOG.info("Group does not exist!");
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

    public void leave() {

    }

    public void createGroup(String groupName, String password, PeerDHT ownerPeerDHT) {
        Number160 groupId = Number160.createHash(groupName);
        try {
            if (TomP2PExtras.findData(groupId, ownerPeerDHT) == null) {
                ownerPeerDHT.put(groupId).data(new Data(groupName).protectEntry(keyPair)).sign().start().awaitUninterruptibly();
                FutureGet futureGet = ownerPeerDHT.get(groupId).start().awaitUninterruptibly();
                PeerAddress rootAddress = (PeerAddress) futureGet.rawData().keySet().toArray()[0];
                SeriEvent getPKEvent = new SeriEvent(EventType.GET_PK);

                FutureResponse response = ownerPeerDHT.peer().sendDirect(rootAddress).object(getPKEvent.serialize()).start().futureResponse();
                response.awaitUninterruptibly();

                PublicKey rootPublicKey = (PublicKey)response.responseMessage().buffer(0).object();

                ownerPeerDHT.put(groupId).data(new Data(rootPublicKey).protectEntry(keyPair)).sign().start().awaitUninterruptibly();

                SecretKey grpAESKey = KeyGenerator.getInstance("AES").generateKey();

                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, rootPublicKey);
                byte[] cipheredGrpAESKey = cipher.doFinal(grpAESKey.getEncoded());
                byte[] cipheredPassword = cipher.doFinal(password.getBytes());

                SeriEvent createEvent = new SeriEvent(EventType.CREATE, groupName, nickName, cipheredPassword, cipheredGrpAESKey);
                LOG.debug("createEvent length: " + createEvent.serialize().length + " bytes \n");

                ownerPeerDHT.peer().sendDirect(rootAddress).object(createEvent.serialize()).start().awaitUninterruptibly();

                Group group = new Group(Role.OWNER,groupName, groupId, grpAESKey, rootAddress, password);
                groups.put(groupName, group);

                LOG.info("Chat group " + groupName + " is now created");
            }
        else {
                LOG.info("Group name allready exists!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        }

    }

    public void sendMsg(String groupName, PeerDHT sendingPeerDHT, String msg) {

        SeriEvent seriEvent = new SeriEvent(groupName, msg, nickName);
        Group group = groups.get(groupName);
        if(group.getRole() != Role.ROOT){
            PeerAddress root = group.getRootAddress();

            sendingPeerDHT.peer().sendDirect(root).object(seriEvent).start().awaitUninterruptibly();
        }
        else {

        }
    }

    public void stabilization() {

    }

    public void forwardMsg() {

    }

    public void changeRole() {

    }

    public Object handleEvent(SeriEvent event, PeerAddress sender) {
        Cipher cipher = null;
        switch (event.getType()) {
            case CHAT:
                return null;
            case CREATE:
                try {
                    cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                    byte[] grpSecretKeyBytes = cipher.doFinal(event.getGrpSecretKey());
                    SecretKey grpSecretKey = new SecretKeySpec(grpSecretKeyBytes, 0, grpSecretKeyBytes.length, "AES");
                    String password = new String(cipher.doFinal(event.getPassword()));
                    Group group = new Group(
                            Role.ROOT,
                            event.getGroupName(),
                            Number160.createHash(event.getGroupName()),
                            grpSecretKey,
                            sender,
                            event.getOwnerNickName(),
                            password);
                    groups.put(group.getGroupName(), group);
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
                }

                return null;

            case GET_PK:
                return keyPair.getPublic();
            case SEND_PK:
            case JOIN:
                try {
                    Group groupToJoin = groups.get(event.getGroupName());
                    if (groupToJoin != null && groupToJoin.getRole() == Role.ROOT) {
                        cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                        String password = new String(cipher.doFinal(event.getPassword()));
                        if (groupToJoin.getPassword() == password) {
                            cipher.init(Cipher.ENCRYPT_MODE, event.getPublicKey());
                            return cipher.doFinal(groupToJoin.getGrpAESKey().getEncoded());
                        }
                        return null;
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
                }
            case LEAVE:
            default:
                return null;
        }
    }
}
