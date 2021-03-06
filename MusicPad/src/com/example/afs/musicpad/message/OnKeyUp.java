// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.message;

import com.example.afs.musicpad.player.Sound;

public class OnKeyUp extends TypedMessage {

  private int deviceIndex;

  private Sound sound;

  public OnKeyUp(int deviceIndex, Sound sound) {
    this.deviceIndex = deviceIndex;
    this.sound = sound;
  }

  public int getDeviceIndex() {
    return deviceIndex;
  }

  public Sound getSound() {
    return sound;
  }

  @Override
  public String toString() {
    return "OnKeyUp [deviceIndex=" + deviceIndex + ", sound=" + sound + "]";
  }

}
