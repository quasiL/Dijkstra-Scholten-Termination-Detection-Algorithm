package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CheckSignals implements Runnable {

    private final Logger log = LogManager.getLogger();
    private final Node node;

    @Override
    public void run() {
        while (true) {
            synchronized (node.getVariables().getSignalToSend()) {
                try {
                    if (node.getVariables().getSignalToSend().isEmpty()) {
                        node.getVariables().getSignalToSend().wait();
                    } else {
                        Map<Map<String, Integer>, Address> signal = node.getVariables().getSignalToSend();

                        for (Map.Entry<Map<String, Integer>, Address> entry : signal.entrySet()) {
                            node.sendSignal(entry.getKey(), entry.getValue());
                        }
                        signal.clear();
                        node.getVariables().getSignalToSend().wait();
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
