// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.util;

public class Velocity {

  // https://sourceforge.net/p/fluidsynth/wiki/FluidSettings/
  public static final float DEFAULT_GAIN = 1f;

  public static int scale(int velocity, int percent) {
    int scaledVelocity = (velocity * percent) / 100;
    if (scaledVelocity < 0) {
      scaledVelocity = 0;
    } else if (scaledVelocity > 127) {
      scaledVelocity = 127;
    }
    return scaledVelocity;
  }

  public static float scalePercentGain(int percentGain) {
    float scaledGain = (DEFAULT_GAIN * percentGain) / 100;
    return scaledGain;
  }

}