//package cn.edu.sustech.cs209.chatting.client;
//
//import cn.edu.sustech.cs209.chatting.client.Controller.MessageCellFactory;
//import cn.edu.sustech.cs209.chatting.common.Message;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.ListView;
//
//public class Client {
//  @FXML
//  ListView<Message> chatContentList;
//  private Socket socket;
//  private BufferedReader input;
//  private PrintWriter output;
//  List<String> onlineUsers = new ArrayList<>();
//  public Client(String host, int port,String username) {
//    try {
//      // 创建 socket 并连接到服务器
//      socket = new Socket(host, port);
//      // 获取输入流和输出流
//      input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//      output = new PrintWriter(socket.getOutputStream(), true);
//      output.println(username);
//    } catch (IOException e) {
//      System.err.println("Error connecting to server:," + e.getMessage());
//      System.exit(1);
//    }
//  }
//  public List<String> getOnlineUsers() {
//    // 向服务器发送获取在线用户列表的请求
//    output.println("GET_ONLINE_USERS");
//
//
//
//    try {
//      // 读取服务器返回的响应，即在线用户列表
//      String response = input.readLine();
//      // 假设服务器返回的在线用户列表是以逗号分隔的字符串
//      String userListString = response.substring(1, response.length() - 1);
//      String[] users = userListString.split(",");
//      // 将每个在线用户添加到 onlineUsers 列表中
//      for (String user : users) {
//        onlineUsers.add(user.trim());
//      }
//    } catch (IOException e) {
//      System.err.println("Error communicating with server: " + e.getMessage());
//    }
//    return onlineUsers;
//  }
//
//  public void sendMessage(Message message) {
//    output.println("SEND"+","+message.getTimestamp().toString()+","+message.getSendTo()+","+message.getSentBy()+","+message.getData());
//
//  }
//
//
//  public void start() {
//    try {
//      // 从控制台读取用户输入
//      BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
//      String line;
//      while ((line = consoleInput.readLine()) != null) {
//        String[] parts = line.split(",");
//        String command = parts[0];
//        String message = parts.length > 1 ? parts[1] : "";
//        switch (command){
//          case "USER_JOINED":
//            onlineUsers.add(message);
//            break;
//          case "MESSAGE":
//            Long Timestamp=Long.parseLong(parts[1]);
//            String Sendto=parts[2];
//            String SendBy=parts[3];
//            String Data=parts[4];
//            Message msg=new Message(Timestamp,SendBy,Sendto,Data,"MESSAGE");
//            Platform.runLater(() -> {
//              chatContentList.getItems().add(msg);
//            });
//            chatContentList.setCellFactory(new MessageCellFactory());
//            break;
//          default:
//            break;
//        }
//
//      }
//    } catch (IOException e) {
//      System.err.println("Error communicating with server:," + e.getMessage());
//    } finally {
//      // 关闭 socket 和输入输出流
//      try {
//        socket.close();
//        input.close();
//        output.close();
//      } catch (IOException e) {
//        System.err.println("Error closing socket:," + e.getMessage());
//      }
//    }
//  }
//
//}
