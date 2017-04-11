// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.midi.configuration;

import java.util.HashMap;
import java.util.Map;

import com.example.afs.musicpad.Command;
import com.example.afs.musicpad.Trace;

public class Context {

  public interface HasSendDeviceMessage {
    void sendDeviceMessage(int port, int command, int channel, int data1, int data2);
  }

  public interface HasSendHandlerCommand {
    void sendHandlerCommand(Command command, Integer parameter);
  }

  public interface HasSendHandlerMessage {
    void sendHandlerMessage(int data1);
  }

  public static final String PORT = "port";
  public static final String COMMAND = "command";
  public static final String CHANNEL = "channel";
  public static final String DATA1 = "data1";
  public static final String DATA2 = "data2";
  public static final String CHANNEL_STATE = "channelState";

  private HasSendDeviceMessage hasSendDeviceMessage;
  private HasSendHandlerCommand hasSendHandlerCommand;
  private HasSendHandlerMessage hasSendHandlerMessage;

  private Map<String, Object> context = new HashMap<>();

  public void remove(String key) {
    context.remove(key);
  }

  public boolean contains(String key) {
    return context.containsKey(key);
  }

  public HasSendDeviceMessage getHasSendDeviceMessage() {
    return hasSendDeviceMessage;
  }

  public HasSendHandlerCommand getHasSendHandlerCommand() {
    return hasSendHandlerCommand;
  }

  public HasSendHandlerMessage getHasSendHandlerMessage() {
    return hasSendHandlerMessage;
  }

  public Object getLeft(String key) {
    return context.get(key);
  }

  public Object getRight(String value) {
    Object right;
    try {
      right = Integer.decode(value);
    } catch (NumberFormatException e) {
      right = context.get(value);
    }
    return right;
  }

  public boolean isTrace() {
    return Trace.isTraceConfiguration();
  }

  public void set(String key, Object value) {
    context.put(key, value);
  }

  public void setChannel(int channel) {
    context.put(CHANNEL, channel);
  }

  public void setChannelState(ChannelState channelState) {
    context.put(CHANNEL_STATE, channelState);
  }

  public void setCommand(int command) {
    context.put(COMMAND, command);
  }

  public void setData1(int data1) {
    context.put(DATA1, data1);
  }

  public void setData2(int data2) {
    context.put(DATA2, data2);
  }

  public void setHasSendDeviceMessage(HasSendDeviceMessage hasSendDeviceMessage) {
    this.hasSendDeviceMessage = hasSendDeviceMessage;
  }

  public void setHasSendHandlerCommand(HasSendHandlerCommand hasSendHandlerCommand) {
    this.hasSendHandlerCommand = hasSendHandlerCommand;
  }

  public void setHasSendHandlerMessage(HasSendHandlerMessage hasSendHandlerMessage) {
    this.hasSendHandlerMessage = hasSendHandlerMessage;
  }

  public void setPort(int port) {
    context.put(PORT, port);
  }

  @Override
  public String toString() {
    return "Context [context=" + context + "]";
  }

}