// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.message;

public class OnDeviceAttached extends TypedMessage {

  private int deviceIndex;

  public OnDeviceAttached(int deviceIndex) {
    this.deviceIndex = deviceIndex;
  }

  public int getDeviceIndex() {
    return deviceIndex;
  }

  @Override
  public String toString() {
    return "OnDeviceAttached [deviceIndex=" + deviceIndex + "]";
  }

}
