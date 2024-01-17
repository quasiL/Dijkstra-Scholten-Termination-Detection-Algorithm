package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HashOperations implements Runnable {

    protected static final Integer LOWER_BOUND = 1000000;
    protected static final Integer UPPER_BOUND = 9999999;

    private final Logger log = LogManager.getLogger();
    private static final ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final AtomicBoolean done = new AtomicBoolean(false);

    @Getter
    private List<String> hashes;
    private final Node node;
    private List<String> synchronizedHashes;
    private Address address;

    public HashOperations(List<String> hashes, Node node, Address address) {
        this.hashes = hashes;
        this.node = node;
        //this.synchronizedHashes = Collections.synchronizedList(new ArrayList<>(hashes));
        this.address = address;
    }

    @Override
    public void run() {
        log.info("Starting calculation...");
        Map<String, Integer> result = new HashMap<>();
        for (String hash : hashes) {
            int number = findNumberFromHash(hash);
            result.put(hash, number);
        }
        log.info("Finishing calculation.");
    }

    public static int findNumberFromHash(String hash) {
        int number = 0;
        while (!hash.equals(DigestUtils.sha256Hex(String.valueOf(number)))) {
            number++;
        }
        return number;
    }

    public static Map<String, Integer> getAllResults() {
        return new HashMap<>(results);
    }

    public static boolean isCalculationRunning() {
        return isRunning.get();
    }
    public static boolean isCalculationDone() {
        return done.get();
    }

    public static List<String> generateHashes(int numberOfHashes) {
        if (LOWER_BOUND >= UPPER_BOUND) {
            throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        }

        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < numberOfHashes; i++) {
            Integer number = Node.random.nextInt(UPPER_BOUND - LOWER_BOUND + 1) + LOWER_BOUND;
            hashes.add(DigestUtils.sha256Hex(String.valueOf(number)));
        }
        return hashes;
    }
}
