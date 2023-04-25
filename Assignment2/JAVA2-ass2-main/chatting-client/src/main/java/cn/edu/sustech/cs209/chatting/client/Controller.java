package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.ChatItem;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
  private Socket socket;
  private BufferedReader input;
  private PrintWriter output;
  @FXML
  private Label groupMembersLabel;
  @FXML
  private Label currentOnlineCnt;
  @FXML
  ListView<Message> chatContentList;
  //    ObservableList<Message> messages = FXCollections.observableArrayList();
  @FXML
  ListView<ChatItem> chatList;
  @FXML
  TextArea inputArea;
  @FXML
  VBox mainPane;
  @FXML
  Label currentUsername;
  static String username;
  List<String> onlineUsers = new ArrayList<>();
  List<String> Userses = new ArrayList<>();

  public void setGroupMembersList(List<String> sendTOs) {
    if (sendTOs == null || sendTOs.isEmpty()) {
      groupMembersLabel.setVisible(false);
    } else {
      groupMembersLabel.setVisible(true);
      String members = String.join(", ", sendTOs);
      groupMembersLabel.setText("Group Members: " + members);
    }
  }

  public void setCurrentOnlineCnt(String num) {
    Platform.runLater(() -> {
      String members = num;
      currentOnlineCnt.setText("Online: " + members);
    });

  }

  public void setCurrentUsername(String num) {
    Platform.runLater(() -> {
      String members = num;
      currentUsername.setText("NOW: " + members);
    });
  }

  public void kill(Stage stage) {
    stage.setOnCloseRequest(event -> {
      output.println("LOGOUT," + username);

      try {

        socket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.exit(0);
    });

  }

  public void sendMessage(Message message) {
    if (message.getSendTOs() != null) {
      String joined = String.join(",", message.getSendTOs());
      output.println("SENDS" + "," + message.getTimestamp().toString() + "," + message.getSendTo() + "," + joined + "," + message.getSentBy() + "," + message.getData().replace("\n", "/a/").replace(",", "/b/"));
    } else {
      output.println("SEND" + "," + message.getTimestamp().toString() + "," + message.getSendTo() + "," + message.getSentBy() + "," + message.getData().replace("\n", "/a/").replace(",", "/b/"));
    }
  }

  public void sendMessageFile(Message message) {
    if (message.getSendTOs() != null) {
      String joined = String.join(",", message.getSendTOs());
      output.println("SENDS" + "," + message.getTimestamp().toString() + "," + message.getSendTo() + "," + joined + "," + message.getSentBy() + "," + message.getData().replace("\n", "/a/").replace(",", "/b/"));
    } else {
      output.println("SENDFile" + "," + message.getTimestamp().toString() + "," + message.getSendTo() + "," + message.getSentBy() + "," + message.getData().replace("\n", "/a/").replace(",", "/b/"));
    }
  }

  public List<String> getOnlineUsers() {
    // 向服务器发送获取在线用户列表的请求
    output.println("GET_ONLINE_USERS");
    onlineUsers.clear();
    return onlineUsers;
  }

  public List<String> registerOnlineUsers() {
    // 向服务器发送获取在线用户列表的请求
    output.println("REGISTER");
    onlineUsers.clear();
    return onlineUsers;
  }


  @FXML
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Welcome!");
    alert.setHeaderText(null);
    alert.setContentText("Please choose an option:");

// 创建两个按钮，一个登录，一个注册
    ButtonType loginBtn = new ButtonType("Login");
    ButtonType registerBtn = new ButtonType("Register");

// 将按钮添加到对话框中
    alert.getButtonTypes().setAll(loginBtn, registerBtn);

// 显示对话框并等待用户做出选择
    Optional<ButtonType> result = alert.showAndWait();

// 如果用户点击了登录按钮，则执行登录操作
    if (result.isPresent() && result.get() == loginBtn) {
      Dialog<String> dialog = new TextInputDialog();
      dialog.setTitle("Login");
      dialog.setHeaderText(null);
      dialog.setContentText("Username:");
      Optional<String> input1 = dialog.showAndWait();
      if (input1.isPresent() && !input1.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
        username = input1.get();
        try {
          socket = new Socket("localhost", 8002);
          input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          output = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
          System.err.println("Error connecting to server:," + e.getMessage());
          System.exit(1);
        }
        getOnlineUsers();
        String Users = null;
        String Usersoff = null;
        try {
          Users = input.readLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        try {
          Usersoff = input.readLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        String userListString = Users.substring(7, Users.length() - 1);
        String userOffListString = Usersoff.substring(7, Usersoff.length() - 1);
        String[] users = userListString.split(",");
        String[] usersoff = userOffListString.split(",");
        onlineUsers.clear();
        Userses.clear();
        // 将每个在线用户添加到 onlineUsers 列表中
        for (String user : users) {
          if (user.trim() != "") {
            onlineUsers.add(user.trim());
            Userses.add(user.trim());
          }
        }
        for (String user : usersoff) {
          if (user.trim() != "") {
            Userses.add(user.trim());
          }
        }
        System.out.println(Userses);
//            已经在线无法登录
        while (onlineUsers.contains(username)) {
          TextInputDialog newNameDialog = new TextInputDialog();
          newNameDialog.setTitle("Username Already Online");
          newNameDialog.setHeaderText(null);
          newNameDialog.setContentText("The username you entered is already Online. Please enter a different username:");
          Optional<String> result1 = newNameDialog.showAndWait();
          if (result1.isPresent() && !result1.get().isEmpty()) {
            username = result1.get();
          } else {
            // 用户单击了取消按钮或没有输入名称
            System.exit(0);
          }
          // 重新检查在线用户列表以确保新的用户名可用
          registerOnlineUsers();
          try {
            Users = input.readLine();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          try {
            Usersoff = input.readLine();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          userListString = Users.substring(7, Users.length() - 1);
          userOffListString = Usersoff.substring(7, Usersoff.length() - 1);
          users = userListString.split(",");
          usersoff = userOffListString.split(",");
          onlineUsers.clear();
          Userses.clear();
          // 将每个在线用户添加到 onlineUsers 列表中
          for (String user : users) {
            onlineUsers.add(user.trim());
            Userses.add(user.trim());
            System.out.println(onlineUsers);
          }
          for (String user : usersoff) {
            Userses.add(user.trim());
          }
        }
        if (!Userses.contains(username)) {
          Alert alert4 = new Alert(Alert.AlertType.INFORMATION);
          alert4.setTitle("Please register");
          alert4.setHeaderText(null);
          alert4.setContentText("The username you entered not exist");
          alert4.showAndWait();
          System.exit(0);
        }
        output.println(username);
        setCurrentUsername(username);
        new Thread(() -> {
          try {
            String line;
            while ((line = input.readLine()) != null) {
              System.out.println(line);
              String[] parts = line.split(",");
              String command = parts[0];
              String message = parts.length > 1 ? parts[1] : "";
              switch (command) {
                case "HISTORY":
//                                预处理
                  Long Timestamp = Long.parseLong(parts[1]);
                  String Sendto = parts[2];
                  String SendBy = parts[3];
                  String Data = parts[4];
                  Data = Data.replace("/a/", "\n").replace("/b/", ",");
                  // 遍历已有的对话
                  boolean found = false;
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(SendBy)) {
                      found = true;
                      Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                      // 如果找到了对应的chatitem，
                      System.out.println(chatItem);
                      chatItem.getMessages().add(msg);
                      chatList.refresh();
                      break;
                    }
                  }
                  // 如果未找到已有对话，则添加新的对话
                  if (!found) {
                    ChatItem newChatItem = new ChatItem(SendBy);
                    Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                    // 如果找到了对应的chatitem，
                    System.out.println(newChatItem);
                    newChatItem.getMessages().add(msg);
                    chatList.getItems().add(newChatItem);

                    Platform.runLater(() -> {
                      ObservableList<ChatItem> items = chatList.getItems();
//                                            Collections.sort(items, new ChatItemComparator());
                      chatList.setCellFactory(new ChatItemCellFactory());
                      chatList.refresh();
                      chatList.setOnMouseClicked(event -> {
                        if (event.getClickCount() > 1) {

                          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();
                          if (selectedChatItem != null) {
                            refreshChatContent(selectedChatItem);
                            setGroupMembersList(selectedChatItem.getUsrnames());
                          }
                        }
                      });

                      chatList.refresh();
                    });
                  }

                  break;
                case "BYE":
                  String userByename = parts[1];
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(userByename) && (chatItem.getUsrnames() == null)) {
                      Platform.runLater(() -> {
                        Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
                        alert1.setTitle("User has left the chat");
                        alert1.setHeaderText("The user " + userByename + " has left the chat.");
                        alert1.setContentText("Do you want to keep the conversation window or leave this chat item?");

                        ButtonType keepBtn = new ButtonType("Keep Conversation");
                        ButtonType leaveBtn = new ButtonType("Leave Chat Item");

                        alert1.getButtonTypes().setAll(keepBtn, leaveBtn);

                        Optional<ButtonType> result1 = alert1.showAndWait();
                        if (result1.get() == keepBtn) {
                          // do nothing
                        } else if (result1.get() == leaveBtn) {
                          chatList.getItems().remove(chatItem);
                          chatList.refresh();
                        }
                      });
                      break;
                    }
                  }

                  break;
                case "REFRESH":
                  setCurrentOnlineCnt(parts[1]);
                  break;
                case "SERVER":
                  Alert alert2 = new Alert(Alert.AlertType.ERROR);
                  alert2.setTitle("Server Error");
                  alert2.setHeaderText("The server has encountered an error or has exited.");
                  alert2.setContentText("Please close the client.");
                  alert2.showAndWait();
                  System.exit(0);
                  break;
                case "USER_JOINED":
                  onlineUsers.add(message);
                  break;
                case "MESSAGE":

                  Timestamp = Long.parseLong(parts[1]);
                  Sendto = parts[2];
                  SendBy = parts[3];
                  Data = parts[4];
                  Data = Data.replace("/a/", "\n").replace("/b/", ",");
                  // 遍历已有的对话
                  found = false;
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(SendBy)) {
                      found = true;
                      Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                      // 如果找到了对应的chatitem，
                      System.out.println(chatItem);
                      chatItem.getMessages().add(msg);
                      chatItem.incrementUnreadCount();
                      chatList.refresh();
                      break;
                    }
                  }
                  // 如果未找到已有对话，则添加新的对话
                  if (!found) {
                    ChatItem newChatItem = new ChatItem(SendBy);
                    Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                    // 如果找到了对应的chatitem，
                    System.out.println(newChatItem);
                    newChatItem.getMessages().add(msg);
                    newChatItem.incrementUnreadCount();
                    chatList.getItems().add(newChatItem);

                    Platform.runLater(() -> {
                      ObservableList<ChatItem> items = chatList.getItems();
//                                            Collections.sort(items, new ChatItemComparator());
                      chatList.refresh();
                      chatList.setCellFactory(new ChatItemCellFactory());
                      chatList.refresh();
                      chatList.setOnMouseClicked(event -> {
                        if (event.getClickCount() > 1) {

                          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();

                          if (selectedChatItem != null) {
                            refreshChatContent(selectedChatItem);
                            setGroupMembersList(selectedChatItem.getUsrnames());
                          }
                        }
                      });

                      chatList.refresh();
                    });
                  }
//                                chatList.getSelectionModel().select(chatList.getItems().size() - 1);
                  break;
                case "FILE":
                  Timestamp = Long.parseLong(parts[1]);
                  Sendto = parts[2];
                  SendBy = parts[3];
                  Data = parts[4];
                  Data = Data.replace("/a/", "\n").replace("/b/", ",");
                  // 遍历已有的对话
                  found = false;
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(SendBy)) {
                      found = true;
                      Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                      receiveFile(msg);
                      // 如果找到了对应的chatitem，
                      System.out.println(chatItem);
                      Message msg1 = new Message(Timestamp, SendBy, Sendto, "---you recieve a file---", "MESSAGE");
//                                            newChatItem.getMessages().add(msg1);
                      chatItem.getMessages().add(msg1);
                      chatItem.incrementUnreadCount();
                      chatList.refresh();
                      break;
                    }
                  }
                  // 如果未找到已有对话，则添加新的对话
                  if (!found) {
                    ChatItem newChatItem = new ChatItem(SendBy);
                    Message msg = new Message(Timestamp, SendBy, Sendto, Data, "MESSAGE");
                    receiveFile(msg);
                    // 如果找到了对应的chatitem，
                    System.out.println(newChatItem);

                    Message msg1 = new Message(Timestamp, SendBy, Sendto, "---you recieve a file---", "MESSAGE");
                    newChatItem.getMessages().add(msg1);
                    newChatItem.incrementUnreadCount();
                    chatList.getItems().add(newChatItem);
                    Platform.runLater(() -> {
                      ObservableList<ChatItem> items = chatList.getItems();
//                                            Collections.sort(items, new ChatItemComparator());

                      chatList.setCellFactory(new ChatItemCellFactory());
                      chatList.refresh();
                      chatList.setOnMouseClicked(event -> {
                        if (event.getClickCount() > 1) {


                          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();

                          if (selectedChatItem != null) {
                            refreshChatContent(selectedChatItem);
                            setGroupMembersList(selectedChatItem.getUsrnames());
                          }
                        }
                      });

                      chatList.refresh();
                    });
                  }


                  break;
                case "HISTORIES":
                  Long timestamp = Long.parseLong(parts[1]);
                  String sendTo = parts[2];
                  String[] sendTos = Arrays.copyOfRange(parts, 3, parts.length - 2);
                  String sendBy = parts[parts.length - 2];
                  String data = parts[parts.length - 1];

                  data = data.replace("/a/", "\n").replace("/b/", ",");
                  String[] chatItemUsernames = Arrays.copyOf(sendTos, sendTos.length);

//                                chatItemUsernames[chatItemUsernames.length - 1] = sendBy;
                  Arrays.sort(chatItemUsernames);

                  String chatItemName;
                  if (chatItemUsernames.length > 3) {
                    chatItemName = String.join("-", Arrays.asList(chatItemUsernames).subList(0, 3)) + "... (" + chatItemUsernames.length + ")";
                    ;
                  } else {
                    chatItemName = String.join("-", chatItemUsernames) + " (" + chatItemUsernames.length + ")";
                  }

                  // 将消息添加到对应的chatItem中
                  boolean found1 = false;
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(chatItemName) && (chatItem.getUsrnames().equals(Arrays.asList(chatItemUsernames)))) {
                      found1 = true;
                      Message msg = new Message(timestamp, sendBy, sendTo, data, "MESSAGE");
                      chatItem.getMessages().add(msg);

                      chatList.refresh();
                      break;
                    }
                  }

                  // 如果未找到已有对话，则添加新的对话
                  if (!found1) {
                    ChatItem newChatItem = new ChatItem(chatItemName, Arrays.asList(chatItemUsernames));
                    Message msg = new Message(timestamp, sendBy, sendTo, data, "MESSAGE");
                    newChatItem.getMessages().add(msg);

                    chatList.getItems().add(newChatItem);
                    Platform.runLater(() -> {
                      ObservableList<ChatItem> items = chatList.getItems();
//                                            Collections.sort(items, new ChatItemComparator());
                      chatList.setCellFactory(new ChatItemCellFactory());
                      chatList.refresh();
                      chatList.setOnMouseClicked(event -> {
                        if (event.getClickCount() > 1) {

                          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();

                          if (selectedChatItem != null) {
                            refreshChatContent(selectedChatItem);
                            setGroupMembersList(selectedChatItem.getUsrnames());
                          }
                        }
                      });
                      chatList.refresh();
                    });
                  }
                  break;
                case "MESSAGES":
                  timestamp = Long.parseLong(parts[1]);
                  sendTo = parts[2];
                  sendTos = Arrays.copyOfRange(parts, 3, parts.length - 2);
                  sendBy = parts[parts.length - 2];
                  data = parts[parts.length - 1];

                  data = data.replace("/a/", "\n").replace("/b/", ",");
                  chatItemUsernames = Arrays.copyOf(sendTos, sendTos.length);

//                                chatItemUsernames[chatItemUsernames.length - 1] = sendBy;
                  Arrays.sort(chatItemUsernames);


                  if (chatItemUsernames.length > 3) {
                    chatItemName = String.join("-", Arrays.asList(chatItemUsernames).subList(0, 3)) + "... (" + chatItemUsernames.length + ")";
                    ;
                  } else {
                    chatItemName = String.join("-", chatItemUsernames) + " (" + chatItemUsernames.length + ")";
                  }

                  // 将消息添加到对应的chatItem中
                  found1 = false;
                  for (ChatItem chatItem : chatList.getItems()) {
                    if (chatItem.getUsername().equals(chatItemName) && (chatItem.getUsrnames().equals(Arrays.asList(chatItemUsernames)))) {
                      found1 = true;
                      Message msg = new Message(timestamp, sendBy, sendTo, data, "MESSAGE");
                      chatItem.getMessages().add(msg);
                      chatItem.incrementUnreadCount();
                      chatList.refresh();
                      break;
                    }
                  }

                  // 如果未找到已有对话，则添加新的对话
                  if (!found1) {
                    ChatItem newChatItem = new ChatItem(chatItemName, Arrays.asList(chatItemUsernames));
                    Message msg = new Message(timestamp, sendBy, sendTo, data, "MESSAGE");
                    newChatItem.getMessages().add(msg);
                    newChatItem.incrementUnreadCount();

                    Platform.runLater(() -> {
                      chatList.getItems().add(newChatItem);
                      ObservableList<ChatItem> items = chatList.getItems();
//                                            Collections.sort(items, new ChatItemComparator());
                      chatList.setCellFactory(new ChatItemCellFactory());
                      chatList.refresh();
                      chatList.setOnMouseClicked(event -> {
                        if (event.getClickCount() > 1) {

                          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();

                          if (selectedChatItem != null) {
                            refreshChatContent(selectedChatItem);
                            setGroupMembersList(selectedChatItem.getUsrnames());
                          }
                        }
                      });
                      chatList.refresh();
                    });
                  }
                  break;

                case "USERS":
                  // 假设服务器返回的在线用户列表是以逗号分隔的字符串
                  String userListString1 = line.substring(7, line.length() - 1);
                  String[] users1 = userListString1.split(",");
                  onlineUsers.clear();
                  // 将每个在线用户添加到 onlineUsers 列表中
                  for (String user : users1) {
                    onlineUsers.add(user.trim());
                  }
                default:
                  break;
              }

            }
          } catch (IOException e) {

            Platform.runLater(() -> {
              Dialog dialog1 = new Dialog();
              dialog1.setTitle("Server Error");
              dialog1.setHeaderText("The server has encountered an error or has exited.");
              dialog1.setContentText("Please close the client.");

              ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
              dialog1.getDialogPane().getButtonTypes().add(okButton);

              Optional<ButtonType> result2 = dialog1.showAndWait();
              if (result2.isPresent() && result2.get() == okButton) {
                System.exit(0);
              }
            });
          }
        }).start();
      } else {
        System.out.println("Invalid username " + input + ", exiting");
        Platform.exit();
      }
    } else {
      Dialog<String> dialog = new TextInputDialog();
      dialog.setTitle("Register");
      dialog.setHeaderText(null);
      dialog.setContentText("Username:");
      Optional<String> input1 = dialog.showAndWait();
      if (input1.isPresent() && !input1.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
        username = input1.get();
        try {
          socket = new Socket("localhost", 8002);
          input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          output = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
          System.err.println("Error connecting to server:," + e.getMessage());
          System.exit(1);
        }
        registerOnlineUsers();
        String Users = null;
        String Usersoff = null;
        try {
          Users = input.readLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        try {
          Usersoff = input.readLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        String userListString = Users.substring(7, Users.length() - 1);
        String userOffListString = Usersoff.substring(7, Usersoff.length() - 1);
        String[] users = userListString.split(",");
        String[] usersoff = userOffListString.split(",");
        onlineUsers.clear();
        Userses.clear();
        // 将每个在线用户添加到 onlineUsers 列表中
        for (String user : users) {
          onlineUsers.add(user.trim());
          Userses.add(user.trim());
          System.out.println(onlineUsers);
        }
        for (String user : usersoff) {
          Userses.add(user.trim());
        }
//            已经注册无法注册
        while (Userses.contains(username)) {
          TextInputDialog newNameDialog = new TextInputDialog();
          newNameDialog.setTitle("Username Already Used");
          newNameDialog.setHeaderText(null);
          newNameDialog.setContentText("The username you entered is already Used. Please enter a different username:");
          Optional<String> result1 = newNameDialog.showAndWait();
          if (result1.isPresent() && !result1.get().isEmpty()) {
            username = result1.get();
          } else {
            // 用户单击了取消按钮或没有输入名称
            System.exit(0);
          }
          // 重新检查在线用户列表以确保新的用户名可用
          registerOnlineUsers();
          try {
            Users = input.readLine();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          try {
            Usersoff = input.readLine();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          userListString = Users.substring(7, Users.length() - 1);
          userOffListString = Usersoff.substring(7, Usersoff.length() - 1);
          users = userListString.split(",");
          usersoff = userOffListString.split(",");
          onlineUsers.clear();
          Userses.clear();
          // 将每个在线用户添加到 onlineUsers 列表中
          for (String user : users) {
            onlineUsers.add(user.trim());
            Userses.add(user.trim());
            System.out.println(onlineUsers);
          }
          for (String user : usersoff) {
            Userses.add(user.trim());
          }
        }
        output.println(username);
      } else {
        System.out.println("Invalid username " + input + ", exiting");
        Platform.exit();
      }
      Platform.exit();
    }

  }


  private void refreshChatContent(ChatItem chatItem) {
    chatItem.setUnreadCount(0);
    chatList.refresh();
    chatContentList.getItems().clear();
    chatContentList.setItems(FXCollections.observableArrayList(chatItem.getMessages()));
    chatContentList.setCellFactory(new MessageCellFactory());
  }

  @FXML
  public void createPrivateChat() {
    AtomicReference<String> user = new AtomicReference<>();
    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();
    // FIXME: get the user list from server, the current user's name should be filtered out
    // Fix: Call client.getOnlineUsers() to get the list of online users and remove the current user from it
    userSel.getItems().clear();
    getOnlineUsers();
    long startTime = System.currentTimeMillis();
    long timeout = 5000; // 等待时间为 5 秒钟
    while (onlineUsers.isEmpty()) {
      // 检查是否超时
      if (System.currentTimeMillis() - startTime > timeout) {
        System.out.println("等待服务器返回在线用户列表超时");
        break;
      }
    }
    onlineUsers.removeIf(u -> u.equals(username));
    userSel.getItems().addAll(onlineUsers);

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    // TODO: if the current user already chatted with the selected user, just open the chat with that user
    // Fix: Iterate through the chatList and check if there is already a chat with the selected user, if yes, select that chat.
    //      Otherwise, create a new chat item in the left panel, the title should be the selected user's name.
    for (String u : onlineUsers) {
      if (u.equals(user.get())) {
        for (int i = 0; i < chatList.getItems().size(); i++) {
          String chatUser = chatList.getItems().get(i).getUsername();
          if (chatUser.equals(user.get())) {

            chatList.getSelectionModel().select(i);
            return;
          }
        }
        ChatItem newChatItem = new ChatItem(u);
//                newChatItem.incrementUnreadCount();
        chatList.getItems().add(newChatItem);
        chatList.setCellFactory(new ChatItemCellFactory());
        ObservableList<ChatItem> items = chatList.getItems();
//                Collections.sort(items, new ChatItemComparator());
        chatList.refresh();
        chatList.setOnMouseClicked(event -> {
          if (event.getClickCount() > 1) {

            ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();

            if (selectedChatItem != null) {
              refreshChatContent(selectedChatItem);
              setGroupMembersList(selectedChatItem.getUsrnames());
            }
          }
        });
        chatList.refresh();

        // 在其他需要刷新的地方也可以直接调用 refreshChatContent 方法

        chatList.getSelectionModel().select(chatList.getItems().size() - 1);
        return;
      }
    }
  }

  public void createGroupChat() {
    List<String> selectedUsers = new ArrayList<>();
    Stage stage = new Stage();
    ListView<String> userListView = new ListView<>();
    userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    // FIXME: get the user list from server, the current user's name should be filtered out
    // Fix: Call client.getOnlineUsers() to get the list of online users and remove the current user from it
    getOnlineUsers();
    long startTime = System.currentTimeMillis();
    long timeout = 5000; // 等待时间为 5 秒钟
    while (onlineUsers.isEmpty()) {
      // 检查是否超时
      if (System.currentTimeMillis() - startTime > timeout) {
        System.out.println("等待服务器返回在线用户列表超时");
        break;
      }
    }
    onlineUsers.removeIf(u -> u.equals(username));
    userListView.getItems().addAll(onlineUsers);

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      selectedUsers.addAll(userListView.getSelectionModel().getSelectedItems());
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userListView, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    // TODO: create a new chat item in the left panel for the selected users
    // Fix: Create a group chat item with the selected users and add it to the chat list
    if (!selectedUsers.isEmpty()) {
      selectedUsers.add(username);
      String chatTitle;
      Collections.sort(selectedUsers);
      if (selectedUsers.size() > 3) {
        chatTitle = String.join("-", selectedUsers.subList(0, 3)) + "... (" + selectedUsers.size() + ")";
      } else {
        chatTitle = String.join("-", selectedUsers) + " (" + selectedUsers.size() + ")";
      }

      for (ChatItem item : chatList.getItems()) {
        if (item.getUsername().equals(chatTitle)) {

          chatList.getSelectionModel().select(item);
          return;
        }
      }
      setGroupMembersList(selectedUsers);
      ChatItem newChatItem = new ChatItem(chatTitle, selectedUsers);
      chatList.getItems().add(newChatItem);
      chatList.setCellFactory(new ChatItemCellFactory());
      chatList.getSelectionModel().select(newChatItem);
      ObservableList<ChatItem> items = chatList.getItems();
//            Collections.sort(items, new ChatItemComparator());
      chatList.refresh();
      chatList.setOnMouseClicked(event -> {
        if (event.getClickCount() > 1) {

          ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();
          if (selectedChatItem != null) {
            refreshChatContent(selectedChatItem);
            setGroupMembersList(selectedChatItem.getUsrnames());
          }
        }
      });
      chatList.refresh();
    }
  }

  // 发送文件
  @FXML
  private void sendFile() {
    // 创建一个文件选择器对话框
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select File to Send");
    File selectedFile = fileChooser.showOpenDialog(null);
    ChatItem selectedChatItem = chatList.getSelectionModel().getSelectedItem();
    if (selectedFile != null) {
      try {
        // 将所选文件转换为字节数组，并使用 BASE64 编码
        byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
        String encodedFile = Base64.getEncoder().encodeToString(fileBytes);

        // 构造一个消息，包含文件名和编码后的文件内容
        Message message = new Message(System.currentTimeMillis(), username, selectedChatItem.getUsername(), "/file " + selectedFile.getName() + " " + encodedFile, "MESSAGE");

        // 发送消息
        sendMessageFile(message);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // 接收文件
  private void receiveFile(Message message) {
    // 解码消息中的文件内容
    String[] parts = message.getData().split(" ");
    String filename = parts[1];
    byte[] fileBytes = Base64.getDecoder().decode(parts[2]);

    // 将文件保存到本地
    File file = new File(filename);
    try {
      Files.write(file.toPath(), fileBytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed.
   * After sending the message, you should clear the text input field.
   */
  @FXML
  public void doSendMessage() {
    // TODO
    String message = inputArea.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    String chatUser = chatList.getSelectionModel().getSelectedItem().getUsername();
    List<String> chatusers = chatList.getSelectionModel().getSelectedItem().getUsrnames();
    if (chatUser == null) {
      return;
    }
    Message msg = new Message(System.currentTimeMillis(), username, chatUser, chatusers, message, "SEND ");
    sendMessage(msg);
    inputArea.clear();
    ChatItem currentChating = chatList.getSelectionModel().getSelectedItem();
    currentChating.addMessage(msg);
    chatContentList.getItems().clear();
    chatContentList.setItems(FXCollections.observableArrayList(currentChating.getMessages()));
    chatContentList.setCellFactory(new MessageCellFactory());


  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model.
   * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
   */
  public static class ChatItemCellFactory implements Callback<ListView<ChatItem>, ListCell<ChatItem>> {

    @Override
    public ListCell<ChatItem> call(ListView<ChatItem> param) {
      return new ListCell<ChatItem>() {

        @Override
        public void updateItem(ChatItem chatItem, boolean empty) {
          super.updateItem(chatItem, empty);
          if (empty || Objects.isNull(chatItem)) {
            setText(null);
            setGraphic(null);
            return;
          }

          Label nameLabel = new Label(chatItem.getUsername());
          Label countLabel = new Label(chatItem.getUnreadCount() > 0 ? String.valueOf(chatItem.getUnreadCount()) : "");

          nameLabel.setPrefSize(150, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
          nameLabel.setAlignment(Pos.CENTER_LEFT);

          countLabel.setPrefSize(50, 20);
          countLabel.setStyle("-fx-background-color: red; -fx-text-fill: white;");
          countLabel.setAlignment(Pos.CENTER);
          countLabel.setPadding(new Insets(0, 5, 0, 5));
          countLabel.setVisible(chatItem.getUnreadCount() > 0);

          HBox wrapper = new HBox(nameLabel, countLabel);
          wrapper.setAlignment(Pos.CENTER_LEFT);
          wrapper.setSpacing(10);
          setGraphic(wrapper);
//                    param.getItems().sort(Comparator.comparingLong(ChatItem::getLastMessage).reversed());
        }
      };
    }
  }


  public static class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(msg.getData().replace("/笑脸/", "\uD83D\uDE0A").replace("/爱心/", "\u2764\uFE0F").replace("/彩虹/", "\uD83C\uDF08").replace("/狗/", "\uD83D\uDC36").replace("/猫/", "\uD83D\uDC31"));

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}
