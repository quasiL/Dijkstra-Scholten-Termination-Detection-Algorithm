package cz.cvut.dsv;

import cz.cvut.dsv.console.ConsoleHandler;
import cz.cvut.dsv.console.InfoLevel;
import cz.cvut.dsv.console.NodeInfoPrinter;
import cz.cvut.dsv.model.Address;
import cz.cvut.dsv.model.Variables;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Node implements Runnable {

    protected static final Logger log = LogManager.getLogger();
    protected static final String RMI_SERVICE_NAME = "RMIService";
    public static final Random random = new Random();

    @Getter
    private final Address address;
    @Getter
    @Setter
    private Variables variables;
    @Getter
    @Setter
    private List<Address> neighbors;

    private CommunicationHub hub;
    private final NodeInfoPrinter nodeInfoPrinter;

    public static void main(String[] args) {
        if (args.length == 2 || args.length == 4) {
            Node node = new Node(args);
            node.run();
        } else {
            log.error("Invalid number of arguments.");
            throw new IllegalArgumentException("Invalid number of arguments.");
        }
    }

    public Node(String[] args) {
        this.address = new Address(args[0], Integer.parseInt(args[1]));
        this.variables = new Variables();
        if (args.length == 4) {
            Address neighbourAddress = new Address(args[2], Integer.parseInt(args[3]));
            ArrayList<Address> list = new ArrayList<>();
            list.add(neighbourAddress);
            this.neighbors = list;
        } else {
            this.neighbors = Collections.emptyList();
        }
        this.nodeInfoPrinter = new NodeInfoPrinter(this);
        log.info("This node is running on {}", address);
    }

    public List<Address> getAddresses() {
        if (neighbors.isEmpty()) {
            return new ArrayList<>();
        }

        neighbors.removeIf(neighbor -> !ping(neighbor));
        int randomSize = random.nextInt(neighbors.size()) + 1;
        return new ArrayList<>(neighbors.subList(0, randomSize));
    }

    public List<List<String>> distributeHashes(int numberOfNodes, List<String> hashes) {
        List<List<String>> distributedHashes = new ArrayList<>();

        for (int i = 0; i < numberOfNodes; i++) {
            distributedHashes.add(new ArrayList<>());
        }

        for (int i = 0; i < hashes.size(); i++) {
            int addressIndex = i % numberOfNodes;
            distributedHashes.get(addressIndex).add(hashes.get(i));
        }

        distributedHashes.removeIf(List::isEmpty);
        return distributedHashes;
    }

    public void initiate(int numberOfHashes) throws RemoteException {

        List<Address> addresses = getAddresses();
        int number = numberOfHashes == -1 ? 8 : numberOfHashes;
        List<String> hashes = HashOperations.generateHashes(number);

        if (addresses.isEmpty()) {
            log.error("Currently there are no active nodes in the network. You need to run at least one node.");
            return;
        }

        log.info("Initiating. Sending messages to {} nodes. " +
                        "Number of hashes: {}. One node can decipher {} hashes.",
                addresses.size(), hashes.size(), RemoteCommandsImpl.NUMBER_OF_HASHES_PER_REQUEST);

        variables.setIsInitiator(true);
        variables.setDefIn(1);

        List<List<String>> distributedHashes = distributeHashes(addresses.size(), hashes);
        int minSize = Math.min(distributedHashes.size(), addresses.size());
        for (int i = 0; i < minSize; i++) {
            variables.setDefOut(variables.getDefOut() + 1);
            log.info("Node {} will receive {} hashes. DefOut is now {}", addresses.get(i), distributedHashes.get(i).size(), variables.getDefOut());
            hub.getRmiProxy(addresses.get(i)).receiveMessage(distributedHashes.get(i), this.address);
        }
    }

    public void sendMessage(List<String> message, Address address) throws RemoteException {
        variables.setDefOut(variables.getDefOut() + 1);
        log.info("Sending a message ({} hashes) to {}. DefOut is now {}",message.size(), address, variables.getDefOut());
        hub.getRmiProxy(address).receiveMessage(message, this.address);
    }

    public void sendSignal(Map<String, Integer> result, Address address) throws RemoteException {
        variables.setDefIn(variables.getDefIn() - 1);
        log.info("Sending a signal DefIn is now {}", variables.getDefIn());
        hub.getRmiProxy(address).receiveSignal(result, this.address);

        if (Boolean.TRUE.equals(variables.getIsInitiator()) && variables.getDefOut() == 0 && variables.getDefIn() == 1) {
            log.info("Termination detection.");
        }
    }

    @Override
    public void run() {
        System.setProperty("java.rmi.server.hostname", address.getIpAddress());

        try {
            RemoteCommands skeleton = (RemoteCommands) UnicastRemoteObject
                    .exportObject(new RemoteCommandsImpl(this), 40000 + address.getPort());

            Registry registry = LocateRegistry.createRegistry(40000 + address.getPort());
            registry.rebind(RMI_SERVICE_NAME, skeleton);

            hub = new CommunicationHub();
            if (!neighbors.isEmpty()) {
                addressesRequest();
            }
            sendBroadcastUpdate();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        ConsoleHandler consoleHandler = new ConsoleHandler(this);
        new Thread(consoleHandler).start();
        CheckMessages checkMessages = new CheckMessages(this);
        new Thread(checkMessages).start();
        CheckSignals checkSignals = new CheckSignals(this);
        new Thread(checkSignals).start();
        Check check = new Check(this);
        new Thread(check).start();
    }

    public void printNodeInfo(InfoLevel... infoLevels) throws RemoteException {
        hub.getRmiProxy(address).printInfo(nodeInfoPrinter.createInfo(infoLevels));
    }

    private void addressesRequest() throws RemoteException {
        log.info("Requesting addresses from {}", neighbors.get(0));
        hub.getRmiProxy(neighbors.get(0)).addressesRequest(this.address);
    }

    public void addressesResponse(Address toAddress) throws RemoteException {
        hub.getRmiProxy(toAddress).addressesResponse(neighbors);
    }

    private void sendBroadcastUpdate() throws RemoteException {
        log.info("Sending information about this node to neighbors...");
        for (Address a : neighbors) {
            hub.getRmiProxy(a).updateNeighbors(this.address);
        }
    }

    public boolean ping(Address address) {
        try {
            hub.getRmiProxy(address).ping();
            return true;
        } catch (RemoteException e) {
            log.warn("Node {} is not responding.", address);
        }
        return false;
    }

    public void reset() {
        variables.getOthers().clear();
        variables.setIsInitiator(false);
        variables.setDefIn(0);
        variables.setDefOut(0);
        variables.setParent(null);
        variables.getMessages().clear();
        variables.getMessagesToSend().clear();
        variables.getResult().clear();
        variables.getSignalToSend().clear();
        log.info("Reset variables...");
    }
}
