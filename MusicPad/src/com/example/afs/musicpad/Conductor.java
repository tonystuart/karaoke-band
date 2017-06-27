// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.afs.musicpad.message.Message;
import com.example.afs.musicpad.message.OnAllTasksStarted;
import com.example.afs.musicpad.message.OnChannelAssigned;
import com.example.afs.musicpad.message.OnCommand;
import com.example.afs.musicpad.message.OnDeviceAttached;
import com.example.afs.musicpad.message.OnDeviceCommand;
import com.example.afs.musicpad.message.OnDeviceDetached;
import com.example.afs.musicpad.message.OnMidiFiles;
import com.example.afs.musicpad.message.OnRepublishState;
import com.example.afs.musicpad.message.OnSample;
import com.example.afs.musicpad.message.OnSong;
import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.parser.SongBuilder;
import com.example.afs.musicpad.song.ChannelNotes;
import com.example.afs.musicpad.song.Song;
import com.example.afs.musicpad.task.BrokerTask;
import com.example.afs.musicpad.util.Broker;
import com.example.afs.musicpad.util.DirectList;
import com.example.afs.musicpad.util.RandomAccessList;

public class Conductor extends BrokerTask<Message> {

  private static final int TICKS_PER_PIXEL = 5;

  private Song song;
  private File directory;
  private RandomAccessList<File> midiFiles;
  private Map<Integer, Integer> deviceChannelMap = new HashMap<>();
  private Set<Integer> deviceIndexes = new HashSet<>();

  public Conductor(Broker<Message> broker, String path) {
    super(broker);
    this.directory = new File(path);
    this.midiFiles = new DirectList<>();
    listMidiFiles(midiFiles, directory);
    midiFiles.sort((o1, o2) -> o1.getPath().compareTo(o2.getPath()));
    subscribe(OnCommand.class, message -> doCommand(message));
    subscribe(OnDeviceCommand.class, message -> doDeviceCommand(message));
    subscribe(OnAllTasksStarted.class, message -> doAllTasksStarted());
    subscribe(OnDeviceAttached.class, message -> doDeviceAttached(message));
    subscribe(OnDeviceDetached.class, message -> doDeviceDetached(message));
    subscribe(OnRepublishState.class, message -> doRepublishState());
  }

  private void assignChannel(int deviceIndex) {
    unassignChannel(deviceIndex);
    int assignedChannel = -1;
    int firstActiveChannel = -1;
    for (int channel = 0; channel < Midi.CHANNELS && assignedChannel == -1; channel++) {
      if (song.getChannelNoteCount(channel) != 0) {
        if (firstActiveChannel == -1) {
          firstActiveChannel = channel;
        }
        if (!isChannelAssigned(channel)) {
          assignedChannel = channel;
        }
      }
    }
    if (assignedChannel == -1) {
      if (firstActiveChannel != -1) {
        assignedChannel = firstActiveChannel;
      } else {
        assignedChannel = 0;
      }
    }
    assignChannel(deviceIndex, assignedChannel);
  }

  private void assignChannel(int deviceIndex, int assignedChannel) {
    deviceChannelMap.put(deviceIndex, assignedChannel);
    publish(new OnChannelAssigned(song, deviceIndex, assignedChannel));
  }

  private void assignChannels() {
    for (int deviceIndex : deviceIndexes) {
      assignChannel(deviceIndex);
    }
  }

  private void doAllTasksStarted() {
    System.out.println("Conductor.doAllTasksStarted: deferring initialization until OnRepublishState");
    //publish(new OnMidiFiles(midiFiles));
  }

  private void doChannel(int deviceIndex, int channel) {
    unassignChannel(deviceIndex);
    assignChannel(deviceIndex, channel);
  }

  private void doCommand(OnCommand message) {
    Command command = message.getCommand();
    int parameter = message.getParameter();
    switch (command) {
    case SAMPLE:
      doSample(parameter);
      break;
    case SONG:
      doSelectSong(parameter);
      break;
    case TRANSPOSE:
      doTranspose(parameter);
      break;
    default:
      break;
    }
  }

  private void doDeviceAttached(OnDeviceAttached message) {
    int deviceIndex = message.getDeviceIndex();
    deviceIndexes.add(deviceIndex);
    if (song != null) {
      assignChannel(deviceIndex);
    }
  }

  private void doDeviceCommand(OnDeviceCommand message) {
    int deviceIndex = message.getDeviceIndex();
    DeviceCommand deviceCommand = message.getDeviceCommand();
    int parameter = message.getParameter();
    switch (deviceCommand) {
    case CHANNEL:
      doChannel(deviceIndex, parameter);
      break;
    default:
      break;

    }
  }

  private void doDeviceDetached(OnDeviceDetached message) {
    int deviceIndex = message.getDeviceIndex();
    deviceIndexes.remove(deviceIndex);
  }

  private void doRepublishState() {
    System.out.println("Conductor.doRepublishState: entered, song=" + song);
    publish(new OnMidiFiles(midiFiles));
    if (song != null) {
      // TODO: Initialize song based on user interaction
      publish(new OnSong(song, TICKS_PER_PIXEL));
      for (Entry<Integer, Integer> entry : deviceChannelMap.entrySet()) {
        int deviceIndex = entry.getKey();
        int assignedChannel = entry.getValue();
        publish(new OnChannelAssigned(song, deviceIndex, assignedChannel));
      }
    }
  }

  private void doSample(int songIndex) {
    if (songIndex >= 0 && songIndex < midiFiles.size()) {
      File midiFile = midiFiles.get(songIndex);
      SongBuilder songBuilder = new SongBuilder();
      song = songBuilder.createSong(midiFile);
      System.out.println("Sampling song " + songIndex + " - " + song.getTitle());
      publish(new OnSample(song, ChannelNotes.ALL_CHANNELS, TICKS_PER_PIXEL));
    }
  }

  private void doSelectSong(int songIndex) {
    if (songIndex >= 0 && songIndex < midiFiles.size()) {
      File midiFile = midiFiles.get(songIndex);
      SongBuilder songBuilder = new SongBuilder();
      song = songBuilder.createSong(midiFile);
      System.out.println("Selecting song " + songIndex + " - " + song.getTitle());
      publish(new OnSong(song, TICKS_PER_PIXEL));
      assignChannels();
    }
  }

  private void doTranspose(int distance) {
    song.transposeTo(distance);
    publish(new OnSong(song, TICKS_PER_PIXEL));
  }

  private boolean isChannelAssigned(int channel) {
    for (int assignedChannel : deviceChannelMap.values()) {
      if (assignedChannel == channel) {
        return true;
      }
    }
    return false;
  }

  private boolean isMidiFile(String name) {
    String lowerCaseName = name.toLowerCase();
    boolean isMidi = lowerCaseName.endsWith(".mid") || lowerCaseName.endsWith(".kar");
    return isMidi;
  }

  private void listMidiFiles(RandomAccessList<File> midiFiles, File parent) {
    if (!parent.isDirectory() || !parent.canRead()) {
      throw new IllegalArgumentException(parent + " is not a readable directory");
    }
    File[] files = parent.listFiles((dir, name) -> isMidiFile(name));
    for (File file : files) {
      if (file.isFile()) {
        midiFiles.add(file);
      } else if (file.isDirectory()) {
        listMidiFiles(midiFiles, file);
      }
    }
  }

  private void unassignChannel(int deviceIndex) {
    deviceChannelMap.remove(deviceIndex);
  }
}