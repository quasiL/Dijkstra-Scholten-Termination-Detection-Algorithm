package cz.cvut.dsv;

import cz.cvut.dsv.model.Address;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface RemoteCommands extends Remote {

    void printInfo(String info) throws RemoteException;

    void receiveMessage(List<String> message, Address address) throws RemoteException;

    void receiveSignal(Map<String, Integer> result, Address address) throws RemoteException;

    void addressesRequest(Address address) throws RemoteException;

    void addressesResponse(List<Address> addresses) throws RemoteException;

    void updateNeighbors(Address address) throws RemoteException;

    void ping() throws RemoteException;
}
