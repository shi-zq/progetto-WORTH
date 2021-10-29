import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Project {
	
	private String multicastAddress; 
	private String projectName;
	private ArrayList<String> members;
	@JsonIgnore
	private ArrayList<Card> todo;
	@JsonIgnore
	private ArrayList<Card> inprogress;
	@JsonIgnore
	private ArrayList<Card> toberevised;
	@JsonIgnore
	private ArrayList<Card> done;
	@JsonIgnore
	private ObjectMapper projectObjectMapper;
	
	public Project() { //usato per json
		this.todo=new ArrayList<Card>();
		this.inprogress=new ArrayList<Card>();
		this.toberevised=new ArrayList<Card>();
		this.done=new ArrayList<Card>();
		this.projectObjectMapper=new ObjectMapper();
	}
	
	public Project(String projectName, String member, String multicastAddress) {
		this.projectName=projectName;
		this.members=new ArrayList<String>();
		this.members.add(member);
		this.todo=new ArrayList<Card>();
		this.inprogress=new ArrayList<Card>();
		this.toberevised=new ArrayList<Card>();
		this.done=new ArrayList<Card>();
		this.multicastAddress=multicastAddress;
		this.projectObjectMapper=new ObjectMapper();
	}
	//usato solo per creare progetti temporanei per il confronto
	public Project(String projectName) {
		this.projectName=projectName;
	}
	
	public boolean checkmember(String member) {
		return members.contains(member);
	}
	
	public String getAllMembers(String member) {
		if(checkmember(member)) {
			Iterator<String> iterator=members.iterator();
			String tmp=iterator.next(); //sicuramente ha un membro, cioe il creatore
			while(iterator.hasNext()) {
				tmp=tmp.concat(System.lineSeparator() + iterator.next());
			}
			return tmp;
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String addmember(String member, String guest) {
		if(checkmember(member)) {
			if(checkmember(guest)) {
				return guest + " appartiene a questo progetto";
			}
			else {
				members.add(guest);
				try {
					File projectJson=new File("." + File.separator + "Projects" + File.separator + this.projectName + File.separator + "project.json");
					projectObjectMapper.writeValue(projectJson, this);
				}
				catch(IOException e) {
					System.out.println("Project.addMember-errore nel salvare in json");
				}
				return "OK";
			}
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String addCard(String cardName, String description, String member) {
		if(checkmember(member)) {
			Card tmp=new Card(cardName, description);
			if(todo.contains(tmp) || inprogress.contains(tmp) || toberevised.contains(tmp) || done.contains(tmp)) {
				return tmp.getCardName() + " carta appartiene a questo progetto";
			}
			else {
				todo.add(tmp);
				try {
					File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
					cardJson.createNewFile();
					projectObjectMapper.writeValue(cardJson, tmp);
				}
				catch(IOException e) {
					System.out.println("Project.addCard-errore nel salvare in json");
				}
				return "OK";
			}
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String moveCard(String cardName, String source, String destination, String member) {
		if(checkmember(member)) {
			Card tmp=new Card(cardName);
			int index;
			Card tmpJson;
			switch(source) {
				case("TODO"):
					index=todo.indexOf(tmp);
					if(index==-1) {
						return tmp.getCardName() + " non appartiene in questa lista";
					}
					else {
						if(destination.equals("INPROGRESS")) {
							tmpJson=todo.remove(index);
							tmpJson.addHistory("INPROGRESS");
							inprogress.add(tmpJson);
							try {
								File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
								projectObjectMapper.writeValue(cardJson, tmpJson);
							}
							catch(IOException e) {
								System.out.println("Project.moveCard-errore nel salvare in json");
							}
							return "OK";
						}
						else {
							return "operazione non permessa da " + source + " a " + destination;
						}
					}
				case("INPROGRESS"):
					index=inprogress.indexOf(tmp);
					if(index==-1) {
						return tmp.getCardName() + " non appartiene in questa lista";
					}
					else {
						switch(destination) {
							case("TOBEREVISED"):
								tmpJson=inprogress.remove(index);
								tmpJson.addHistory("TOBEREVISED");
								toberevised.add(tmpJson);
								try {
									File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
									projectObjectMapper.writeValue(cardJson, tmpJson);
								}
								catch(IOException e) {
									System.out.println("Project.moveCard-errore nel salvare in json");
								}
								return "OK";
							case("DONE"):
								tmpJson=inprogress.remove(index);
								tmpJson.addHistory("DONE");
								done.add(tmpJson);
								try {
									File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
									projectObjectMapper.writeValue(cardJson, tmpJson);
								}
								catch(IOException e) {
									System.out.println("Project.moveCard-errore nel salvare in json");
								}
								return "OK";
							default:
								return " operazione non permessa da " + source + " a " + destination;
						}
					}
				case("TOBEREVISED"):
					index=toberevised.indexOf(tmp);
					if(index==-1) {
						return tmp.getCardName() + " non appartiene in questa lista";
					}
					else {
						switch(destination) {
						case("INPROGRESS"):
							tmpJson=toberevised.remove(index);
							tmpJson.addHistory("INPROGRESS");
							inprogress.add(tmpJson);
							try {
								File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
								projectObjectMapper.writeValue(cardJson, tmp);
							}
							catch(IOException e) {
								System.out.println("Project.moveCard-errore nel salvare in json");
							}
							return "OK";
						case("DONE"):
							tmpJson=toberevised.remove(index);
							tmpJson.addHistory("DONE");
							done.add(tmpJson);
							try {
								File cardJson=new File("."  + File.separator + "Projects" + File.separator + this.projectName + File.separator + "Cards" + File.separator + tmp.getCardName() + ".json");
								projectObjectMapper.writeValue(cardJson, tmp);
							}
							catch(IOException e) {
								System.out.println("Project.moveCard-errore nel salvare in json");
							}
							return "OK";
						default:
							return "operazione non permessa da " + source + " a " + destination;
						}
					}
				default:
					return "operazione non permessa da " + source + " a " + destination;
			}				
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String getCard(String cardName, String member) {
		if(checkmember(member)) {
			Card tmp=new Card(cardName);
			int index=todo.indexOf(tmp);
			if(index!=-1) {
				return todo.get(index).toString() + "Stato:TODO";
			}
			index=inprogress.indexOf(tmp);
			if(index!=-1) {
				return inprogress.get(index).toString() + "Stato:INPROGRESS";
			}
			index=toberevised.indexOf(tmp);
			if(index!=-1) {
				return toberevised.get(index).toString() + "Stato:TOBEREVISED";
			}
			index=done.indexOf(tmp);
			if(index!=-1) {
				return done.get(index).toString() + "Stato:DONE";
			}
			return tmp.getCardName() + " non appartiene in questa lista";
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String getAllCards(String member) {
		if(checkmember(member)) {
			String answer="TODO" + System.lineSeparator();
			Iterator<Card> iterator=todo.iterator();
			while(iterator.hasNext()) {
				answer=answer.concat(iterator.next().getCardName() + System.lineSeparator());
			}
			iterator=inprogress.iterator();
			answer=answer.concat("INPROGRESS" + System.lineSeparator());
			while(iterator.hasNext()) {
				answer=answer.concat(iterator.next().getCardName() + System.lineSeparator());
			}
			iterator=toberevised.iterator();
			answer=answer.concat("TOBEREVISED" + System.lineSeparator());
			while(iterator.hasNext()) {
				answer=answer.concat(iterator.next().getCardName() + System.lineSeparator());
			}
			iterator=done.iterator();
			answer=answer.concat("DONE");
			while(iterator.hasNext()) {
				answer=answer.concat(System.lineSeparator() + iterator.next().getCardName());
			}
			return answer;
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String getCardHistory(String CardName, String member) {
		if(checkmember(member)) {
			Card tmp=new Card(CardName);
			int index=todo.indexOf(tmp);
			if(index!=-1) {
				return todo.get(index).getHistory();
			}
			index=inprogress.indexOf(tmp);
			if(index!=-1) {
				return inprogress.get(index).getHistory();
			}
			index=toberevised.indexOf(tmp);
			if(index!=-1) {
				return toberevised.get(index).getHistory();
			}
			index=done.indexOf(tmp);
			if(index!=-1) {
				return done.get(index).getHistory();
			}
			return tmp.getCardName() + " non appartiene in questa lista";
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	
	public String cancelProject(String member) {
		if(checkmember(member)) {
			if(todo.size()==0 && inprogress.size()==0 && toberevised.size()==0) {
				return "OK";
			}
			else {
				return "le liste di TODO, INPROGRESS, TOBEREVISED non sono vuoti";
			}
		}
		else {
			return member + " non appartiene in questa lista";
		}
	}
	//metodo usato per ricostruire le ArrayList dalle card
	public void jsonToArrayList(Card card, String position) { 
		switch(position) {
			case("TODO"):
				todo.add(card);
				break;
			case("INPROGRESS"):
				inprogress.add(card);
				break;
			case("TOBEREVISED"):
				toberevised.add(card);
				break;
			case("DONE"):
				done.add(card);
				break;
		}
	}
	
	public boolean equals(Object project) {
		Project tmp = (Project)project;
		return this.projectName.equals(tmp.getProjectName());
	}
	//metodi set e get di multicastAddress, projectName, members, 
	public String getMulticastAddress() {
		return this.multicastAddress;
	}
	
	public String getProjectName() {
		return this.projectName;
	}
	
	public ArrayList<String> getmembers() {
		return this.members;
	}
	
	public void setMulticastAddress(String multicastAddress) {
		this.multicastAddress=multicastAddress;
	}
	
	public void setProjectName(String ProjectName) {
		this.projectName=ProjectName;
	}
	
	public void setmembers(ArrayList<String> members) {
		this.members=members;
	}

}
