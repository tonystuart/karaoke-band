// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.player;

import java.util.Set;

import com.example.afs.fluidsynth.FluidSynth;
import com.example.afs.fluidsynth.Synthesizer;
import com.example.afs.musicpad.Trace;
import com.example.afs.musicpad.analyzer.Names;
import com.example.afs.musicpad.device.common.DeviceHandler;
import com.example.afs.musicpad.device.common.InputMapping;
import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.renderer.Notator.KeyCap;
import com.example.afs.musicpad.renderer.Notator.Slice;
import com.example.afs.musicpad.song.Song;
import com.example.afs.musicpad.util.RandomAccessList;
import com.example.afs.musicpad.util.Velocity;

public abstract class Player {

  public enum Action {
    PRESS, RELEASE
  }

  private static final int PLAYER_BASE = Midi.CHANNELS;
  private static final int PLAYER_CHANNELS = Midi.CHANNELS;
  public static final int TOTAL_CHANNELS = PLAYER_BASE + PLAYER_CHANNELS;

  private static final int DEFAULT_VELOCITY = 96;
  private static final int DEFAULT_PERCENT_VELOCITY = 100;

  protected Song song;
  protected int songChannel;
  protected int deviceIndex;
  protected String mappingType;
  protected InputMapping inputMapping;
  private int playbackChannel;
  private Synthesizer synthesizer;
  private int percentVelocity = DEFAULT_PERCENT_VELOCITY;

  public Player(DeviceHandler deviceHandler, Song song) {
    this.deviceIndex = deviceHandler.getDeviceIndex();
    this.songChannel = deviceHandler.getChannel();
    this.inputMapping = deviceHandler.getInputMapping();
    this.mappingType = inputMapping.getClass().getSimpleName();
    this.synthesizer = deviceHandler.getSynthesizer();
    this.song = song;
    initializeDeviceChannel();
    initializeChannelProgram();
  }

  public void bendPitch(int pitchBend) {
    synthesizer.bendPitch(playbackChannel, pitchBend);
  }

  public void changeControl(int control, int value) {
    synthesizer.changeControl(playbackChannel, control, value);
  }

  public abstract void play(Action action, int midiNote);

  public void selectProgram(int program) {
    synthesizer.changeProgram(playbackChannel, program);
  }

  public void setPercentVelocity(int percentVelocity) {
    this.percentVelocity = percentVelocity;
  }

  public abstract String toKeyCap(Chord chord);

  public abstract String toKeyCap(int midiNote);

  public RandomAccessList<KeyCap> toKeyCaps(RandomAccessList<Slice> slices) {
    return inputMapping.toKeyCaps(slices);
  }

  protected int getHighestMidiNote() {
    int highestMidiNote = song.getHighestMidiNote(songChannel);
    if (highestMidiNote == -1) {
      highestMidiNote = 24;
    }
    return highestMidiNote;
  }

  protected int getLowestMidiNote() {
    int lowestMidiNote = song.getLowestMidiNote(songChannel);
    if (lowestMidiNote == -1) {
      lowestMidiNote = 84;
    }
    return lowestMidiNote;
  }

  protected void playMidiChord(Action action, Chord chord) {
    if (action == Action.PRESS && Trace.isTracePlay()) {
      System.out.println("Player.play: chordType=" + chord);
    }
    for (int midiNote : chord.getMidiNotes()) {
      // TODO: Consider arpeggiator options
      synthesizeNote(action, midiNote);
    }
  }

  protected void playMidiNote(Action action, int midiNote) {
    if (action == Action.PRESS && Trace.isTracePlay()) {
      System.out.println("Player.play: midiNote=" + Names.formatNote(midiNote));
    }
    synthesizeNote(action, midiNote);
  }

  private void initializeChannelProgram() {
    Set<Integer> programs = song.getPrograms(songChannel);
    if (programs.size() > 0) {
      int program = programs.iterator().next();
      synthesizer.changeProgram(playbackChannel, program);
    }
  }

  private void initializeDeviceChannel() {
    this.playbackChannel = PLAYER_BASE + deviceIndex;
    if (songChannel == Midi.DRUM) {
      synthesizer.setChannelType(playbackChannel, FluidSynth.CHANNEL_TYPE_DRUM);
    } else {
      synthesizer.setChannelType(playbackChannel, FluidSynth.CHANNEL_TYPE_MELODIC);
    }
    synthesizer.changeProgram(playbackChannel, 0); // initialize fluid_synth.c channel
  }

  private void synthesizeNote(Action action, int midiNote) {
    switch (action) {
    case PRESS:
      synthesizer.pressKey(playbackChannel, midiNote, Velocity.scale(DEFAULT_VELOCITY, percentVelocity));
      break;
    case RELEASE:
      synthesizer.releaseKey(playbackChannel, midiNote);
      break;
    default:
      throw new UnsupportedOperationException();
    }
  }

}
