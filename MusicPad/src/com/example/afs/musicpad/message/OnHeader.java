// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.message;

public class OnHeader extends Message {

  private String title;
  private String html;

  public OnHeader(String title, String html) {
    this.title = title;
    this.html = html;
  }

  public String getHtml() {
    return html;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return "OnHeader [title=" + title + ", html=" + html + "]";
  }

}