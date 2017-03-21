// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.theory;

public class ScaleBasedChordType extends ChordType {

  public ScaleBasedChordType(Key key, String name, int degree) {
    super(name, key.getMidiNotes()[degree - 1 + 0], key.getMidiNotes()[degree - 1 + 2], key.getMidiNotes()[degree - 1 + 4]);
  }
}