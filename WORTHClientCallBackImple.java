import java.rmi.server.RemoteObject;
import java.util.ArrayList;

public class WORTHClientCallBackImple extends RemoteObject implements WORTHClientCallBackInterface {
	
	private ArrayList<Member> listOfUser;
	
	public WORTHClientCallBackImple(ArrayList<Member> listOfUser) {
		this.listOfUser=listOfUser;
	}
	@Override
	public synchronized void update(String message) {
		String[] input=message.split("-");
		int index=listOfUser.indexOf(new Member(input[0]));
		if(index==-1) {
			listOfUser.add(new Member(input[0], null, input[1]));
		}
		else {
			listOfUser.get(index).changeStatus(input[1]);
		}
	}
}
