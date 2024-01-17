package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.util.*;

@RequiredArgsConstructor
public class CheckMessages implements Runnable {

    private final Logger log = LogManager.getLogger();
    private final Node node;

    @Override
    public void run() {
        while (true) {
            synchronized (node.getVariables().getMessagesToSend()) {
                try {
                    Map<List<String>, Address> messages = node.getVariables().getMessagesToSend();
                    if (messages.isEmpty()) {
                        messages.wait();
                    } else {
                        for (Map.Entry<List<String>, Address> entry : messages.entrySet()) {
                            node.sendMessage(entry.getKey(), entry.getValue());
                        }
                        messages.clear();
                        node.getVariables().getMessagesToSend().wait();
                    }

                    if (node.getAddress() == null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while waiting", e);
                    Thread.currentThread().interrupt();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
