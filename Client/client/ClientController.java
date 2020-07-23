package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;


public class ClientController {
	@FXML
	private AnchorPane BasePane;
	@FXML
	private AnchorPane Base;
	@FXML
	private AnchorPane emojis;
	@FXML
	private AnchorPane LoginPane;
	@FXML
	private Button LoginButton;
	@FXML
	private TextField Account;	 
	@FXML
	private Button QuitButton;
	@FXML
	private ImageView QuitButton1;
	@FXML
	private ListView<Object> ChatList;
	@FXML
	public ListView<Object> Clients;
	@FXML
	private Button SendButton;
	@FXML 
	public TextArea input;
	@FXML
	private ImageView happy;
	@FXML
	private ImageView confuse;
	@FXML
	private ImageView fat;
	@FXML
	private ImageView shock;
	@FXML
	private Button EmojiButton;
	
	private ClientThread clientThread;
	public boolean connected=false;
	int chosen=0;
	ObservableList<Object> data = FXCollections.observableArrayList();
	static ObservableList<Object> clients = FXCollections.observableArrayList();
	String selected="";//��ѡ�е���
	public void initialize(){
		EmojiButton.setDisable(true);
		SendButton.setDisable(true);
		Clients.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) ->{
					HBox temp=(HBox)newValue;
					Label l=(Label)temp.getChildren().get(1);
					selected=l.getText();
		});
		class MyEventHandler implements EventHandler<Event>{
	        @Override
	        public void handle(Event event) {
	        	String id=event.getSource().toString();
	        	Base.setDisable(false);
	    		emojis.setDisable(true);
	    		emojis.setVisible(false);
	        	clientThread.sendEmoji(id);	
	        	
	        }
	    }
		fat.addEventHandler(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
		shock.addEventHandler(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
		happy.addEventHandler(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
		confuse.addEventHandler(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
	} 
	public void OpenEmoji() {
		Base.setDisable(true);
		emojis.setDisable(false);
		emojis.setVisible(true);
	}
	public void CloseEmoji() {
		Base.setDisable(false);
		emojis.setDisable(true);
		emojis.setVisible(false);
	}
	public void Send() {
		if(null!=clientThread)
			clientThread.sendChatMag();
	}
	public void Quit() {
		if(connected==true) {
			clientThread.logout();
			System.exit(0);
		}else {
			System.exit(0);
		}
	}
	public void HoverQuit() {
		Image image=new Image("file:..\\..\\Assets\\icons\\quit.png");
		this.QuitButton1.setImage(image);
	}
	public void UnHoverQuit() {
		Image image=new Image("file:..\\..\\Assets\\icons\\quit2.png");
		this.QuitButton1.setImage(image);
	}
	public void login() {
		if (connected==false) {
			System.out.println("aaaaddsfsdfsdʿ���11");
			clientThread = new ClientThread();
			System.out.println("aaaaddsfsdfsdʿ���22");
			clientThread.start();
		} else {
			clientThread.logout();
		}
	}
	class ClientThread extends Thread {
		private  Socket socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private boolean isLogged;
		void sendChatMag()
		{
			String msgChat="";
			String ms=input.getText();
			if(ms.indexOf('#')!=-1) {
				input.appendText("dont input #");
				return;
			}
			if(selected=="GroupChat"){
				msgChat="TALKTO_ALL#"+input.getText();
				addMsg("��˵"+input.getText());
			}
			if(selected!=""&&selected!="GroupChat"){
				String toUsername=selected;
				System.out.println(selected);
				msgChat="TALKTO#"+toUsername+"#"+input.getText();
				addMsg("�Ҷ�"+toUsername+"˵"+input.getText());
			}
			if(null!=msgChat){
				try {
					dos.writeUTF(msgChat);
					dos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		void sendEmoji(String emo) {
			int n1=emo.indexOf('=');
			int n2=emo.indexOf(',');
			emo=emo.substring(n1+1,n2);
			System.out.println("iii"+emo);
			String msgChat="";
			if(selected=="GroupChat"){
				msgChat="EMOJITO_ALL#"+emo;
				addEmo("��˵"+emo);
			}
			if(selected!=""&&selected!="GroupChat"){
				String toUsername=selected;
				System.out.println(selected);
				msgChat="EMOJITO#"+toUsername+"#"+emo;
				addEmo("�Ҷ�"+toUsername+"˵"+emo);
			}
			if(emo!=null){
				try {
					dos.writeUTF(msgChat);
					dos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		void logout()
		{
			try {
				String msgLogout="LOGOUT";
				dos.writeUTF(msgLogout);
				dos.flush();
				isLogged=false;
				connected=false;
				socket.close();
				
				SendButton.setDisable(true);
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	clients.clear();
		            	LoginButton.setText("Login");
		            }
		        });						
				addMsg("�Ѿ��˳�������");
				EmojiButton.setDisable(true);
				SendButton.setDisable(true);
				socket=null;
				System.out.println("aaaadasf");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void login() throws IOException{			
			// ��ȡ������IP��ַ�Ͷ˿�
			String serverIp = "127.0.0.1";
			int serverPort = 8000;
			// ���ӷ���������ȡ�׽���IO��
			System.out.println("aaaaddsfsdfsdʿ���");
			socket = new Socket(serverIp, serverPort);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			// ��ȡ�û��������������͵�¼����
			String username = Account.getText();
			if(username.indexOf('#')!=-1) {
				Account.appendText("dont input #");
				return;
			}
			String msgLogin = "LOGIN#" + username;
			dos.writeUTF(msgLogin);
			dos.flush();
			// ��ȡ���������ص���Ϣ���ж��Ƿ��¼�ɹ�
			String response = dis.readUTF();
			// ��¼ʧ��
			if(response.equals("FAIL")) {
				addMsg("��¼������ʧ��");
				// ��¼ʧ�ܣ��Ͽ����ӣ������ͻ����߳�
				socket.close();
				return;
			}
			// ��¼�ɹ�
			if(response.equals("SUCCESS")) {
				addMsg("��¼�������ɹ�");
				EmojiButton.setDisable(false);
				SendButton.setDisable(false);
				addClient("GroupChat");
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	LoginButton.setText("Logout");
		            }
		        });			
				isLogged = true;
				SendButton.setDisable(false);
				connected=true;
			}
		}

		@Override
		public void run() {
			// ���ӷ���������¼
			try {
				login();
			} catch (IOException e) {
				addMsg("���ӵ�¼������ʱ�����쳣");
				e.printStackTrace();
				return;
			}
			while(isLogged) {
				try {
					String msg = dis.readUTF();
					String[] parts = msg.split("#");
					switch (parts[0]) {
					// ����������������û��б���
					case "USERLIST":
						for(int i = 1; i< parts.length; i++) {
							addClient(parts[i]);
						}					
						break;
					// ������������������û���¼����
					case "LOGIN":
						addClient(parts[1]);						
						break;
					case "LOGOUT":
						deleteClient(parts[1]);
						break;
					case "TALKTO_ALL":
						addMsg(parts[1]+"��������˵:"+parts[2]);						
						break;
					case "TALKTO":					
						addMsg(parts[1]+"����˵:"+parts[2]);
						break;
					case "EMOJITO_ALL":
						addMsg(parts[1]+":");
						addEmo("�������˷����˱��飬˵"+parts[2]);
						break;
					case "EMOJITO":
						addMsg(parts[1]+":");
						addEmo("���ҷ����˱��飬˵"+parts[2]);
						break;
					case "KICKED":
						
						logout();
						break;
					default:
						break;
					}
				} catch (Exception e) {
					// TODO �����쳣
					System.out.println("dsfsdf666");
					isLogged = false;
					connected=false;
					e.printStackTrace();
					return;
				}
			}
		}
		
	}
	private void addMsg(String msg) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	data.add(msg);
    			ChatList.setItems(data);
            }
        });
	}
	private void addEmo(String modified_emo) {
		int n1=modified_emo.indexOf("˵");
		String pre=modified_emo.substring(0,n1+1);
		String name=modified_emo.substring(n1+1);
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	HBox hb=new HBox();
            	Image im=new Image("file:..\\..\\Assets\\emojis\\"+name+".png");
            	ImageView view=new ImageView(im);
            	Label l=new Label(pre);
            	hb.getChildren().addAll(l,view);
            	data.addAll(hb);
            	ChatList.setItems(data);
            }
        });
	}
	private void addClient(String name) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	HBox hb=new HBox();           	
            	Image im=new Image("file:..\\..\\Assets\\ProfilePto\\"+Integer.toString(chosen)+".png");
            	chosen=(chosen+1)%6;
            	if(name=="GroupChat") {
            		im=new Image("file:..\\..\\Assets\\ProfilePto\\"+Integer.toString(-1)+".png");
            		chosen=0;
            	}
            	ImageView view=new ImageView(im);
            	view.setFitHeight(40);
            	view.setFitWidth(40);
            	view.setPreserveRatio(true);
            	Label l=new Label(name);
            	l.setStyle("-fx-font-size: 30;");
            	hb.getChildren().addAll(view,l);
            	clients.addAll(hb);
            	Clients.setItems(clients);
            }
        });
	}
	private void deleteClient(String name) {
		System.out.println(Account.getText()+"   "+name);
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	HBox temp;
            	for(int i=0;i<clients.size();i++) {
        			temp=(HBox)clients.get(i);
        			String nameTemp=((Label)temp.getChildren().get(1)).getText();
        			if(name.equals(nameTemp)) {
        				clients.remove(temp);
        				Clients.setItems(clients);
        			}
        		}
            }
        });
	}
}
