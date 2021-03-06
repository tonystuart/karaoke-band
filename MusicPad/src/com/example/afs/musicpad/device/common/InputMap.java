// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.common;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

public class InputMap {
  private int[] inputCodes;
  private String[] labels;

  public InputMap(int[] inputCodes, String[] legends) {
    this.inputCodes = inputCodes;
    this.labels = legends;
  }

  public InputMap(String s) {
    int length = s.length();
    inputCodes = new int[length];
    labels = new String[length];
    for (int index = 0; index < length; index++) {
      char c = s.charAt(index);
      inputCodes[index] = c;
      labels[index] = (c >= ' ' && c <= '~') ? String.valueOf(c) : KeyEvent.getKeyText(c);
    }
  }

  public InputMap(TreeMap<Integer, String> map) {
    int size = map.size();
    inputCodes = new int[size];
    labels = new String[size];
    int index = 0;
    for (Entry<Integer, String> entry : map.entrySet()) {
      inputCodes[index] = entry.getKey();
      labels[index] = entry.getValue();
      index++;
    }
  }

  public int[] getInputCodes() {
    return inputCodes;
  }

  public String getLabel(int index) {
    return labels[index];
  }

  public String[] getLabels() {
    return labels;
  }

  // TODO: Optimize if necessary
  public int indexOf(int inputCode) {
    for (int i = 0; i < inputCodes.length; i++) {
      if (inputCodes[i] == inputCode) {
        return i;
      }
    }
    return -1;
  }

  public int size() {
    return inputCodes.length;
  }

  @Override
  public String toString() {
    return "InputMap [inputCodes=" + Arrays.toString(inputCodes) + ", legends=" + Arrays.toString(labels) + "]";
  }
}