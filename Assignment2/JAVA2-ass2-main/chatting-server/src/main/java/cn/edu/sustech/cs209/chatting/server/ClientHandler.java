//package cn.edu.sustech.cs209.chatting.server;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//
//public class ClientHandler implements Runnable {
//  private Socket clientSocket;
//
//  public ClientHandler(Socket socket) {
//    this.clientSocket = socket;
//  }
//
//  @Override
//  public void run() {
//    try {
//      BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//      PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
//
//      String inputLine;
//      while ((inputLine = input.readLine()) != null) {
//        String[] parts = inputLine.split(" ", 2);
//        String command = parts[0];
//        String message = parts.length > 1 ? parts[1] : "";
//
//        switch (command) {
//          case "SEND":
//            // 处理发送消息请求
//            // 将消息广播给所有在线用户
//            Server.broadcastMessage(message);
//            break;
//
//          case "GET_USER":
//            // 处理获取用户名请求
//            String userName = Server.getUserName(clientSocket);
//            if (userName != null) {
//              output.println("USER " + userName);
//            } else {
//              output.println("ERROR User not found");
//            }
//            break;
//
//          case "GET_ONLINE_USERS":
//            // 处理获取在线用户列表请求
//            String onlineUsers = Server.getOnlineUsers();
//            output.println("ONLINE_USERS " + onlineUsers);
//            break;
//
//          default:
//            output.println("ERROR Unknown command: " + command);
//            break;
//        }
//      }
//
//      input.close();
//      output.close();
//      clientSocket.close();
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//}
