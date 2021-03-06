// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.util;

public class Value {

  public static int getInt(Integer value) {
    return Value.getInt(value, 0);
  }

  public static int getInt(Integer value, int defaultValue) {
    return value == null ? defaultValue : value;
  }

  public static int toIndex(int number) {
    return number - 1;
  }

  public static int toNumber(int index) {
    return index + 1;
  }

}
