public class ClientMain {

	public static void main(String[] args)  {
		WORTHclient client=new WORTHclient();
		Thread t=new Thread(client);
		t.start();
	}
}
