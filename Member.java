import com.fasterxml.jackson.annotation.JsonIgnore;

public class Member {
	
	private String nickName;
	private String password;
	@JsonIgnore
	private String status;
	
	public Member() { //usato per il json
		this.status="offline";
	}
	
	public Member(String nickName, String password) {
		this.nickName=nickName;
		this.password=password;
		this.status="offline";
	}
	
	public Member(String nickName, String password, String status) {
		this.nickName=nickName;
		this.password=password;
		this.status=status;
	}
	
	public Member(String nickName) { //usato per il confontro equals
		this.nickName=nickName;
	}
	
	public void goOnline() {
		this.status="online";
	}
	
	public void goOffline() {
		this.status="offline";
	}
	
	public void changeStatus(String status) {
		this.status=status;
	}
	
	public boolean equals(Object member) {
		Member tmp = (Member)member;
		return this.nickName.equals(tmp.getNickName());
	}
	//metodi get e set
	public String getNickName() {
		return this.nickName;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setNickName(String nickName) {
		this.nickName=nickName;
	}
	
	public void setPassword(String password) {
		this.password=password;
	}
}
