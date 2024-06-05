/**
 * Names:
 * Caparas, Joaquin Gabriel
 * Carino,Mark Lorenz
 * DeMesa, Rovic Louie
 * San Miguel, Chloe Lee
 * Sin, Lawrence Edward
 * Vergara, Carlos Miguel
 * Project: IntegLee Car Rental - RMI implementation
 */
package Server;
import Utilities.servant;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain extends UnicastRemoteObject {
    protected ServerMain() throws RemoteException {}
    public static void main(String[] args) {
        try {
            servant stub =  new ServerController();
            Registry reg = LocateRegistry.createRegistry(2000);
            reg.rebind("IntegLee", stub);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
