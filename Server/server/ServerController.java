package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.Gson;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ServerController {
	@FXML
	private Button QuitButton;
	@FXML
	private ImageView StartButton;
	@FXML
	private ImageView StopButton;
	@FXML
	private ListView<Object> ChatList;
	@FXML
	public ListView<String> Clients;
	@FXML
	private Button KickButton;
	@FXML
	private Button SendButton;
	@FXML 
	private TextArea input;
	private ServerSocket server;
	private boolean isRunning=false;
	String selected="";
	ObservableList<Object> data = FXCollections.observableArrayList();
	static ObservableList<String> clients = FXCollections.observableArrayList();
	public HashMap<String, ClientHandler> clientHandlerMap = new HashMap<String, ClientHandler>();
	public void initialize(){
		KickButton.setDisable(true);
		SendButton.setDisable(true);
		Clients.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends String> observable, String oldValue, String newValue) ->{
					selected=newValue;
		});
	} 
	public void kick() {
		try {
		ClientHandler temp=clientHandlerMap.get(selected);
		if (temp!=null) {
			System.out.println("kick");
			String msgTalkTo="KICKED#";
			temp.dos.writeUTF(msgTalkTo);
			temp.dos.flush();
			clients.remove(selected);
			Clients.setItems(clients);
			//�����˳���Ϣ
		}}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void EnterStart2() {
		Image image=new Image("file:..\\..\\Assets\\icons\\stop2.png");
		StopButton.setImage(image);
	}
	public void ExitStart2() {
		Image image=new Image("file:..\\..\\Assets\\icons\\stop.png");
		StopButton.setImage(image);
	}
	public void EnterStart() {
		Image image=new Image("file:..\\..\\Assets\\icons\\play2.png");
		this.StartButton.setImage(image);
	}
	public void ExitStart() {
		Image image=new Image("file:..\\..\\Assets\\icons\\play.png");
		this.StartButton.setImage(image);
	}
	public void Quit() {
		StopServer();
		System.exit(0);
	}
	public void Send() {
		try {
			String ms=input.getText();
			if(ms.indexOf('#')!=-1) {
				input.appendText("dont input #");
				return;
			}
		String msg="TALKTO_ALL#"+"������"+"#"+input.getText();	
		addMsg("������˵"+input.getText());
		for(String toUserName : clientHandlerMap.keySet()) {
				DataOutputStream dos = clientHandlerMap.get(toUserName).dos;
				dos.writeUTF(msg);
				dos.flush();
			
		}}catch(Exception e) {
			
		}
	}
	public void StopServer() {
		KickButton.setDisable(true);
		SendButton.setDisable(true);
		if(isRunning==true) {
		try {
			isRunning = false;
			// �رշ������׽��֣���տͻ���ӳ��
			server.close();
			clientHandlerMap.clear();
			// �޸İ�ť״̬
			StartButton.setDisable(false);
			StopButton.setDisable(true);
			addMsg("�������رճɹ�");
		} catch (IOException e) {
			e.printStackTrace();
		}}else {
			return;
		}
	}
	public void StartServer() {
		KickButton.setDisable(false);
		SendButton.setDisable(false);
		Thread serverThread = new Thread(new ServerThread());
		serverThread.start();
	}
	
	class ServerThread implements Runnable {	
		/**
		 * ��������
		 */
		private void startServer() {
			try {
				// �����׽��ֵ�ַ
				SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 8000);
				// ����ServerSocket�����׽��ֵ�ַ
				server = new ServerSocket();
				server.bind(socketAddress);
				// �޸��жϷ������Ƿ����еı�ʶ����
				isRunning = true;
				// �޸�������ֹͣ��ť״̬
				StartButton.setDisable(true);
				StopButton.setDisable(false);
				addMsg("�����������ɹ�");
			} catch (IOException e) {
				addMsg("����������ʧ�ܣ�����˿��Ƿ�ռ��");
				e.printStackTrace();
				isRunning = false;
			}
		}

		/**
		 * �߳���
		 */
		@Override
		public void run() {
			startServer();
			// ����������������״̬ʱ��ѭ�������ͻ��˵���������
			while(isRunning) {
				try {
					Socket socket = server.accept();
					// ������ͻ��˽������߳�
					Thread thread = new Thread(new ClientHandler(socket));
					thread.start();
				} catch (IOException e) {
					addMsg("��û����");
					System.out.println("��û����");
				}
			}
		}
		
	}
	
	class ClientHandler implements Runnable {
		private Socket socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private boolean isConnected;
		private String username;
		
		public ClientHandler(Socket socket) {
			this.socket = socket;
			try {
			
				this.dis = new DataInputStream(socket.getInputStream());
				this.dos = new DataOutputStream(socket.getOutputStream());
				isConnected = true;
			} catch (IOException e) {
				isConnected = false;
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while(isRunning && isConnected) {
				try {
					// ��ȡ�ͻ��˷��͵ı���
					String msg = dis.readUTF();
					String[] parts = msg.split("#");
					switch (parts[0]) {
					// �����¼����
					case "LOGIN":
						String loginUsername = parts[1];
						// ������û����ѵ�¼���򷵻�ʧ�ܱ��ģ����򷵻سɹ�����
						if(clientHandlerMap.containsKey(loginUsername)) {
							dos.writeUTF("FAIL");
						} else {
							
							dos.writeUTF("SUCCESS");
							addClient(loginUsername);
							// ���˿ͻ��˴����̵߳���Ϣ��ӵ�clientHandlerMap��
							clientHandlerMap.put(loginUsername, this);
							// �������û�����Ϣ�������û�
							StringBuffer msgUserList = new StringBuffer();
							msgUserList.append("USERLIST#");
							for(String username : clientHandlerMap.keySet()) {

								msgUserList.append(username + "#");
							}
							dos.writeUTF(msgUserList.toString());
							// ���µ�¼���û���Ϣ�㲥�������û�
							String msgLogin = "LOGIN#" + loginUsername;
							broadcastMsg(loginUsername, msgLogin);
							// �洢��¼���û���
							this.username = loginUsername;
						}
						break;
					case "LOGOUT":
						clientHandlerMap.remove(username);
						//clients.remove(username);
						//Clients.setItems(clients);
						deleteClient(username);
						String msgLogout="LOGOUT#"+username;
						broadcastMsg(username, msgLogout);
						isConnected=false;
						socket.close();
						break;
					case "TALKTO_ALL":			
						String msgTalkToAll="TALKTO_ALL#"+username+"#"+parts[1];
						addMsg(username+"˵"+parts[1]);
						broadcastMsg(username, msgTalkToAll);
						break;
					case "TALKTO":
						
						ClientHandler clientHandler=clientHandlerMap.get(parts[1]);
						if(null!=clientHandler){
							String msgTalkTo="TALKTO#"+username+"#"+parts[2];
							addMsg(username+"��"+parts[1]+"˽��˵"+parts[2]);
							clientHandler.dos.writeUTF(msgTalkTo);
							clientHandler.dos.flush();
						}
						break;
					case "EMOJITO_ALL":
						String emoTalkToAll="EMOJITO_ALL#"+username+"#"+parts[1];
						addMsg(username+":");
						addEmo("�������˷����˱��飬˵"+parts[1]);
						broadcastMsg(username, emoTalkToAll);
						break;
					case "EMOJITO":
						ClientHandler clientHandler2=clientHandlerMap.get(parts[1]);
						if(null!=clientHandler2){
							String msgTalkTo="EMOJITO#"+username+"#"+parts[2];
							//addMsg(username+"��"+parts[1]+"˽��˵"+parts[2]);
							addMsg(username+"��"+parts[1]+"�����˱��飬");
							addEmo("˵"+parts[2]);
							clientHandler2.dos.writeUTF(msgTalkTo);
							clientHandler2.dos.flush();
						}
						break;
					default:
						break;
					}
				} catch (IOException e) {
					isConnected = false;
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * ��ĳ���û���������Ϣ�㲥�������û�
		 * @param fromUsername ������Ϣ���û�
		 * @param msg ��Ҫ�㲥����Ϣ
		 */
		private void broadcastMsg(String fromUsername, String msg) throws IOException{
			for(String toUserName : clientHandlerMap.keySet()) {
				if(fromUsername.equals(toUserName) == false) {
					DataOutputStream dos = clientHandlerMap.get(toUserName).dos;
					dos.writeUTF(msg);
					dos.flush();
				}
			}
		}
		public void disconnect() {
			try {
			//clientHandlerMap.remove(username);
			//clients.remove(username);
			deleteClient(username);
			String msgLogout="LOGOUT#"+username;
			addMsg(msgLogout);
			broadcastMsg(username, msgLogout);
			isConnected=false;
			socket.close();}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �����Ϣ���ı���textAreaRecord
	 * @param msg��Ҫ��ӵ���Ϣ
	 */
	private void addMsg(String msg) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	data.add(msg);
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

	private void deleteClient(String name) {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	clients.remove(name);
    			Clients.setItems(clients);
            }
        });
	}

}
