// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.html;

public class TextElement extends Node {

  private String text;

  public TextElement(Object value) {
    this.text = value.toString();
  }

  public String getText() {
    return text;
  }

  @Override
  public void render(StringBuilder s) {
    s.append(text);
  }

  @Override
  public String toString() {
    return "TextElement [text=" + text + "]";
  }

}