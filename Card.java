public class Card {
	
	private String cardName;
	private String description;
	private String history;
	//usato per il json
	public Card() { 
	}
	//usato per creare Card temporanei per il confronto
	public Card(String cardName) {
		this.cardName=cardName;
	}
	
	public Card(String cardName, String description) {
		this.cardName=cardName;
		this.description=description;
		this.history=("TODO");
	}
	
	public Card addHistory(String newState) {
		this.history=this.history.concat("-"+ newState);
		return this;
	}
	
	public String status() {
		String[] history=this.history.split("-");
		return history[history.length-1];
	}
	@Override //metodo usato per il confronto tra le card nel ArrayList
	public boolean equals(Object Card) {
		Card tmp = (Card)Card;
		return this.cardName.equals(tmp.getCardName());
	}
	@Override 
	public String toString() {
		return "cardName:"+ this.cardName + System.lineSeparator() + "description:" + this.description + System.lineSeparator();
	}
	//metodi get e set di cardName, description, history
	public String getCardName() {
		return this.cardName;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getHistory() {
		return this.history;
	}
	
	public void setCardName(String cardName) {
		this.cardName=cardName;
	}
	
	public void setDescription(String description) {
		this.description=description;
	}
	
	public void setHistory(String history) {
		this.history=history;
	}
}
