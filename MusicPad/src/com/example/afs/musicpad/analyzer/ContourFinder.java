// Copyright 2016 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.analyzer;

import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.example.afs.musicpad.midi.Midi;
import com.example.afs.musicpad.song.Contour;
import com.example.afs.musicpad.song.Default;
import com.example.afs.musicpad.song.Note;

// TODO: Integrate with Group
// TODO: Use common note to eliminate effect of chord inversion

public class ContourFinder {

  public TreeSet<Contour> getContours(NavigableSet<Note> notes) {
    TreeSet<TickEvent> tickEvents = new TreeSet<TickEvent>();
    TreeSet<Contour> contours = new TreeSet<>();
    for (Note note : notes) {
      if (note.getChannel() != Midi.DRUM) {
        TickEvent noteOnEvent = new TickEvent(note);
        TickEvent noteOffEvent = new TickEvent(note, noteOnEvent);
        tickEvents.add(noteOnEvent);
        tickEvents.add(noteOffEvent);
      }
    }
    ArrayList<TickEvent> activeNotes = new ArrayList<>();
    TickEvent currentTickEvent = null;
    long xtick = 0;
    for (TickEvent tickEvent : tickEvents) {
      TickEvent associatedNoteOnEvent = tickEvent.getNoteOnEvent();
      if (associatedNoteOnEvent == null) {
        activeNotes.add(tickEvent);
      } else {
        activeNotes.remove(associatedNoteOnEvent);
      }
      TickEvent highestTickEvent = findHighestTickEvent(activeNotes, tickEvent.getTick());
      if (highestTickEvent != currentTickEvent) {
        if (currentTickEvent != null) {
          long xduration = tickEvent.getTick() - xtick;
          if (xduration > Default.TICKS_PER_BEAT / 8) {
            contours.add(new Contour(xtick, currentTickEvent.getNote().getMidiNote(), xduration));
          }
        }
        xtick = tickEvent.getTick();
        currentTickEvent = highestTickEvent;
      }
    }
    return contours;
  }

  private TickEvent findHighestTickEvent(ArrayList<TickEvent> activeNotes, long tick) {
    TickEvent highestTickEvent = null;
    for (TickEvent tickEvent : activeNotes) {
      long beginTick = tickEvent.getTick();
      long endTick = tickEvent.getEndTick();
      boolean inRange = beginTick <= tick && tick < endTick;
      if (highestTickEvent == null || (inRange && tickEvent.getNote().getMidiNote() > highestTickEvent.getNote().getMidiNote())) {
        highestTickEvent = tickEvent;
      }
    }
    return highestTickEvent;
  }

}