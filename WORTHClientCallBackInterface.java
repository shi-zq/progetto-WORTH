import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WORTHClientCallBackInterface extends Remote {
	
	public void update(String message) throws RemoteException;
}
