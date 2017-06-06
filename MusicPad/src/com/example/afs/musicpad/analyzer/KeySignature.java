package com.example.afs.musicpad.analyzer;

import com.example.afs.musicpad.midi.Midi;

public final class KeySignature {
  private String notes;
  private int tonic;
  private int relativeTonic;
  private boolean isMajor;
  private int sharpsOrFlats;

  public KeySignature(String notes, int tonic, int relativeTonic, boolean isMajor, int sharpsOrFlats) {
    this.notes = notes;
    this.tonic = tonic;
    this.relativeTonic = relativeTonic;
    this.isMajor = isMajor;
    this.sharpsOrFlats = sharpsOrFlats;
  }

  // See http://en.wikipedia.org/wiki/Chord_%28music%29

  public KeyScore getKeyScore(int[] chromaticCounts) {
    int naturals = 0;
    int accidentals = 0;
    for (int i = 0; i < chromaticCounts.length; i++) {
      boolean isInKey = notes.charAt(i) == '1';
      if (isInKey) {
        naturals += chromaticCounts[i];
      } else {
        accidentals += chromaticCounts[i];
      }
    }
    int third;
    if (isMajor) {
      third = (tonic + Midi.SEMITONES_TO_MAJOR_THIRD) % Midi.SEMITONES_PER_OCTAVE;
    } else {
      third = (tonic + Midi.SEMITONES_TO_MINOR_THIRD) % Midi.SEMITONES_PER_OCTAVE;
    }
    int perfectFifth = (tonic + Midi.SEMITONES_TO_PERFECT_FIFTH) % Midi.SEMITONES_PER_OCTAVE;
    int tonicThirdCount = Math.min(chromaticCounts[tonic], chromaticCounts[third]);
    int tonicTriadCount = Math.min(tonicThirdCount, chromaticCounts[perfectFifth]);
    KeyScore keyScore = new KeyScore(naturals, accidentals, tonicThirdCount, tonicTriadCount, tonic, relativeTonic, isMajor, sharpsOrFlats);
    return keyScore;
  }
}
