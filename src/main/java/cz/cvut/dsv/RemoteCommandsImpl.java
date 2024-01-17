package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

@RequiredArgsConstructor
public class RemoteCommandsImpl implements RemoteCommands {

    private final Logger log = LogManager.getLogger();
    private final Node node;
    // How many hashes a node should calculate per request
    protected static final Integer NUMBER_OF_HASHES_PER_REQUEST = 2;

    @Override
    public void printInfo(String info) throws RemoteException {
        log.info(info);
    }

    @Override
    public void receiveMessage(List<String> message, Address address) throws RemoteException {

        if (node.getVariables().getDefIn() == 0) {
            node.getVariables().setParent(address);
        } else {
            List<Address> others = node.getVariables().getOthers();
            others.add(address);
        }
        node.getVariables().setDefIn(node.getVariables().getDefIn() + 1);
        synchronized (node.getVariables().getMessages()) {
            node.getVariables().getMessages().put(message, address);
        }
        log.info("Received a message from {} with {} hashes. DefIn is now {}", address, message.size(), node.getVariables().getDefIn());

        // Split the message
        List<String> newMessage = Collections.synchronizedList(new ArrayList<>());
        if (message.size() > NUMBER_OF_HASHES_PER_REQUEST) {
            for (int i = NUMBER_OF_HASHES_PER_REQUEST; i < message.size(); i++) {
                newMessage.add(message.get(i));
            }
            message = message.subList(0, NUMBER_OF_HASHES_PER_REQUEST);
        }

        // Send subsequent messages
        if (!newMessage.isEmpty()) {
            List<Address> addresses = node.getAddresses();
            List<List<String>> distributedHashes = node.distributeHashes(addresses.size(), newMessage);

            int minSize = Math.min(distributedHashes.size(), addresses.size());
            synchronized (node.getVariables().getMessagesToSend()) {
                for (int i = 0; i < minSize; i++) {
                    Map<List<String>, Address> messages = node.getVariables().getMessagesToSend();
                    messages.put(distributedHashes.get(i), addresses.get(i));
                }
                node.getVariables().getMessagesToSend().notifyAll();
            }
        }

        // Calculate hashes
        Map<String, Integer> result = new HashMap<>();
        for (String hash : message) {
            log.info("Starting to calculate hash {}", hash);
            int number = HashOperations.findNumberFromHash(hash);
            result.put(hash, number);
            log.info("Ending to calculate hash {}", hash);
        }

        boolean isFirst = true;
        Map<List<String>, Address> messages = node.getVariables().getMessages();
        for (Map.Entry<List<String>, Address> entry : messages.entrySet()) {
            Address a = entry.getValue();
            //List<String> hashList = entry.getKey();

            if (address.equals(a) && isFirst) {
                synchronized (node.getVariables().getResult()) {
                    node.getVariables().getResult().putAll(result);
                }
            }
            isFirst = false;
        }
        synchronized (node.getVariables().getSignalToSend()) {
            node.getVariables().getSignalToSend().put(result, address);
            node.getVariables().getSignalToSend().notifyAll();
        }

    }

    @Override
    public void receiveSignal(Map<String, Integer> result, Address address) throws RemoteException {

        log.info("Getting signal from {} with result {}", address, result);
        node.getVariables().setDefOut(node.getVariables().getDefOut() - 1);

        if (Boolean.TRUE.equals(node.getVariables().getIsInitiator()) && node.getVariables().getDefOut() == 0 && node.getVariables().getDefIn() == 1) {
            log.info("Termination detection.");
        }
    }

    @Override
    public void addressesRequest(Address address) throws RemoteException {
        node.addressesResponse(address);
        log.info("Sent addresses to {}.", address);
    }

    @Override
    public void addressesResponse(List<Address> addresses) throws RemoteException {
        List<Address> updatedNeighbors = new ArrayList<>(node.getNeighbors());
        for (Address a : addresses) {
            if (!a.equals(node.getNeighbors().get(0))) {
                updatedNeighbors.add(a);
            }
        }
        node.setNeighbors(updatedNeighbors);

        log.info("Discovered nodes: {}", addresses);
    }

    @Override
    public void updateNeighbors(Address address) throws RemoteException {
        List<Address> updatedNeighbors = new ArrayList<>(node.getNeighbors());
        updatedNeighbors.add(address);
        node.setNeighbors(updatedNeighbors);
        log.info("Current neighbors: {}", updatedNeighbors);
    }


    @Override
    public void ping() throws RemoteException {
        // checking if node is alive
    }
}
