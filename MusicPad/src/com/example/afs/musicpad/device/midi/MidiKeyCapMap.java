// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.midi;

import com.example.afs.musicpad.keycap.KeyCap;
import com.example.afs.musicpad.keycap.KeyCapMap;
import com.example.afs.musicpad.player.Sound;
import com.example.afs.musicpad.song.Note;
import com.example.afs.musicpad.util.DirectList;
import com.example.afs.musicpad.util.RandomAccessList;

public class MidiKeyCapMap implements KeyCapMap {

  public MidiKeyCapMap() {
  }

  @Override
  public RandomAccessList<KeyCap> getKeyCaps() {
    return new DirectList<>();
  }

  @Override
  public Sound onDown(int inputCode) {
    return new Sound(new Note.NoteBuilder().withMidiNote(inputCode).create());
  }

  @Override
  public void onUp(int inputCode) {
  }

}