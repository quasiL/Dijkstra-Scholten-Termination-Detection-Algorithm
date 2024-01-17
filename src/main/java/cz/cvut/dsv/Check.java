package cz.cvut.dsv;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;

@RequiredArgsConstructor
public class Check implements Runnable {

    private final Logger log = LogManager.getLogger();
    private final Node node;

    @Override
    public void run() {
        while (true) {
            synchronized (node.getVariables().getResult()) {
                try {
                    if (node.getVariables().getResult().isEmpty()) {
                        node.getVariables().getResult().wait();
                    } else {
                        if (node.getVariables().getDefIn() == 1 && node.getVariables().getDefOut() == 0) {
                            node.sendSignal(node.getVariables().getResult(), node.getVariables().getParent());
                        }

                        node.getVariables().getResult().wait();
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

