package cz.cvut.dsv.console;

import cz.cvut.dsv.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeInfoPrinter {

    private Node node;

    public String createInfo(InfoLevel... infoLevels) {
        StringBuilder result = new StringBuilder("Node Info:");

        if (infoLevels.length == 0) {
            appendBasicInfo(result);
        } else {
            for (InfoLevel level : infoLevels) {
                switch (level) {
                    case BASIC:
                        appendBasicInfo(result);
                        break;
                    case VARIABLES:
                        appendVariablesInfo(result);
                        break;
                    case NEIGHBOURS:
                        appendAddressesInfo(result);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported InfoLevel: " + level);
                }
            }
        }

        return result.toString();
    }

    private void appendBasicInfo(StringBuilder result) {
        result.append(" Address: ").append(node.getAddress());
    }

    private void appendVariablesInfo(StringBuilder result) {
        result.append(" DefIn: ").append(node.getVariables().getDefIn())
                .append(", DefOut: ").append(node.getVariables().getDefOut())
                .append(", Parent: ").append(node.getVariables().getParent())
                .append(", Others: ").append(node.getVariables().getOthers())
                .append(", Result: ").append(node.getVariables().getResult());
    }

    private void appendAddressesInfo(StringBuilder result) {
        result.append(" Neighbors: ").append(node.getNeighbors().toString());
        //result.append(" message: ").append(node.getVariables().getMessages());
    }

}
