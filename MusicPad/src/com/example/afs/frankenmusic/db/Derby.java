// Copyright 2016 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.frankenmusic.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.afs.fluidsynth.Synthesizer;
import com.example.afs.fluidsynth.Synthesizer.Settings;
import com.example.afs.frankenmusic.loader.Notable;
import com.example.afs.frankenmusic.midi.SequenceBuilder;
import com.example.afs.jni.FluidSynth;
import com.example.afs.musicpad.midi.Instruments;
import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.song.Note;
import com.example.afs.musicpad.song.Note.NoteBuilder;
import com.example.afs.musicpad.transport.Transport;
import com.example.afs.musicpad.util.DirectList;
import com.example.afs.musicpad.util.RandomAccessList;

public class Derby {

  public static class DerbyImpl {

    private int baseTick;
    private int tickLength;
    private int lastEndTick;
    private int lastStartTick;
    private int previousTicks;

    private Transport transport;
    private RandomAccessList<Note> notes;

    public DerbyImpl() {
      Synthesizer synthesizer = createSynthesizer();
      transport = new Transport(synthesizer);
      reset();
    }

    private void addNote(Notable notable) {
      int duration = notable.getDuration();
      int tick = notable.getTick();
      if (tick == -1) {
        tick = lastEndTick;
      }
      if (tick < lastStartTick || tick > (lastEndTick + SequenceBuilder.TICKS_PER_MEASURE)) {
        baseTick = tick;
        int noteCount = notes.size();
        if (noteCount > 0) {
          int lastNoteIndex = noteCount - 1;
          long desiredTick = notes.get(lastNoteIndex).getTick() - SequenceBuilder.TICKS_PER_MEASURE;
          int noteIndex = findPreviousNoteIndex(desiredTick);
          rampDown(noteIndex);
          baseTick += SequenceBuilder.TICKS_PER_MEASURE;
        }
        previousTicks = tickLength;
      }
      int adjustedTick = previousTicks + (tick - baseTick);
      notable.setTick(adjustedTick);
      lastStartTick = tick;
      lastEndTick = tick + duration;
      tickLength = Math.max(tickLength, adjustedTick + duration);
      notes.add(toNote(notable));
    }

    private int append(int tick, int midiNote, int duration, int velocity, int program, int channel) {
      Notable notable = new Notable();
      notable.setChannel(channel);
      notable.setDuration(duration);
      notable.setNote(midiNote);
      notable.setProgram(program);
      notable.setTick(tick);
      notable.setVelocity(velocity);
      addNote(notable);
      return notes.size();
    }

    private int copy(int firstId, int lastId) {
      try {
        Connection connection = DriverManager.getConnection("jdbc:default:connection");
        Database database = new Database(connection);
        database.selectAllByClause(notable -> addNote(notable), Notable.class, "where id >= " + firstId + " and id <= " + lastId);
        return notes.size();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    private Synthesizer createSynthesizer() {
      System.loadLibrary(FluidSynth.NATIVE_LIBRARY_NAME);
      int processors = Runtime.getRuntime().availableProcessors();
      System.out.println("Derby.createSynthesizer: processors=" + processors);
      Settings settings = Synthesizer.createDefaultSettings();
      settings.set("synth.midi-channels", Midi.CHANNELS);
      settings.set("synth.cpu-cores", processors);
      Synthesizer synthesizer = new Synthesizer(settings);
      return synthesizer;
    }

    private int findPreviousNoteIndex(long desiredTick) {
      int index = notes.size() - 1;
      while (index > 0) {
        Note note = notes.get(index);
        if (note.getTick() < desiredTick) {
          return index;
        }
        index--;
      }
      return 0;
    }

    private void play() {
      transport.play(notes);
    }

    private String program(int program) {
      return Instruments.getProgramName(program);
    }

    private void rampDown(int noteIndex) {
      int limit = notes.size();
      int count = limit - noteIndex;
      if (count > 0) {
        double percent = 100;
        double percentDelta = 100d / count;
        for (int i = noteIndex; i < limit; i++) {
          Note note = notes.get(i);
          int velocity = (int) ((percent * note.getVelocity()) / 100);
          notes.set(i, new NoteBuilder().withNote(note).withVelocity(velocity).create());
          percent -= percentDelta;
        }
      }
    }

    private void reset() {
      baseTick = 0;
      tickLength = 0;
      lastEndTick = 0;
      lastStartTick = 0;
      previousTicks = 0;
      notes = new DirectList<>();
    }

    private int round(int value, int toNearest) {
      int roundedValue = (((value - (toNearest / 2)) + (toNearest - 1)) / toNearest) * toNearest;
      return roundedValue;
    }

    private void stop() {
      transport.stop();
    }

    private void tempo(int percentTempo) {
      // NB: MusicPad uses 0 to 100% for min to max with 50% for normal
      transport.setPercentTempo(percentTempo / 2);
    }

    private Note toNote(Notable notable) {
      Note note = new NoteBuilder() //
          .withChannel(notable.getChannel()) //
          .withDuration(notable.getDuration()) //
          .withMidiNote(notable.getNote()) //
          .withProgram(notable.getProgram()) //
          .withTick(notable.getTick()) //
          .withVelocity(notable.getVelocity()) //
          .create();
      return note;
    }

    private ResultSet transpose(int song, int amount) {
      return null;
    }
  }

  private static DerbyImpl derbyImpl = new DerbyImpl();

  public static int append(int tick, int note, int duration, int velocity, int program, int channel) {
    return derbyImpl.append(tick, note, duration, velocity, program, channel);
  }

  public static int copy(int firstId, int lastId) {
    return derbyImpl.copy(firstId, lastId);
  }

  public static void play() {
    derbyImpl.play();
  }

  public static String program(int program) {
    return derbyImpl.program(program);
  }

  public static void reset() {
    derbyImpl.reset();
  }

  public static int round(int value, int toNearest) {
    return derbyImpl.round(value, toNearest);
  }

  public static void stop() {
    derbyImpl.stop();
  }

  public static void tempo(int percentTempo) {
    derbyImpl.tempo(percentTempo);
  }

  public static ResultSet transpose(int song, int amount) {
    return derbyImpl.transpose(song, amount);
  }
}
