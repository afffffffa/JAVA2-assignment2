package cn.edu.sustech.cs209.chatting.server;

public class Main {

  public static void main(String[] args) {
    System.out.println("Starting server");
    Server server = new Server();

    server.start();
  }
}