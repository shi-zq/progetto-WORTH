import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class WORTHclient implements Runnable{
	
	private ArrayList<Member> listUsers;
	private HashMap<String, ChatHistory> chatHashMap;
	private ThreadPoolExecutor executor;
	private String nickName;
	private boolean running;
	
	public WORTHclient() {
		listUsers=new ArrayList<Member>();
		chatHashMap=new HashMap<String, ChatHistory>();
		executor=(ThreadPoolExecutor)Executors.newCachedThreadPool();
		this.nickName="";
		running=true;
	}
	
	public void help() {
		System.out.println("login username password");
		System.out.println("logout");
		System.out.println("listUsers");
		System.out.println("listOnlineUsers");
		System.out.println("listProjects");
		System.out.println("createProject projectName");
		System.out.println("addMember projectName memberName");
		System.out.println("showMembers projectName");
		System.out.println("showCards projectName");
		System.out.println("showCard projectName cardName");
		System.out.println("addCard projectName cardName, description usare _ per il spazio");
		System.out.println("moveCard projectName cardName source destination");
		System.out.println("getCardHistory projectName cardName");
		System.out.println("cancelProject projectName");
		System.out.println("sencChat projectName");
		System.out.println("readChat rojectName");
	}
	
	public void run() {
		try {
			Registry r=LocateRegistry.getRegistry(30002);
			String name="WORTHServerCallBack";
			WORTHServerCallBackInterface server=(WORTHServerCallBackInterface)r.lookup(name);
			WORTHClientCallBackInterface callBackObject=new WORTHClientCallBackImple(listUsers);
			WORTHClientCallBackInterface callBackStub=(WORTHClientCallBackInterface) UnicastRemoteObject.exportObject(callBackObject, 0);
			SocketAddress address=new InetSocketAddress("localhost", 33333);
			SocketChannel client=SocketChannel.open(address);
			Scanner scanner = new Scanner(System.in);
			while(running) {
				System.out.println("inserisce il commando");
				String input= scanner.nextLine();
				input=input.concat(" "); //per avere un separatore nel finale della string
				String[] command=input.split(" ");
				switch(command[0]) {
					case("register"):
						if(command.length==3) {
							register(command);
						}
						else {
							this.help();
						}
						break;
					case("login"): 
						if(command.length==3) {
							login(input, client, callBackStub, server);
						}
						else {
							this.help();
						}
						break;
					case("logout"): 
						if(command.length==1) {
							logout(input, client, callBackStub, server);
						}
						else {
							this.help();
						}
						break;
					case("listUsers"):
						if(command.length==1) {
							listUsers();
						}
						else {
							this.help();
						}
						break;
					case("listOnlineUsers"): 
						if(command.length==1) {
							listOnlineUsers();
						}
						else {
							this.help();
						}
						break;
					case("listProjects"): 
						if(command.length==1) {
							listProjects(input, client);
						}
						else {
							this.help();
						}
						break;
					case("createProject"):
						if(command.length==2) {
							createProject(input, client);
						}
						else {
							this.help();
						}
						break;
					case("addMember"):
						if(command.length==3) {
							addMember(input, client);
						}
						else {
							this.help();
						}
						break;
					case("showMembers"):
						if(command.length==2) {
							showMembers(input, client);
						}
						else {
							this.help();
						}
						break;
					case("showCards"):
						if(command.length==2) {
							showCards(input, client);
						}
						else {
							this.help();
						}
						break;
					case("showCard"):
						if(command.length==3) {
							showCard(input, client);
						}
						else {
							this.help();
						}
						break;
					case("addCard"):
						if(command.length==4) {
							addCard(input, client);
						}
						else {
							this.help();
						}
						break;
					case("moveCard"):
						if(command.length==5) {
							moveCard(input, client);
						}
						else {
							this.help();
						}
						break;
					case("getCardHistory"):
						if(command.length==3) {
							getCardHistory(input, client);
						}
						else {
							this.help();
						}
						break;
					case("cancelProject"):
						if(command.length==2) {
							cancelProject(input, client);
						}
						else {
							this.help();
						}
						break;
					case("sendChat"):
						if(!nickName.equals("")) {
							if(command.length==2) {
								System.out.println("inserisci il tuo messaggio");
								String message=scanner.nextLine();
								message=nickName.concat(": " + message);
								sendChat(command, message, nickName);
							}
							else {
								this.help();
							}
						}
						else {
							System.out.println("non sei loggato");
						}
						break;
					case("readChat"):
						if(!nickName.equals("")) {
							if(command.length==2) {
								readChat(command);
								}
							else {
								this.help();
							}
						}
						else {
							System.out.println("non sei loggato");
						}
						break;
					case("quit"):
						if(nickName.equals("")) {
							running=false;
						}
						else {
							System.out.println("fare il logout");
						}
						break;
					default:
						this.help();
				}
			}
			scanner.close();
			System.out.println("chiusura del client");
		}
		catch(RemoteException e) {
			System.out.println("errore nel inizializzazione del RMI call back");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(NotBoundException e) {
			System.out.println("errore nel lookup");
		}
	}
	
	public void login(String command, SocketChannel client, WORTHClientCallBackInterface callBackStub, WORTHServerCallBackInterface server) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		String[] tmp=output.split(System.lineSeparator());
		output=tmp[0];
		if(output.equals("OK")) {
			nickName=command.split(" ")[1];
			int i=1; //elemento 0 \`e il OK
			while(!(tmp[i].equals("SERVER.SPLIT"))) { //i dati riguardanti al progetto e multicastAddress vanno a copie per ogni volta fa +2
				chatHashMap.put(tmp[i], new ChatHistory(tmp[i+1]));
				ChatThread chatThread=new ChatThread(tmp[i], chatHashMap);
				executor.execute(chatThread);
				i=i+2;
			}
			i=i+1;//elemento i \`e il SERVER.SPLIT
			while(i<tmp.length-1) {//ultima stringa \`e vuoto
				this.listUsers.add(new Member(tmp[i], null, tmp[i+1]));
				i=i+2;
			}
			try {
				server.register(callBackStub);
			}
			catch(RemoteException e) {
				System.out.println("errore nel RMI call back durante login");
			}
		}
		System.out.println(output);
	}
	
	public void logout(String command, SocketChannel client, WORTHClientCallBackInterface callBackStub, WORTHServerCallBackInterface server) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		output=output.split(System.lineSeparator())[0];
		if(output.equals("OK")) {
			try {
				server.unregister(callBackStub);
				this.listUsers.clear();
				nickName="";
				this.executor.shutdownNow();
				while(!this.executor.isTerminated());
				this.chatHashMap.clear();
				this.executor=(ThreadPoolExecutor)Executors.newCachedThreadPool();
				}
				catch(RemoteException e) {
					System.out.println("errore nel RMI call back durante logout");
				}
		}
		System.out.println(output);
	}
	
	public void listUsers() {
		Iterator<Member> iterator=this.listUsers.iterator();
		while(iterator.hasNext()) {
			System.out.println(iterator.next().getNickName());
		}
	}
	
	public void listOnlineUsers() {
		Iterator<Member> iterator=this.listUsers.iterator();
		while(iterator.hasNext()) {
			Member tmp=iterator.next();
			if(tmp.getStatus().equals("online")) {
				System.out.println(tmp.getNickName());
			}
		}
	}
	
	public void listProjects(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void createProject(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void addMember(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void showMembers(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void showCards(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void showCard(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void addCard(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void moveCard(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void getCardHistory(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void cancelProject(String command, SocketChannel client) throws IOException {
		byte[] inputByte=new byte[1024];
		ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputByte);
		inputByteBuffer.put(command.getBytes(StandardCharsets.UTF_8));
		inputByteBuffer.flip();
		while(inputByteBuffer.hasRemaining()) {
			client.write(inputByteBuffer);
		}
		byte[] outputByte=new byte[1024];
		ByteBuffer outputByteBuffer=ByteBuffer.wrap(outputByte);
		client.read(outputByteBuffer);
		String output=new String(outputByteBuffer.array(), StandardCharsets.UTF_8);
		System.out.println(output);
	}
	
	public void sendChat(String[] command, String message, String nickName) throws IOException {
		ChatHistory tmp=chatHashMap.get(command[1]);
		if(tmp!=null) {
			InetAddress ip=InetAddress.getByName(chatHashMap.get(command[1]).getIp());
			byte[] inputByte=new byte[1024];
			inputByte=message.getBytes();
			DatagramPacket data=new DatagramPacket(inputByte, inputByte.length, ip, 30000);
			DatagramSocket ms = new DatagramSocket();
			ms.send(data);
			ms.close();
		}
		else {
			System.out.println("progetto non esistente, prova a fare il logout e login per il aggiornamento");
		}
	}
	
	public void readChat(String[] command) {
		ChatHistory tmp=chatHashMap.get(command[1]);
			if(tmp!=null) {
				tmp.readChat();
			}
			else {
				System.out.println("progetto non esistente, prova a fare il logout e login per il aggiornamento");
			}
	}
	
	public void register(String[] command) {
		try {
			Registry r=LocateRegistry.getRegistry(30001);
			Remote remoteObject=r.lookup("WORTHServerRegister");
			WORTHServerRegisterInterface serverObject=(WORTHServerRegisterInterface) remoteObject;
			System.out.println(serverObject.register(command[1], command[2]));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("errore nel invocazione del registrazione di RMI");
		}
	}
	
	
}
	