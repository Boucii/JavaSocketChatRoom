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
	public ListView<String> Clients;
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
	
	private ClientThread clientThread;
	public boolean connected=false;
	ObservableList<Object> data = FXCollections.observableArrayList();
	static ObservableList<String> clients = FXCollections.observableArrayList();
	String selected="";//被选中的人
	public void initialize(){
		Clients.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends String> observable, String oldValue, String newValue) ->{
					selected=newValue;
		});//选择了哪一个用户作为聊天对象
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
	} //每一个表情有一个监听器，点击则发送该表情
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
			clientThread = new ClientThread();
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
			if(selected=="GroupChat"){
				msgChat="TALKTO_ALL#"+input.getText();
				addMsg("我说"+input.getText());
			}
			if(selected!=""&&selected!="GroupChat"){
				String toUsername=selected;
				System.out.println(selected);
				msgChat="TALKTO#"+toUsername+"#"+input.getText();
				addMsg("我对"+toUsername+"说"+input.getText());
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
			String msgChat="";
			if(selected=="GroupChat"){
				msgChat="EMOJITO_ALL#"+emo;
				addEmo("我说"+emo);
			}
			if(selected!=""&&selected!="GroupChat"){
				String toUsername=selected;
				System.out.println(selected);
				msgChat="EMOJITO#"+toUsername+"#"+emo;
				addEmo("我对"+toUsername+"说"+emo);
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
				socket.close();
				clients.clear();
				SendButton.setDisable(true);
				LoginButton.setText("Login");
				addMsg("已经退出聊天室");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private void login() throws IOException{
			String serverIp = "127.0.0.1";
			int serverPort = 8000;
			socket = new Socket(serverIp, serverPort);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			String username = Account.getText();
			String msgLogin = "LOGIN#" + username;
			dos.writeUTF(msgLogin);
			dos.flush();
			String response = dis.readUTF();
			if(response.equals("FAIL")) {
				addMsg("登录服务器失败");
				socket.close();
				return;
			}
			if(response.equals("SUCCESS")) {
				addMsg("登录服务器成功");
				clients.add("GroupChat");
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
			try {
				login();
			} catch (IOException e) {
				addMsg("连接登录服务器时出现异常");
				e.printStackTrace();
				return;
			}
			while(isLogged) {
				try {
					String msg = dis.readUTF();
					String[] parts = msg.split("#");
					switch (parts[0]) {
					// 根据数据包头信息，处理服务器发来的不同报文
					case "USERLIST":
						for(int i = 1; i< parts.length; i++) {
							addClient(parts[i]);
						}					
						break;
					case "LOGIN":
						addClient(parts[1]);						
						break;
					case "LOGOUT":
						clients.remove(parts[1]);					
						break;
					case "TALKTO_ALL":
						addMsg(parts[1]+"跟所有人说:"+parts[2]);						
						break;
					case "TALKTO":					
						addMsg(parts[1]+"跟我说:"+parts[2]);
						break;
					case "EMOJITO_ALL":
						addMsg(parts[1]+":");
						addEmo("跟所有人发送了表情，说"+parts[2]);
						break;
					case "EMOJITO":
						addMsg(parts[1]+":");
						addEmo("跟我发送了表情，说"+parts[2]);
						break;
					case "KICKED":
						System.out.println("lcc");
						logout();
						break;
					default:
						break;
					}
				} catch (IOException e) {
					// TODO 处理异常
					isLogged = false;
					e.printStackTrace();
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
		int n1=modified_emo.indexOf("说");
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
            	clients.add(name);
    			Clients.setItems(clients);
            }
        });
	}
}
