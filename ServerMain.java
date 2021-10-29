public class ServerMain {
	
	public static void main(String[] args)  {
		WORTHServer server = new WORTHServer();
		Thread threadserver=new Thread(server);
		threadserver.start();
	}
}
