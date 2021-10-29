import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class ChatThread implements Runnable{
	
	private HashMap<String, ChatHistory> chatHashMap;
	private String projectName;
	
	public ChatThread(String projectName, HashMap<String, ChatHistory> chatHashMap) {
		this.projectName=projectName;
		this.chatHashMap=chatHashMap;
	}

	public void run() {
		try {
			MulticastSocket ms=new MulticastSocket(30000);
			InetAddress group=InetAddress.getByName(chatHashMap.get(projectName).getIp());
			ms.joinGroup(group);
			ms.setSoTimeout(5000); //cosi riesce a terminare il thread con SocketTimeoutException
			while(!Thread.currentThread().isInterrupted()) {
				byte[] buffer=new byte[128];
				DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
				try {
					ms.receive(dp);
					String tmp=new String(dp.getData(), dp.getOffset() ,dp.getLength());
					chatHashMap.get(projectName).addChat(tmp);
				}
				catch(SocketTimeoutException e) {
				}
			}
			ms.leaveGroup(group);
			ms.close();
		}
		catch(IOException e) {
			System.out.println("errore nel ChatThread");
		}
	}
}
