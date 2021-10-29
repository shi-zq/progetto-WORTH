import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WORTHServerCallBackInterface extends Remote {
	
	public void update(String message) throws RemoteException;
    public void register(WORTHClientCallBackInterface client) throws RemoteException;
    public void unregister(WORTHClientCallBackInterface client) throws RemoteException;
}
