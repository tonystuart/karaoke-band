// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.common;

import java.util.Set;

import com.example.afs.fluidsynth.Synthesizer;
import com.example.afs.musicpad.Command;
import com.example.afs.musicpad.DeviceCommand;
import com.example.afs.musicpad.message.OnCommand;
import com.example.afs.musicpad.message.OnConfigurationChange;
import com.example.afs.musicpad.message.OnDeviceCommand;
import com.example.afs.musicpad.message.OnInputMessage;
import com.example.afs.musicpad.message.OnInputMessage.MessageType;
import com.example.afs.musicpad.message.OnKeyDown;
import com.example.afs.musicpad.message.OnKeyUp;
import com.example.afs.musicpad.message.OnPublishInputMode;
import com.example.afs.musicpad.message.OnSampleChannel;
import com.example.afs.musicpad.message.OnSampleSong;
import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.player.PlayableMap;
import com.example.afs.musicpad.player.PlayableMap.OutputType;
import com.example.afs.musicpad.player.Player;
import com.example.afs.musicpad.player.Player.Action;
import com.example.afs.musicpad.player.PlayerDetail;
import com.example.afs.musicpad.player.Sound;
import com.example.afs.musicpad.service.BackgroundMuteService;
import com.example.afs.musicpad.service.DeviceControllerService;
import com.example.afs.musicpad.service.PlayerDetailService;
import com.example.afs.musicpad.service.PlayerVelocityService;
import com.example.afs.musicpad.song.Song;
import com.example.afs.musicpad.task.MessageBroker;
import com.example.afs.musicpad.task.ServiceTask;
import com.example.afs.musicpad.util.Range;

public class DeviceHandler extends ServiceTask {

  private static final int DEFAULT_VELOCITY = 64;

  private int channel;
  private int deviceIndex;
  private int velocity = DEFAULT_VELOCITY;

  private Song song;
  private Player player;
  private Controller controller;
  private Synthesizer synthesizer;
  private PlayableMap playableMap;
  private Sound[] activeSounds = new Sound[256]; // NB: KeyEvents VK codes, not midiNotes
  private ModeCounter publishInputMode = new ModeCounter();

  public DeviceHandler(MessageBroker broker, Synthesizer synthesizer, int deviceIndex) {
    super(broker);
    this.synthesizer = synthesizer;
    this.deviceIndex = deviceIndex;
    this.player = new Player(synthesizer, deviceIndex);
    subscribe(OnCommand.class, message -> doCommand(message));
    subscribe(OnSampleSong.class, message -> doSampleSong(message));
    subscribe(OnSampleChannel.class, message -> doSampleChannel(message));
    subscribe(OnDeviceCommand.class, message -> doDeviceCommand(message));
    subscribe(OnConfigurationChange.class, message -> doConfigurationChange(message));
    subscribe(OnPublishInputMode.class, message -> doPublishInputMode(message));
    provide(new PlayerDetailService(deviceIndex), () -> getPlayerDetail());
    provide(new PlayerVelocityService(deviceIndex), () -> getPercentVelocity());
    provide(new BackgroundMuteService(deviceIndex), () -> synthesizer.isMuted(channel));
    provide(new DeviceControllerService(deviceIndex), () -> controller);
  }

  public int getDeviceIndex() {
    return deviceIndex;
  }

  public int getPercentVelocity() {
    return Range.scaleMidiToPercent(velocity);
  }

  public Player getPlayer() {
    return player;
  }

  public void onChannelPressure(int channel, int pressure) {
    synthesizer.setChannelPressure(channel, pressure);
  }

  public void onControlChange(int control, int value) {
    getPlayer().changeControl(control, value);
    if (publishInputMode.isSet()) {
      publish(new OnInputMessage(MessageType.CONTROL_CHANGE, deviceIndex, control, value));
    }
  }

  public void onDown(int inputCode) {
    processDown(inputCode, velocity);
  }

  public void onDown(int inputCode, int velocity) {
    processDown(inputCode, Range.scale(this.velocity / 2, Midi.MAX_VALUE, 0, Midi.MAX_VALUE, velocity));
  }

  public void onUp(int inputCode) {
    processUp(inputCode, velocity);
  }

  public void onUp(int inputCode, int velocity) {
    processUp(inputCode, Range.scale(this.velocity / 2, Midi.MAX_VALUE, 0, Midi.MAX_VALUE, velocity));
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public void setPercentVelocity(int velocity) {
    this.velocity = Range.scalePercentToMidi(velocity);
  }

  private void createPlayableMap() {
    playableMap = new PlayableMap(controller.getConfiguration(), song.getChannelNotes(channel), player.getOutputType());
  }

  private void doCommand(OnCommand message) {
    Command command = message.getCommand();
    int parameter = message.getParameter();
    switch (command) {
    case PLAY:
      player.setEnabled(true);
      break;
    case RESET:
      doReset();
      break;
    case SET_TEMPO:
      player.setPercentTempo(parameter);
      break;
    case STOP:
      // TODO: Consider making explicit
      player.setEnabled(true);
      break;
    default:
      break;
    }
  }

  private void doConfigurationChange(OnConfigurationChange message) {
    if (message.getDeviceIndex() == deviceIndex) {
      createPlayableMap();
    }
  }

  private void doDecreasePlayerVelocity() {
    publish(new OnDeviceCommand(DeviceCommand.VELOCITY, deviceIndex, Math.max(0, getPercentVelocity() - 5)));
  }

  private void doDeviceCommand(OnDeviceCommand message) {
    if (message.getDeviceIndex() == deviceIndex) {
      DeviceCommand deviceCommand = message.getDeviceCommand();
      int parameter = message.getParameter();
      switch (deviceCommand) {
      case DECREASE_PLAYER_VELOCITY:
        doDecreasePlayerVelocity();
        break;
      case INCREASE_PLAYER_VELOCITY:
        doIncreasePlayerVelocity();
        break;
      case OUTPUT:
        doOutput(parameter);
        break;
      case MUTE_BACKGROUND:
        doMuteBackground();
        break;
      case NEXT_CHANNEL:
        doNextChannel();
        break;
      case NEXT_PROGRAM:
        doNextProgram();
        break;
      case PREVIOUS_CHANNEL:
        doPreviousChannel();
        break;
      case PREVIOUS_PROGRAM:
        doPreviousProgram();
        break;
      case PROGRAM:
        selectProgram(parameter);
        break;
      case SELECT_CHANNEL:
        selectChannel(parameter);
        break;
      case VELOCITY:
        setPercentVelocity(parameter);
        break;
      default:
        break;
      }
    }
  }

  private void doIncreasePlayerVelocity() {
    publish(new OnDeviceCommand(DeviceCommand.VELOCITY, deviceIndex, Math.min(100, getPercentVelocity() + 5)));
  }

  private void doMuteBackground() {
    synthesizer.muteChannel(channel, !synthesizer.isMuted(channel));
  }

  private void doNextChannel() {
    int activeChannel;
    int nextActiveChannel = -1;
    int[] activeChannels = song.getActiveChannels();
    for (int i = activeChannels.length - 1; i >= 0 && (activeChannel = activeChannels[i]) > channel; i--) {
      nextActiveChannel = activeChannel;
    }
    if (nextActiveChannel != -1) {
      selectChannel(nextActiveChannel);
    }
  }

  private void doNextProgram() {
    if (channel != Midi.DRUM) {
      int currentProgram = player.getProgram();
      int nextProgram = currentProgram + 1;
      if (nextProgram == Midi.PROGRAMS) {
        nextProgram = 0;
      }
      selectProgram(nextProgram);
    }
  }

  private void doOutput(int typeIndex) {
    OutputType outputType = OutputType.values()[typeIndex];
    player.setOutputType(outputType);
    createPlayableMap();
  }

  private void doPreviousChannel() {
    int activeChannel;
    int previousActiveChannel = -1;
    int[] activeChannels = song.getActiveChannels();
    for (int i = 0; i < activeChannels.length && (activeChannel = activeChannels[i]) < channel; i++) {
      previousActiveChannel = activeChannel;
    }
    if (previousActiveChannel != -1) {
      selectChannel(previousActiveChannel);
    }
  }

  private void doPreviousProgram() {
    if (channel != Midi.DRUM) {
      int currentProgram = player.getProgram();
      int nextProgram = currentProgram - 1;
      if (nextProgram < 0) {
        nextProgram = Midi.PROGRAMS - 1;
      }
      selectProgram(nextProgram);
    }
  }

  private void doPublishInputMode(OnPublishInputMode message) {
    publishInputMode.adjust(message.isPublishMode());
  }

  private void doReset() {
    velocity = DEFAULT_VELOCITY;
    player.reset();
    synthesizer.muteChannel(channel, false);
  }

  private void doSampleChannel(OnSampleChannel message) {
    if (message.getDeviceIndex() == deviceIndex) {
      selectChannel(message.getChannel());
      player.setEnabled(true);
    } else {
      player.setEnabled(false);
    }
  }

  private void doSampleSong(OnSampleSong message) {
    song = message.getSong();
  }

  private PlayerDetail getPlayerDetail() {
    if (playableMap == null) {
      createPlayableMap();
    }
    return new PlayerDetail(playableMap.getPlayables(), channel, player.getProgram());
  }

  private void processDown(int inputCode, int velocity) {
    if (playableMap != null) {
      Sound sound = playableMap.onDown(inputCode);
      if (sound != null) {
        if (sound != null) {
          player.play(Action.PRESS, sound, velocity);
          activeSounds[inputCode] = sound;
          publish(new OnKeyDown(deviceIndex, sound));
        }
      }
    }
    if (publishInputMode.isSet()) {
      publish(new OnInputMessage(MessageType.DOWN, deviceIndex, inputCode, velocity));
    }
  }

  private void processUp(int inputCode, int velocity) {
    if (playableMap != null) {
      playableMap.onUp(inputCode);
      Sound sound = activeSounds[inputCode];
      if (sound != null) {
        player.play(Action.RELEASE, sound, velocity);
        activeSounds[inputCode] = null;
        publish(new OnKeyUp(deviceIndex, sound));
      }
    }
    if (publishInputMode.isSet()) {
      publish(new OnInputMessage(MessageType.UP, deviceIndex, inputCode, velocity));
    }
  }

  private void selectChannel(int channel) {
    this.channel = channel;
    if (channel == Midi.DRUM) {
      selectProgram(-1);
    } else {
      Set<Integer> programs = song.getPrograms(channel);
      if (programs.size() > 0) {
        int program = programs.iterator().next();
        selectProgram(program);
      }
    }
    createPlayableMap();
  }

  private void selectProgram(int program) {
    player.selectProgram(program);
  }

}
