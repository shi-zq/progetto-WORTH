import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WORTHServer implements Runnable{
	
	private ArrayList<Member> listUsers;
	private ArrayList<Project> listProject;
	private MulticastAddressGenerator generator;
	private ObjectMapper serverObjectMapper;
	private WORTHServerCallBackImple callBack;
	
	public WORTHServer() {
		listUsers=new ArrayList<Member>();
		listProject=new ArrayList<Project>();
		generator= new MulticastAddressGenerator();
		this.serverObjectMapper=new ObjectMapper();
		this.callBack=new WORTHServerCallBackImple();
	}

	public void run() {
		boolean running=true;
		loadJson(running);
		startRegister(running);
		startCallBack(running);
		try {
			ServerSocketChannel serverChannel;
			Selector selector;
			serverChannel=ServerSocketChannel.open();
			ServerSocket server=serverChannel.socket();
			InetSocketAddress address=new InetSocketAddress(33333);
			server.bind(address);
			serverChannel.configureBlocking(false);
			selector=Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("server pronto");
			while(true) {
				try {
					selector.select();
				}
				catch(IOException e) {
					System.out.println("seletore fallito");
					break;
				}
				Set<SelectionKey> readyKeys=selector.selectedKeys();
				Iterator<SelectionKey> iterator=readyKeys.iterator();
				while(iterator.hasNext()) {
					SelectionKey key=iterator.next();
					iterator.remove();
					try {
						if(key.isAcceptable()) {
							ServerSocketChannel serverAccept=(ServerSocketChannel) key.channel();
							SocketChannel client=serverAccept.accept();
							System.out.println("connesso da " + client);
							client.configureBlocking(false);
							SelectionKey key1=client.register(selector, SelectionKey.OP_READ);
							key1.attach(new ServerAnswer("",""));
						}
						if(key.isReadable()) {
							SocketChannel serverReceive=(SocketChannel)key.channel();
							byte[] inputBuffer=new byte[1024];
							ByteBuffer inputByteBuffer=ByteBuffer.wrap(inputBuffer);
							serverReceive.read(inputByteBuffer);
							String input=new String(inputByteBuffer.array(), StandardCharsets.UTF_8);
							String[] command=input.split(" ");
							ServerAnswer serverAnswer=(ServerAnswer)key.attachment();
							System.out.println("command ricevuto: " + command[0]); //command ha un lenght in piu per la stringa vuota in fondo
							switch(command[0]) {
								case("login"):
									login(command, serverAnswer);
									break;
								case("logout"):
									logout(serverAnswer);
									break;
								case("listProjects"):
									listProjects(serverAnswer);
									break;
								case("createProject"):
									createProject(command, serverAnswer);
									break;
								case("addMember"):
									addMember(command, serverAnswer);
									break;
								case("showMembers"):
									showMembers(command, serverAnswer);
									break;
								case("showCards"):
									showCards(command, serverAnswer);
									break;
								case("showCard"):
									showCard(command, serverAnswer);
									break;
								case("addCard"):
									addCard(command, serverAnswer);
									break;
								case("moveCard"):
									moveCard(command, serverAnswer);
									break;
								case("getCardHistory"):
									getCardHistory(command, serverAnswer);
									break;
								case("cancelProject"):
									cancelProject(command, serverAnswer);
									break;
								default:
									serverAnswer.setAnswer("operazione non valida");
							}
							SelectionKey key2= serverReceive.register(selector, SelectionKey.OP_WRITE);
							key2.attach(serverAnswer);
						}
						if(key.isWritable()) {
							SocketChannel serverWrite=(SocketChannel)key.channel();
							ServerAnswer output=(ServerAnswer)key.attachment();
							byte[] outBuffer=new byte[1024];
							ByteBuffer outputByteBuffer=ByteBuffer.wrap(outBuffer);
							outputByteBuffer.put(output.getAnswer().getBytes(StandardCharsets.UTF_8));
							outputByteBuffer.flip();
							while(outputByteBuffer.hasRemaining()) {
								serverWrite.write(outputByteBuffer);
							}
							SelectionKey key3= serverWrite.register(selector, SelectionKey.OP_READ);
							key3.attach(output);
						}
					}
					catch(IOException e) {
						key.cancel();
					}
				}
			}
		}
		catch(IOException e) {
			System.out.println("server down");
		}
	}
	
	public void login(String[] command, ServerAnswer serverAnswer) {
		int found=listUsers.indexOf(new Member(command[1]));				
		if(found!=-1) {													
			if(serverAnswer.getNickName().equals("")) {
				if(listUsers.get(found).getPassword().equals(command[2])) {
					if(listUsers.get(found).getStatus().equals("offline")) {
						listUsers.get(found).goOnline();
						serverAnswer.setNickName(command[1]);
						serverAnswer.setAnswer("OK");
						Iterator<Project> iteratorProject=listProject.iterator();
						while(iteratorProject.hasNext()) {
							Project tmpProject=iteratorProject.next();
							if(tmpProject.checkmember(serverAnswer.getNickName())) {
								serverAnswer.setAnswer(serverAnswer.getAnswer() + System.lineSeparator() + tmpProject.getProjectName() + System.lineSeparator() + tmpProject.getMulticastAddress());
							}
						}
						serverAnswer.setAnswer(serverAnswer.getAnswer() + System.lineSeparator() +  "SERVER.SPLIT" + System.lineSeparator());
						Iterator<Member> iteratorUser=listUsers.iterator();
						while(iteratorUser.hasNext()) {
							Member tmpUser=iteratorUser.next();
							serverAnswer.setAnswer(serverAnswer.getAnswer() + tmpUser.getNickName() + System.lineSeparator() + tmpUser.getStatus() + System.lineSeparator());
						}
						try {
							callBack.update(command[1] + "-" + "online");
						}
						catch(RemoteException e) {
							System.out.println("errore nel callBack durante login");
						}
						return;
					}
					else {
						serverAnswer.setAnswer("utente loggato in una altro posto");
						return;
					}
				}
				else {
					serverAnswer.setAnswer("password errato");
					return;
				}
			}
			else {
				serverAnswer.setAnswer("utente loggato, fare il logout");
				return;	
			}
		}
		else {
			serverAnswer.setAnswer("utente non esistente");
			return;
		}
	}
	
	public void logout(ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			int found=listUsers.indexOf(new Member(serverAnswer.getNickName()));
			listUsers.get(found).goOffline();
			try {
				callBack.update(serverAnswer.getNickName() + "-" + "offline");
			}
			catch(RemoteException e) {
				System.out.println("errore nel callBack durante logout");
			}
			serverAnswer.setNickName("");
			serverAnswer.setAnswer("OK" + System.lineSeparator());
			return;
		}
	}
		
	public void listProjects(ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");	
			return;
		}
		else {
			String answer="";
			Iterator<Project> iterator=listProject.iterator();
			while(iterator.hasNext()) {
				Project tmp=iterator.next();
				if(tmp.checkmember(serverAnswer.getNickName())) {
					if(answer.equals("")) {
						answer=tmp.getProjectName();
					}
					else {
						answer=answer.concat(System.lineSeparator() + tmp.getProjectName());
					}
				}
			}
			if(answer.equals("")) {
				serverAnswer.setAnswer("non hai progetti");
			}
			else {
				serverAnswer.setAnswer(answer);
			}
			return;
		}
	}
	
	public void createProject(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			if(listProject.contains(tmp)) {
				serverAnswer.setAnswer("progetto esistente, cambiare il nome");
				return;
			}
			else {
				Project newProject=new Project(command[1], serverAnswer.getNickName(), generator.generateMulticastAddress());
				try {
					File projectDir=new File("." + File.separator + "Projects" + File.separator + command[1]);
					projectDir.mkdir();
					File projectJson=new File("." + File.separator + "Projects" + File.separator + command[1] + File.separator + "project.json");
					projectJson.createNewFile();
					serverObjectMapper.writeValue(projectJson, newProject);
					File cardDir=new File("." + File.separator + "Projects" + File.separator + command[1] + File.separator + "Cards");
					cardDir.mkdir();
				}
				catch(IOException e) {
					System.out.println("WORTHServer.createProject-errore nel salvare in json");
				}
				listProject.add(newProject);
				serverAnswer.setAnswer("OK, riavviare il client");
				return;
			}
		}
	}
	
	public void addMember(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				if(listUsers.contains(new Member(command[2]))) {
					serverAnswer.setAnswer(listProject.get(found).addmember(serverAnswer.getNickName(), command[2]));
					return;
				}
				else {
					serverAnswer.setAnswer("utente non esistente");
					return;
				}
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void showMembers(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).getAllMembers(serverAnswer.getNickName()));
				return;
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void showCards(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).getAllCards(serverAnswer.getNickName()));
				return;
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void showCard(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).getCard(command[2], serverAnswer.getNickName()));
				return;
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void addCard(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).addCard(command[2], command[3], serverAnswer.getNickName()));
				if(serverAnswer.getAnswer().equals("OK")) {
					try {
						InetAddress ip=InetAddress.getByName(listProject.get(found).getMulticastAddress());
						String message=new String(serverAnswer.getNickName() + " ha creato " + command[2]);
						byte[] messageByte=new byte[1024];
						messageByte=message.getBytes();
						DatagramPacket data=new DatagramPacket(messageByte, messageByte.length, ip, 30000);
						DatagramSocket ms = new DatagramSocket();
						ms.send(data);
						ms.close();
					}
					catch(IOException e) {
						System.out.println("errore nel mandare messaggio nel multicast");
					}
				return;
				}
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void moveCard(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).moveCard(command[2], command[3], command[4], serverAnswer.getNickName()));
				if(serverAnswer.getAnswer().equals("OK")) {
					try {
						InetAddress ip=InetAddress.getByName(listProject.get(found).getMulticastAddress());
						String message=new String(serverAnswer.getNickName() + " ha spostato " + command[2] + " da " + command[3] + " a " + command[4]);
						byte[] messageByte=new byte[1024];
						messageByte=message.getBytes();
						DatagramPacket data=new DatagramPacket(messageByte, messageByte.length, ip, 30000);
						DatagramSocket ms = new DatagramSocket();
						ms.send(data);
						ms.close();
					}
					catch(IOException e) {
						System.out.println("errore nel mandare messaggio nel multicast");
					}
					return;
				}
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	
	public void getCardHistory(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).getCardHistory(command[2], serverAnswer.getNickName()));
				return;
			}
			else {
				serverAnswer.setAnswer("progetto non esistente");
				return;
			}
		}
	}
	public void cancelProject(String[] command, ServerAnswer serverAnswer) {
		if(serverAnswer.getNickName().equals("")) {
			serverAnswer.setAnswer("utente non loggato, fare il login");
			return;
		}
		else {
			Project tmp=new Project(command[1]);
			int found=listProject.indexOf(tmp);
			if(found!=-1) {
				serverAnswer.setAnswer(listProject.get(found).cancelProject(serverAnswer.getNickName()));
				if(serverAnswer.getAnswer().equals("OK")) {
					File projectDir=new File("." + File.separator + "Projects" + File.separator + command[1]);
					File cardDir=new File("." + File.separator + "Projects" + File.separator + command[1] + File.separator + "Cards");
					File[] cardJson=cardDir.listFiles();
					for(int i=0 ; i<cardJson.length ; i++) {
						cardJson[i].delete();
					}
					File projectJson=new File("." + File.separator + "Projects" + File.separator + command[1] + File.separator + "project.json");
					projectJson.delete();
					cardDir.delete();
					projectDir.delete();
					try {
						InetAddress ip=InetAddress.getByName(listProject.get(found).getMulticastAddress());
						String message=new String(serverAnswer.getNickName() + " ha cancellato il progetto, il chat valido fino al prossimo riavvio del server");
						byte[] messageByte=new byte[1024];
						messageByte=message.getBytes();
						DatagramPacket data=new DatagramPacket(messageByte, messageByte.length, ip, 30000);
						DatagramSocket ms = new DatagramSocket();
						ms.send(data);
						ms.close();
					}
					catch(IOException e) {
						System.out.println("errore nel mandare messaggio nel multicast");
					}
					listProject.remove(found);
				}
				return;
			}
			else {
				serverAnswer.setAnswer("project non esistente");
				return;
			}
		}
	}
	
	public void loadJson(boolean running) {
		try {
			File memberDir=new File("." + File.separator + "Members");
			if(memberDir.exists()) {
				File[] memberArray=memberDir.listFiles();
				for(int i=0 ; i<memberArray.length ; i++) {
					listUsers.add(serverObjectMapper.readValue(memberArray[i], Member.class));
				}
			}
			else {
				memberDir.mkdir();
			}
			File projectDir=new File("." + File.separator + "Projects");
			if(projectDir.exists()) {
				File[] projectArray=projectDir.listFiles();
				for(int j=0 ; j<projectArray.length ; j++) {
					File projectJson=new File(projectArray[j].getAbsoluteFile() + File.separator + "project.json");
					Project tmp=serverObjectMapper.readValue(projectJson, Project.class);
					File cardDir=new File(projectArray[j].getAbsoluteFile() + File.separator + "Cards");
					if(cardDir.exists()) {
						File[] cardArray=cardDir.listFiles();
						for(int k=0 ; k<cardArray.length ; k++) {
							Card card=serverObjectMapper.readValue(cardArray[k], Card.class);
							String status=card.status();
							tmp.jsonToArrayList(card, status);
						}
						listProject.add(tmp);
						generator.addMulticastAddress(tmp.getMulticastAddress());
					}
					else {
						cardDir.mkdir();
					}
				}
			}
			else {
				projectDir.mkdir();	
			}
		} catch(IOException e) {
			running=false;
			System.out.println("errore nel caricamento dei file");
		}
	}
	
	public void startRegister(boolean running) {
		try {
		WORTHServerRegisterImple register=new WORTHServerRegisterImple(listUsers, callBack);
		WORTHServerRegisterInterface stub=(WORTHServerRegisterInterface) UnicastRemoteObject.exportObject(register,0);
		LocateRegistry.createRegistry(30001);
		Registry r=LocateRegistry.getRegistry(30001);
		r.rebind("WORTHServerRegister", stub);
		}
		catch(RemoteException e) {
			running=false;
			System.out.println("errore nel inizializzazione del RMI");
		}
	}
	
	public void startCallBack(boolean running) {
		try {
		WORTHServerCallBackInterface stub=(WORTHServerCallBackInterface) UnicastRemoteObject.exportObject(callBack,0);
		LocateRegistry.createRegistry(30002);
		Registry r=LocateRegistry.getRegistry(30002);
		r.rebind("WORTHServerCallBack", stub);
		}
		catch(RemoteException e) {
			running=false;
			System.out.println("errore nel inizializzazione del RMI call back");
		}
	}
}

