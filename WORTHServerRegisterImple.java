import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WORTHServerRegisterImple extends RemoteServer implements WORTHServerRegisterInterface{
	
	private ArrayList<Member> listOfUser;
	private WORTHServerCallBackImple callBack;
	
	public WORTHServerRegisterImple(ArrayList<Member> listOfUser, WORTHServerCallBackImple callBack) {
		this.listOfUser=listOfUser;
		this.callBack=callBack;
	}
	
	public synchronized String register(String username, String password) throws RemoteException {
		if(username==null || password==null || username.equals("") || password.equals("") || username.contains(" ") || password.contains(" ")) {
			return "username e password non conforme alle regole";
		}
		if(this.listOfUser.contains(new Member(username))) {
			return "username esistente";
		}
		else {
			Member newMember=new Member(username, password);
			listOfUser.add(newMember);
			callBack.update(username + "-" + "offline");
			try{
				File member=new File("." + File.separator + "Members" + File.separator + username + ".json");
				member.createNewFile();
				ObjectMapper objectMapper=new ObjectMapper();
				objectMapper.writeValue(member, newMember);
			}
			catch(IOException e) {
				System.out.println("errore nel salvare in json nel registrazione membro");
			}
			return "OK";
		}
	}
}
