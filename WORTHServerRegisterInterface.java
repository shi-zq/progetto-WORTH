import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WORTHServerRegisterInterface extends Remote {
	
	public String register(String username, String password) throws RemoteException ;
}
