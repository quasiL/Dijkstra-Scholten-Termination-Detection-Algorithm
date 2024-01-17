package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CommunicationHub {

    public RemoteCommands getRmiProxy(Address address) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(address.getIpAddress(), 40000 + address.getPort());
            return (RemoteCommands) registry.lookup(Node.RMI_SERVICE_NAME);
        } catch (NotBoundException nbe) {
            throw new RemoteException(nbe.getMessage());
        }
    }
}
