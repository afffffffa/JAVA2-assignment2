package cn.edu.sustech.cs209.chatting.common;

import java.util.List;

public class Message {

  private Long timestamp;

  private String sentBy;

  private String sendTo;
  private List<String> sendTOs;

  public String data;

  public String command;

  public Message(Long timestamp, String sentBy, String sendTo, String data, String command) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendTo;
    this.data = data;
    this.command = command;
  }

  public Message(Long timestamp, String sentBy, String sendto, List<String> sendTOs, String data, String command) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendto;
    this.sendTOs = sendTOs;
    this.data = data;
    this.command = command;
  }

  public Message(String inputLine) {
    String[] parts = inputLine.split(",");
    this.timestamp = Long.parseLong(parts[1]);
    this.sendTo = parts[2];
    this.sentBy = parts[3];
    this.data = parts[4];
    this.command = parts[0];
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getSentBy() {
    return sentBy;
  }

  public String getSendTo() {
    return sendTo;
  }

  public String getData() {
    return data;
  }

  public List<String> getSendTOs() {
    return sendTOs;
  }

  public void setSendTOs(List<String> sendTOs) {
    this.sendTOs = sendTOs;
  }
}
