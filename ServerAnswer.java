public class ServerAnswer {
	
	private String nickName;
	private String answer;
	
	public ServerAnswer(String nickName, String answer) {
		this.nickName=nickName;
		this.answer=answer;
	}
	
	public String getNickName() {
		return this.nickName;
	}
	
	public String getAnswer() {
		return this.answer;
	}
	
	public void setAnswer(String answer) {
		this.answer=answer;
	}
	
	public void setNickName(String nickName) {
		this.nickName=nickName;
	}
}
