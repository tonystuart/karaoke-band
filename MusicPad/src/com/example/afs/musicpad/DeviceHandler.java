// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad;

import com.example.afs.fluidsynth.Synthesizer;
import com.example.afs.musicpad.device.DeviceReader;
import com.example.afs.musicpad.message.Command;
import com.example.afs.musicpad.message.CommandEntered;
import com.example.afs.musicpad.message.CommandForwarded;
import com.example.afs.musicpad.message.Message;
import com.example.afs.musicpad.message.PlayOff;
import com.example.afs.musicpad.message.PlayOn;
import com.example.afs.musicpad.message.SongSelected;
import com.example.afs.musicpad.message.TickOccurred;
import com.example.afs.musicpad.player.KeyChordPlayer;
import com.example.afs.musicpad.player.KeyNotePlayer;
import com.example.afs.musicpad.player.Player;
import com.example.afs.musicpad.player.Player.Action;
import com.example.afs.musicpad.player.SongChordPlayer;
import com.example.afs.musicpad.player.SongNotePlayer;
import com.example.afs.musicpad.song.Song;
import com.example.afs.musicpad.theory.Keys;
import com.example.afs.musicpad.util.Broker;
import com.example.afs.musicpad.util.BrokerTask;

public class DeviceHandler extends BrokerTask<Message> {

  private Player player;
  private Song currentSong;
  private Synthesizer synthesizer;
  private DeviceReader deviceReader;
  private Player defaultPlayer;

  protected DeviceHandler(Broker<Message> messageBroker, Synthesizer synthesizer, String deviceName) {
    super(messageBroker);
    this.synthesizer = synthesizer;
    this.deviceReader = new DeviceReader(getInputQueue(), deviceName);
    this.defaultPlayer = new KeyNotePlayer(synthesizer, Keys.CMajor, 0);
    this.player = defaultPlayer;
    delegate(CommandEntered.class, message -> onCommand(message.getCommand(), message.getParameter()));
    delegate(PlayOn.class, message -> onPlayOn(message.getPlayIndex()));
    delegate(PlayOff.class, message -> onPlayOff(message.getPlayIndex()));
    subscribe(SongSelected.class, message -> OnSongSelected(message.getSong()));
    subscribe(TickOccurred.class, message -> onTick(message.getTick()));
  }

  @Override
  public void start() {
    super.start();
    deviceReader.start();
  }

  @Override
  public void terminate() {
    deviceReader.terminate();
    super.terminate();
  }

  private void doSelectChords(int channelNumber) {
    player.close();
    if (channelNumber == 0 || currentSong == null) {
      defaultPlayer = new KeyChordPlayer(synthesizer, Keys.CMajor, 0);
      player = defaultPlayer;
    } else {
      int channelIndex = channelNumber - 1;
      player = new SongChordPlayer(synthesizer, currentSong, channelIndex);
    }
  }

  private void doSelectContour(int channelNumber) {
    player.close();
    if (channelNumber == 0 || currentSong == null) {
      defaultPlayer = new KeyNotePlayer(synthesizer, Keys.CMajor, 0);
      player = defaultPlayer;
    } else {
      int channelIndex = channelNumber - 1;
      player = new SongNotePlayer(synthesizer, currentSong, channelIndex);
    }
  }

  private void doSelectProgram(int programNumber) {
    int programIndex = programNumber - 1;
    player.selectProgram(programIndex);
  }

  private void onCommand(int command, int parameter) {
    switch (command) {
    case Command.SELECT_CHORDS:
      doSelectChords(parameter);
      break;
    case Command.SELECT_PROGRAM:
      doSelectProgram(parameter);
      break;
    case Command.SELECT_NOTES:
      doSelectContour(parameter);
      break;
    default:
      publish(new CommandForwarded(command, parameter));
      break;
    }
  }

  private void onPlayOff(int playIndex) {
    player.play(Action.RELEASE, playIndex);
  }

  private void onPlayOn(int playIndex) {
    player.play(Action.PRESS, playIndex);
  }

  private void OnSongSelected(Song song) {
    player.close();
    currentSong = song;
    player = defaultPlayer;
  }

  private void onTick(long tick) {
    player.onTick(tick);
  }

}
