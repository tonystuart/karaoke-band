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

import com.example.afs.musicpad.task.ServiceTask.Response;
import com.example.afs.musicpad.util.RandomAccessList;

public class MidiFiles implements Response {

  private RandomAccessList<File> midiFiles;

  public MidiFiles(RandomAccessList<File> midiFiles) {
    this.midiFiles = midiFiles;
  }

  public RandomAccessList<File> getMidiFiles() {
    return midiFiles;
  }

}