package cn.edu.sustech.cs209.chatting.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ChatItem {
  private String username;
  private List<String> usrnames;
  private int unreadCount;
  ObservableList<Message> messages = FXCollections.observableArrayList();
  private Message lastMessage; // 新添加的属性

  public ChatItem(String username) {
    this.username = username;
    this.unreadCount = 0;
    this.messages = FXCollections.observableArrayList();
  }

  public ChatItem(String username, List<String> usernames) {
    this.usrnames = usernames;
    this.username = username;
    this.unreadCount = 0;
    this.messages = FXCollections.observableArrayList();
  }

  public String getUsername() {
    return username;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }

  public void incrementUnreadCount() {
    this.unreadCount++;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void addMessage(Message msg) {
    messages.add(msg);
    updateLastMessage();
//        incrementUnreadCount();
  }

  public List<String> getUsrnames() {
    return usrnames;
  }

  private void updateLastMessage() {
    if (!messages.isEmpty()) {
      lastMessage = messages.get(messages.size() - 1);
    }
  }

  public Long getLastMessage() {
    long timestamp = 0;
    if (lastMessage != null) {
      timestamp = lastMessage.getTimestamp();
      // rest of the code that uses the timestamp
    } else {
      // handle the case when lastMessage is null
    }
    return timestamp;
  }

  public void setUsrnames(List<String> usrnames) {
    this.usrnames = usrnames;
  }


  // ...


}

