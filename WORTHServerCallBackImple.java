import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

public class WORTHServerCallBackImple extends RemoteObject implements WORTHServerCallBackInterface {
	
	private ArrayList<WORTHClientCallBackInterface> clients;
    
    public WORTHServerCallBackImple() {
    	this.clients=new ArrayList<WORTHClientCallBackInterface>();
    }
	@Override
	public synchronized void update(String message) throws RemoteException {
		doCallBacks(message);
	}
	@Override
	public synchronized void register(WORTHClientCallBackInterface client) throws RemoteException {
		 if(!clients.contains(client)) {
	            clients.add(client);
	            System.out.println("client registrato");
	        }
	}
	@Override
	public synchronized void unregister(WORTHClientCallBackInterface client) throws RemoteException {
		if(clients.contains(client)) {
            clients.remove(client);
            System.out.println("client cancellato");
        }
	}
	
	public  void doCallBacks(String message) throws RemoteException{
		Iterator<WORTHClientCallBackInterface> iterator=clients.iterator();
		while(iterator.hasNext()) {
			iterator.next().update(message);
		}
	}
}
