import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MulticastAddressGenerator {
	
	private ArrayList<String> usedAddress;
	
	public MulticastAddressGenerator() {
		this.usedAddress=new ArrayList<String>();
	}
	
	public void addMulticastAddress(String MulticastAddress) {
		this.usedAddress.add(MulticastAddress);
	}
	
	public String generateMulticastAddress() {
		String ip="239" + "." + ThreadLocalRandom.current().nextInt(0, 256) + "." + ThreadLocalRandom.current().nextInt(0, 256) + "." + ThreadLocalRandom.current().nextInt(0, 256);
		while(usedAddress.contains(ip)) { 
			ip="239" + "." + ThreadLocalRandom.current().nextInt(0, 256) + "." + ThreadLocalRandom.current().nextInt(0, 256) + "." + ThreadLocalRandom.current().nextInt(0, 256);
		}
		return ip;
	}
}
