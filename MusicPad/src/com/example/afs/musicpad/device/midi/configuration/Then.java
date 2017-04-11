// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.midi.configuration;

public abstract class Then extends Node {

  public Then(int lineIndex, String[] tokens) {
    super(lineIndex, tokens);
  }

  @Override
  public ReturnState execute(Context context) {
    if (context.isTrace()) {
      System.out.println("Executing " + this);
    }
    executeThen(context);
    return ReturnState.UNCONDITIONAL;
  }

  public abstract void executeThen(Context context);

}