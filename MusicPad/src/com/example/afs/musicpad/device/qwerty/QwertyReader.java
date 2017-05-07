// Copyright 2017 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.afs.musicpad.device.qwerty;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;

import com.example.afs.fluidsynth.FluidSynth;
import com.example.afs.musicpad.device.common.CommandBuilder;
import com.example.afs.musicpad.device.common.DeviceHandler;
import com.example.afs.musicpad.message.Message;
import com.example.afs.musicpad.util.ByteArray;

// See /usr/include/linux/input.h
// See https://www.kernel.org/doc/Documentation/input/input.txt

public class QwertyReader {

  private static final int EV_KEY = 0x01;

  private DeviceHandler deviceHandler;
  private Thread deviceReader;
  private boolean isTerminated;
  private CommandBuilder commandBuilder;

  public QwertyReader(BlockingQueue<Message> queue, DeviceHandler deviceHandler) {
    this.deviceHandler = deviceHandler;
    this.commandBuilder = new CommandBuilder(queue, deviceHandler);
  }

  public void capture(FileInputStream fileInputStream) {
    try {
      Field f = FileDescriptor.class.getDeclaredField("fd");
      if (f != null) {
        f.setAccessible(true);
        Integer fd = (Integer) f.get(fileInputStream.getFD());
        if (fd != null) {
          // See EVIOCGRAB in drivers/input/evdev.c
          FluidSynth.capture(fd, 1);
        }
      }
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int processKeyDown(short keyCode) {
    int ignoreKeyUp;
    if (keyCode < QwertyKeyCodes.inputCodes.length) {
      char inputCode = QwertyKeyCodes.inputCodes[keyCode];
      ignoreKeyUp = commandBuilder.processKeyDown(inputCode);
    } else {
      // e.g. windows meta key (125)
      ignoreKeyUp = 1;
    }
    return ignoreKeyUp;
  }

  public void processKeyUp(short keyCode) {
    char inputCode = QwertyKeyCodes.inputCodes[keyCode];
    commandBuilder.processKeyUp(inputCode);
  }

  public void start() {
    deviceReader = new Thread(() -> run(), deviceHandler.getName());
    deviceReader.start();
  }

  public void terminate() {
    isTerminated = true;
  }

  private void run() {
    try (FileInputStream fileInputStream = new FileInputStream(deviceHandler.getName())) {
      capture(fileInputStream);
      int ignoreKeyUp = 0;
      byte[] buffer = new byte[16];
      while (!isTerminated) {
        try {
          fileInputStream.read(buffer);
          short type = ByteArray.toNativeShort(buffer, 8);
          //System.out.printf("buffer=%s, type=%#x, code=%#x, value=%#x\n", Arrays.toString(buffer), type, code, value);
          if (type == EV_KEY) {
            int value = ByteArray.toNativeInteger(buffer, 12);
            if (value == 0) {
              short code = ByteArray.toNativeShort(buffer, 10);
              if (ignoreKeyUp > 0) {
                ignoreKeyUp--;
              } else {
                processKeyUp(code);
              }
            } else if (value == 1) {
              short code = ByteArray.toNativeShort(buffer, 10);
              ignoreKeyUp += processKeyDown(code);
            }
          }
        } catch (RuntimeException e) {
          e.printStackTrace();
          System.err.println("Ignoring exception");
        }
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
    System.out.println("Terminating QWERTY device " + deviceHandler);
  }

}
