// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.midi.configuration;

import com.example.afs.musicpad.Command;

public class ThenSendHandlerCommand extends Then {

  public ThenSendHandlerCommand(int lineIndex, String[] tokens) {
    super(lineIndex, tokens);
    if (tokens.length != 3) {
      throw new IllegalArgumentException(formatMessage("Expected sendHandlerCommand command parameter"));
    }
  }

  @Override
  public void executeThen(Context context) {
    Command command = context.get(Command.class, tokens[1]);
    int parameter = (int) context.getRight(tokens[2]);
    context.getHasSendHandlerCommand().sendHandlerCommand(command, parameter);
  }

}