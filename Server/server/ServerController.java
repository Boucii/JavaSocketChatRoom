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
			//发送退出信息
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
		String msg="TALKTO_ALL#"+"服务器"+"#"+input.getText();	
		addMsg("服务器说"+input.getText());
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
			// 关闭服务器套接字，清空客户端映射
			server.close();
			clientHandlerMap.clear();
			// 修改按钮状态
			StartButton.setDisable(false);
			StopButton.setDisable(true);
			addMsg("服务器关闭成功");
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
		 * 启动服务
		 */
		private void startServer() {
			try {
				// 创建套接字地址
				SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 8000);
				// 创建ServerSocket，绑定套接字地址
				server = new ServerSocket();
				server.bind(socketAddress);
				// 修改判断服务器是否运行的标识变量
				isRunning = true;
				// 修改启动和停止按钮状态
				StartButton.setDisable(true);
				StopButton.setDisable(false);
				addMsg("服务器启动成功");
			} catch (IOException e) {
				addMsg("服务器启动失败，请检查端口是否被占用");
				e.printStackTrace();
				isRunning = false;
			}
		}

		/**
		 * 线程体
		 */
		@Override
		public void run() {
			startServer();
			// 当服务器处于运行状态时，循环监听客户端的连接请求
			while(isRunning) {
				try {
					Socket socket = server.accept();
					// 创建与客户端交互的线程
					Thread thread = new Thread(new ClientHandler(socket));
					thread.start();
				} catch (IOException e) {
					addMsg("还没连接");
					System.out.println("还没连接");
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
					// 读取客户端发送的报文
					String msg = dis.readUTF();
					String[] parts = msg.split("#");
					switch (parts[0]) {
					// 处理登录报文
					case "LOGIN":
						String loginUsername = parts[1];
						// 如果该用户名已登录，则返回失败报文，否则返回成功报文
						if(clientHandlerMap.containsKey(loginUsername)) {
							dos.writeUTF("FAIL");
						} else {
							
							dos.writeUTF("SUCCESS");
							addClient(loginUsername);
							// 将此客户端处理线程的信息添加到clientHandlerMap中
							clientHandlerMap.put(loginUsername, this);
							// 将现有用户的信息发给新用户
							StringBuffer msgUserList = new StringBuffer();
							msgUserList.append("USERLIST#");
							for(String username : clientHandlerMap.keySet()) {

								msgUserList.append(username + "#");
							}
							dos.writeUTF(msgUserList.toString());
							// 将新登录的用户信息广播给其他用户
							String msgLogin = "LOGIN#" + loginUsername;
							broadcastMsg(loginUsername, msgLogin);
							// 存储登录的用户名
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
						addMsg(username+"说"+parts[1]);
						broadcastMsg(username, msgTalkToAll);
						break;
					case "TALKTO":
						
						ClientHandler clientHandler=clientHandlerMap.get(parts[1]);
						if(null!=clientHandler){
							String msgTalkTo="TALKTO#"+username+"#"+parts[2];
							addMsg(username+"对"+parts[1]+"私聊说"+parts[2]);
							clientHandler.dos.writeUTF(msgTalkTo);
							clientHandler.dos.flush();
						}
						break;
					case "EMOJITO_ALL":
						String emoTalkToAll="EMOJITO_ALL#"+username+"#"+parts[1];
						addMsg(username+":");
						addEmo("跟所有人发送了表情，说"+parts[1]);
						broadcastMsg(username, emoTalkToAll);
						break;
					case "EMOJITO":
						ClientHandler clientHandler2=clientHandlerMap.get(parts[1]);
						if(null!=clientHandler2){
							String msgTalkTo="EMOJITO#"+username+"#"+parts[2];
							//addMsg(username+"对"+parts[1]+"私聊说"+parts[2]);
							addMsg(username+"对"+parts[1]+"发送了表情，");
							addEmo("说"+parts[2]);
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
		 * 将某个用户发来的消息广播给其它用户
		 * @param fromUsername 发来消息的用户
		 * @param msg 需要广播的消息
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
	 * 添加消息到文本框textAreaRecord
	 * @param msg，要添加的消息
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
