package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server implements Runnable {
    public static final int PORT = 8002;
    private static Set<PrintWriter> clients = new HashSet<>();

    private static Map<String, List<String>> unreadMessages = new HashMap<>();
    private static Map<String, List<String>> readMessages = new HashMap<>();
    // 在server中定义客户端列表
    private static Map<String, Client> clientMap = new HashMap<>();
    private static Set<String> onlineUsers = new HashSet<>();
    private static Set<String> offlineUsers = new HashSet<>();

    public class Client {
        public String username;
        public PrintWriter writer;

        public Client(String username, PrintWriter writer) {
            this.username = username;
            this.writer = writer;
        }

        public String getUsername() {
            return username;
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }


    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // 将新连接的客户端的输出流加入到 clients 集合中
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                clients.add(output);
//                不许在线
                String userName = getUserName(socket);
                if (userName=="REGISTER"){
                    clients.remove(output);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
//                添加在线名单
                addOnlineUser(userName);
//                发离线群组消息，发历史消息
                addClient(userName,output);
                Client client=new Client(userName,output);
                // 新开一个线程来处理该客户端的消息
                broadcastMessage("REFRESH,"+onlineUsers.size());
                ClientHandler clientHandler = new ClientHandler(socket,client);
                new Thread(clientHandler).start();}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            broadcastMessage("SERVER,OUT");
        }
    }

    @Override
    public void run() {
        start();
    }

    // 广播消息给所有在线客户端
    public static void broadcastMessage(String message) {
        for (PrintWriter output : clients) {
            output.println(message);
        }
    }

    // 获取客户端的用户名
    public static String getUserName(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

        String inputLine;
        int flag=0;
        boolean flager=false;
        try {
            while ((inputLine = input.readLine()) != null){
                System.out.println(inputLine);
                    switch (inputLine){
                        case "GET_ONLINE_USERS":
                            String onlineUsers = Server.getOnlineUsers().toString();
                            output.println("USERS," + onlineUsers);
                            String offlineUsers= Server.getOfflineUsers().toString();
                            output.println("USERS," + offlineUsers);
                            flag =1;
                            break;
                        case "REGISTER":
                             onlineUsers = Server.getOnlineUsers().toString();
                            output.println("USERS," + onlineUsers);
                             offlineUsers= Server.getOfflineUsers().toString();
                            output.println("USERS," + offlineUsers);
                            flag=2;
                            break;
                        default:
                            flager=true;
                            break;
                    }
                if(flager==true){
                    break;
                }
            }
            if(flag==2){
                offlineUsers.add(inputLine);
                System.out.println(offlineUsers);
                return "REGISTER";
            }
            String userName = inputLine;
            return userName;
        }catch (SocketException e){

        }
        return null;
    }

    // 获取在线用户列表
    public static Set<String> getOnlineUsers() {
        return onlineUsers;
    }
    public static Set<String> getOfflineUsers() {
        return offlineUsers;
    }

    // 添加用户到在线用户列表
    public static void addOnlineUser(String userName) {
        System.out.println(userName);
        onlineUsers.add(userName);
    }

    // 从在线用户列表中移除用户
    public static void removeOnlineUser(String userName) {
        onlineUsers.remove(userName);
        offlineUsers.add(userName);
    }
    // 客户端下线时调用此方法，将用户加入离线用户列表
    public synchronized void removeClient(String username) throws IOException {
        if (clientMap.containsKey(username)) {
            clientMap.remove(username);
            onlineUsers.remove(username);
            offlineUsers.add(username);
        }
    }
    // 客户端上线时调用此方法，向用户发送未读消息
    public synchronized void addClient(String username, PrintWriter output) throws IOException {
        if (!clientMap.containsKey(username)) {
//            不在线
            List<String>History=getReadMessages(username);
            for (String message: History){
                output.println(message);
            }
            Client client=new Client(username,output);
            clientMap.put(username, client);
            onlineUsers.add(username);
            if (offlineUsers.contains(username)) {
//                已注册
                List<String> messages = getUnreadMessages(username);
                for (String message : messages) {
                    output.println(message);
                    String[] parts = message.split(",");
                    parts[0] = "HISTORIES " + parts[0];
                    String newMessage = String.join(",", parts);
                    addReadMessage(username,newMessage);
                }
                clearUnreadMessages(username);
                offlineUsers.remove(username);
            }
        }

    }

    // 处理客户端的消息
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter output;
        private String userName;
        private Client client;
        public ClientHandler(Socket socket,Client client) {
            this.client=client;
            this.socket = socket;
            this.output = client.writer;
            this.userName = client.username;
        }


        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    System.out.println(inputLine);
                    String[] parts = inputLine.split(",");
                    String command = parts[0];
                    String message = parts.length > 1 ? parts[1] : "";
                    Long Timestamp ;
                    String Sendto;
                    String SendBy;
                    String Data;
                    PrintWriter writer;
                    switch (command) {
                        case "LOGOUT":
                            break;
                        case "SEND":
                             Timestamp = Long.parseLong(parts[1]);
                             Sendto = parts[2];
                             SendBy = parts[3];
                             Data = parts[4];
                            String Message2="MESSAGE" + "," + Timestamp.toString() + "," + Sendto + "," + SendBy + "," + Data;
                             writer = clientMap.get(Sendto).writer;
                            writer.println(Message2);
                            Message2="HISTORY" + "," + Timestamp.toString() + "," + Sendto + "," + Sendto + "," + Data;
                            addReadMessage(SendBy,Message2);
                            Message2="HISTORY" + "," + Timestamp.toString() + "," + Sendto + "," + SendBy + "," + Data;
                            addReadMessage(Sendto,Message2);
                            break;
                        case "SENDFile":
                            Timestamp = Long.parseLong(parts[1]);
                            Sendto = parts[2];
                            SendBy = parts[3];
                            Data = parts[4];
                            writer = clientMap.get(Sendto).writer;
                            String Message3="FILE" + "," + Timestamp.toString() + "," + Sendto + "," + SendBy + "," + Data;
                            writer.println(Message3);
//                            addReadMessage(SendBy,Message2);
//                            addReadMessage(Sendto,Message2);
                            break;
                        case "SENDS":
                            Timestamp = Long.parseLong(parts[1]);
                            Sendto = parts[2];
                            String[] Sendtos = new String[parts.length-5];
                            for (int i=3; i<parts.length-2; i++) {
                                Sendtos[i-3] = parts[i];
                            }
                            SendBy = parts[parts.length-2];
                            Data = parts[parts.length-1];
                            String joined = String.join(",", Sendtos);
                            Message2="HISTORIES" + "," + Timestamp.toString() + "," + Sendto + "," +joined+","+SendBy + "," + Data;
                            addReadMessage(SendBy,Message2);
                            for (String user : Sendtos) {
                                if(!user.equals(SendBy)){
                                // 检查用户是否在线
                                if (clientMap.containsKey(user)) {
                                    // 用户在线，向其发送消息

                                    writer = clientMap.get(user).writer;
                                    writer.println("MESSAGES," + Timestamp.toString() + "," + Sendto + "," + joined + "," + SendBy + "," + Data);
                                    Message2="HISTORIES" + "," + Timestamp.toString() + "," + Sendto + "," +joined+","+ SendBy + "," + Data;
                                    addReadMessage(user,Message2);
                                } else {
                                    // 用户离线，将消息存储到离线消息列表中
                                    String message1 = "MESSAGES," + Timestamp.toString() + "," + Sendto + "," + String.join(",", Sendtos) + "," + SendBy + "," + Data;
                                    addUnreadMessage(user, message1);
                                }
                            }}
                            break;


                        case "GET_ONLINE_USERS":
                            // 处理获取在线用户列表请求
                            String onlineUsers = Server.getOnlineUsers().toString();
                            output.println("USERS," + onlineUsers);
                            break;


                        default:
                            output.println("ERROR Unknown command: " + command);
                            break;
                    }
                }

            } catch (IOException e) {
                System.out.println("hasleaved");
            }
            finally {
                // 客户端断开连接时，将其从在线用户列表中移除，并广播离开消息
                removeOnlineUser(userName);
                clients.remove(output);
                clientMap.remove(userName);
                broadcastMessage("BYE,"+userName);
                broadcastMessage("REFRESH,"+onlineUsers.size());
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }// 定义client类


    // 添加未读消息到unreadMessages
    public static synchronized void addUnreadMessage(String username, String message) {
        List<String> messages = unreadMessages.getOrDefault(username, new ArrayList<>());
        messages.add(message);
        unreadMessages.put(username, messages);
    }
    public static synchronized void addReadMessage(String username, String message) {
        List<String> messages = readMessages.getOrDefault(username, new ArrayList<>());
        messages.add(message);
        readMessages.put(username, messages);
    }
    public static synchronized void clearUnreadMessages(String username) {
        unreadMessages.remove(username);
    }
    // 获取未读消息列表
    public static synchronized List<String> getUnreadMessages(String username) {
        return unreadMessages.getOrDefault(username, new ArrayList<>());
    }
    public static synchronized List<String> getReadMessages(String username) {
        return readMessages.getOrDefault(username, new ArrayList<>());
    }

}
