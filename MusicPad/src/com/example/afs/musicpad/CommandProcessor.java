// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad;

import com.example.afs.musicpad.Trace.TraceOption;
import com.example.afs.musicpad.message.OnCommand;
import com.example.afs.musicpad.task.MessageBroker;
import com.example.afs.musicpad.task.MessageTask;

// new things
// zero/enter when not in command mode are modulation down/up1550000550
// contour// save current recording
// clear current recording
// next four measures
// previous four measures
// control
// 1 - play
// 2 - pause/stop
// 3 - stop
// 4 - previous measure
// 5 - next measure
// 6 - loop
// 7 - record
// 8 - previous song
// 9 - next song
// 10 - new clip
// 11 - previous clip
// 12 - next clip
// 13 - transpose to white
// 20 - clip append
// 21 - clip merge
// control, value
// 100 - set song
// 101 - set song page
// 102 - set song index on page
// 103 - set master gain
// 104 - set tempo
// 105 - set note concurrency
// 106 - set note duration
// 107 - set default program
// 108 - set default velocity
// 109 - set loop start (in measures)
// 110 - set loop length (in beats)
// control-range
// 200 to 299 - play next note in track
// 300 to 399 - play note in page
// 400 to 499 - set clip
// 500 to 599 - play clip
// control-range, value
// 600 to 699 - set track program
// 700 to 799 - set track velocity

public class CommandProcessor extends MessageTask {

  public CommandProcessor(MessageBroker broker) {
    super(broker);
    subscribe(OnCommand.class, message -> doCommand(message.getCommand(), message.getParameter()));
  }

  private void doCommand(Command command, int parameter) {
    switch (command) {
    case HELP:
      doHelp();
      break;
    case TRON:
      doTron(parameter);
      break;
    case TROFF:
      doTroff(parameter);
      break;
    case QUIT:
      System.exit(0);
      break;
    default:
      break;
    }
  }

  private void doHelp() {
    for (Command command : Command.values()) {
      System.out.println(command.ordinal() + " -> " + command);
    }
  }

  private void doTroff(int trace) {
    setTrace(trace, false);
  }

  private void doTron(int trace) {
    setTrace(trace, true);
  }

  private TraceOption getTraceOption(int trace) {
    TraceOption traceOption;
    TraceOption[] traceOptions = TraceOption.values();
    if (trace < 0 || trace >= traceOptions.length) {
      traceOption = null;
      for (int i = 0; i < traceOptions.length; i++) {
        System.out.println((i + 1) + " -> " + traceOptions[i]);
      }
    } else {
      traceOption = traceOptions[trace];
    }
    return traceOption;
  }

  private void setTrace(int traceIndex, boolean value) {
    TraceOption traceOption = getTraceOption(traceIndex);
    if (traceOption != null) {
      switch (traceOption) {
      case COMMAND:
        Trace.setTraceCommand(value);
        break;
      case CONFIGURATION:
        Trace.setTraceConfiguration(value);
        break;
      case PLAY:
        Trace.setTracePlay(value);
        break;
      default:
        throw new UnsupportedOperationException();
      }
    }
  }

}
