public class ChatHistory {
	
	private String chat;
	private String ip;
	
	public ChatHistory(String ip) {
		this.chat="";
		this.ip=ip;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public synchronized void readChat() {
		System.out.print(this.chat);
		this.chat="";
		return;
	}
	
	public synchronized void addChat(String message) {
		this.chat=this.chat.concat(message + System.lineSeparator());
		return;
	}
}
