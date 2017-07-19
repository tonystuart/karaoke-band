// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.message;

import com.example.afs.musicpad.song.Song;

public class OnSampleSong extends Message {

  private Song song;
  private int ticksPerPixel;

  public OnSampleSong(Song song, int ticksPerPixel) {
    this.song = song;
    this.ticksPerPixel = ticksPerPixel;
  }

  public Song getSong() {
    return song;
  }

  public int getTicksPerPixel() {
    return ticksPerPixel;
  }

  @Override
  public String toString() {
    return "OnSampleSong [song=" + song + ", ticksPerPixel=" + ticksPerPixel + "]";
  }

}