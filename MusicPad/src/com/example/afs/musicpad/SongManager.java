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
import java.util.Random;
import java.util.Set;

import com.example.afs.musicpad.message.Message;
import com.example.afs.musicpad.message.OnAllTasksStarted;
import com.example.afs.musicpad.message.OnChannelAssigned;
import com.example.afs.musicpad.message.OnCommand;
import com.example.afs.musicpad.message.OnDeviceAttached;
import com.example.afs.musicpad.message.OnDeviceDetached;
import com.example.afs.musicpad.message.OnSong;
import com.example.afs.musicpad.message.OnSongList;
import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.parser.SongBuilder;
import com.example.afs.musicpad.song.Song;
import com.example.afs.musicpad.task.BrokerTask;
import com.example.afs.musicpad.util.Broker;
import com.example.afs.musicpad.util.DirectList;
import com.example.afs.musicpad.util.RandomAccessList;
import com.example.afs.musicpad.util.Value;

public class SongManager extends BrokerTask<Message> {

  private static final int PAGE_SIZE = 10;

  private Song song;
  private File directory;
  private Random random = new Random();
  private RandomAccessList<File> midiFiles;
  private Map<Integer, Integer> deviceChannelMap = new HashMap<>();
  private Set<Integer> deviceIndexes = new HashSet<>();

  public SongManager(Broker<Message> broker, String path) {
    super(broker);
    this.directory = new File(path);
    this.midiFiles = new DirectList<>();
    listMidiFiles(midiFiles, directory);
    midiFiles.sort((o1, o2) -> o1.getPath().compareTo(o2.getPath()));
    subscribe(OnCommand.class, message -> doCommand(message.getCommand(), message.getParameter()));
    subscribe(OnAllTasksStarted.class, message -> doAllTasksStarted());
    subscribe(OnDeviceAttached.class, message -> doDeviceAttached(message.getDeviceIndex()));
    subscribe(OnDeviceDetached.class, message -> doDeviceDetached(message.getDeviceIndex()));
  }

  private int assignChannel(int deviceIndex) {
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
    deviceChannelMap.put(deviceIndex, assignedChannel);
    return assignedChannel;
  }

  private void assignChannels() {
    for (int deviceIndex : deviceIndexes) {
      assignChannel(deviceIndex);
    }
  }

  private void doAllTasksStarted() {
    publishSongList();
    selectRandomSong();
  }

  private void doCommand(Command command, int parameter) {
    switch (command) {
    case SELECT_SONG:
      doSelectSong(parameter);
      break;
    case LIST_SONGS:
      doListSongs(parameter);
      break;
    case SELECT_CHANNEL:
      // TODO: Add support after device index is added to device handler messages
      break;
    default:
      break;
    }
  }

  private void doDeviceAttached(int deviceIndex) {
    deviceIndexes.add(deviceIndex);
    if (song != null) {
      int channel = assignChannel(deviceIndex);
      getBroker().publish(new OnChannelAssigned(song, deviceIndex, channel));
    }
  }

  private void doDeviceDetached(int deviceIndex) {
    deviceIndexes.remove(deviceIndex);
  }

  private void doListSongs(int songNumber) {
    int base;
    int limit;
    if (songNumber == 0) {
      base = 0;
      limit = midiFiles.size();
    } else {
      int songIndex = songNumber - 1;
      base = Math.max(0, songIndex);
      limit = Math.min(songIndex + PAGE_SIZE, midiFiles.size());
    }
    for (int songIndex = base; songIndex < limit; songIndex++) {
      File midiFile = midiFiles.get(songIndex);
      System.out.println("Song #" + (songIndex + 1) + ": " + midiFile.getName());
    }
  }

  private void doSelectSong(int songNumber) {
    int songIndex = Value.toIndex(songNumber);
    if (songIndex >= 0 && songIndex < midiFiles.size()) {
      selectSong(songIndex);
    }
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

  private void publishSongList() {
    String[] songList = new String[midiFiles.size()];
    for (int i = 0; i < songList.length; i++) {
      songList[i] = midiFiles.get(i).getName();
    }
    getBroker().publish(new OnSongList(songList));
  }

  private void selectRandomSong() {
    boolean selected = false;
    while (!selected) {
      try {
        int songIndex = random.nextInt(midiFiles.size());
        selectSong(songIndex);
        selected = true;
      } catch (RuntimeException e) {
        e.printStackTrace();
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          throw new RuntimeException(e1);
        }
      }
    }
  }

  private void selectSong(int songIndex) {
    File midiFile = midiFiles.get(songIndex);
    SongBuilder songBuilder = new SongBuilder();
    song = songBuilder.createSong(midiFile);
    System.out.println("Selecting song " + Value.toNumber(songIndex) + " - " + song.getName());
    assignChannels();
    publish(new OnSong(song, deviceChannelMap));
  }

  private void unassignChannel(int deviceIndex) {
    deviceChannelMap.remove(deviceIndex);
  }
}