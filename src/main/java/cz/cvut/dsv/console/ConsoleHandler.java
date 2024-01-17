package cz.cvut.dsv.console;

import cz.cvut.dsv.Node;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

@RequiredArgsConstructor
public class ConsoleHandler implements Runnable {

    private final Logger log = LogManager.getLogger();

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final Node node;
    private boolean readInput = true;

    private void parseInput(String input) throws RemoteException {
        if (input.startsWith("info")) {
            parseInfoCommand(input.substring("info".length()).trim());
            return;
        }
        if (input.startsWith("init")) {
            if (input.equals("init")) {
                node.initiate(-1);
                return;
            } else {
                String[] parts = input.split(" ");
                if (parts.length == 2) {
                    String additionalInfo = parts[1];
                    if (isNumeric(additionalInfo)) {
                        node.initiate(Integer.parseInt(additionalInfo));
                        return;
                    }
                }
            }

        }
        if (input.equals("reset")) {
            node.reset();
            return;
        }
        log.error("Invalid input.");
    }

    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private void parseInfoCommand(String arguments) throws RemoteException {

        if (arguments.isEmpty()) {
            node.printNodeInfo();
            return;
        }

        if (arguments.contains("-") && arguments.length() == 2) {
            if (arguments.contains("a")) {
                node.printNodeInfo(InfoLevel.BASIC, InfoLevel.VARIABLES, InfoLevel.NEIGHBOURS);
            } else if (arguments.contains("v")) {
                node.printNodeInfo(InfoLevel.VARIABLES);
            } else if (arguments.contains("b")) {
                node.printNodeInfo(InfoLevel.BASIC);
            } else if (arguments.contains("n")) {
                node.printNodeInfo(InfoLevel.NEIGHBOURS);
            }
        } else if (arguments.contains("-") && arguments.length() == 3 &&
                (arguments.contains("vb") || arguments.contains("bv"))) {
            node.printNodeInfo(InfoLevel.BASIC, InfoLevel.VARIABLES);
        } else {
            log.error("Invalid arguments for the 'info' command.");
        }
    }

    @Override
    public void run() {
        log.info("Console handler started");
        while (readInput) {
            try {
                String input = reader.readLine();
                parseInput(input);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                readInput = false;
            }
        }
    }
}
