// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.song;

import java.util.LinkedHashSet;
import java.util.Set;

import com.example.afs.musicpad.midi.Midi;

public class Facet {

  private int occupancy;
  private int concurrency;
  private int totalNoteCount;
  private int highestMidiNote;
  private int lowestMidiNote = Midi.NOTES;
  private Set<Integer> programs = new LinkedHashSet<>();
  private int[] distinctNoteCounts = new int[Midi.NOTES];
  private int[] commonNoteCounts = new int[Midi.SEMITONES_PER_OCTAVE];

  public void addProgram(int program) {
    this.programs.add(program);
  }

  public void countNote(int midiNote) {
    totalNoteCount++;
    distinctNoteCounts[midiNote]++;
    commonNoteCounts[midiNote % Midi.SEMITONES_PER_OCTAVE]++;
    if (midiNote < lowestMidiNote) {
      lowestMidiNote = midiNote;
    }
    if (midiNote > highestMidiNote) {
      highestMidiNote = midiNote;
    }
  }

  public int[] getCommonNoteCounts() {
    return commonNoteCounts;
  }

  public int getConcurrency() {
    return concurrency;
  }

  public int[] getDistinctNoteCounts() {
    return distinctNoteCounts;
  }

  public int getHighestMidiNote() {
    return totalNoteCount == 0 ? -1 : highestMidiNote;
  }

  public int getLowestMidiNote() {
    return totalNoteCount == 0 ? -1 : lowestMidiNote;
  }

  public int getOccupancy() {
    return occupancy;
  }

  public Set<Integer> getPrograms() {
    return programs;
  }

  public int getTotalNoteCount() {
    return totalNoteCount;
  }

  public void incrementTotalNoteCount() {
    this.totalNoteCount++;
  }

  public void setConcurrency(int concurrency) {
    this.concurrency = concurrency;
  }

  public void setOccupancy(int occupancy) {
    this.occupancy = occupancy;
  }
}
