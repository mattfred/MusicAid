package com.mattfred.musicaid.metronome;

import com.mattfred.musicaid.audio.PitchMaker;

/**
 * Created by matthewfrederick on 8/4/15.
 */
public class MetronomePlayer
{
    private double bpm;
    private int beat;
    private int noteValue;
    private int silence;

    private double uBeat;
    private double dBeat;
    private final int tick = 1000; // samples of tick

    private boolean play = true;

    private final PitchMaker pitchMaker = new PitchMaker(8000);

    public MetronomePlayer() {
        pitchMaker.createPlayer();
    }

    public void calcSilence() {
        silence = (int) (((60/bpm)*8000)-tick);
    }

    public void play() {
        calcSilence();
        double[] tick =
                pitchMaker.getSineWave(this.tick, 8000, uBeat);
        double[] tock =
                pitchMaker.getSineWave(this.tick, 8000, dBeat);
        double silence = 0;
        double[] sound = new double[8000];
        int t = 0,s = 0,b = 0;
        do {
            for(int i=0;i<sound.length&&play;i++) {
                if(t<this.tick) {
                    if(b == 0)
                        sound[i] = tock[t];
                    else
                        sound[i] = tick[t];
                    t++;
                } else {
                    sound[i] = silence;
                    s++;
                    if(s >= this.silence) {
                        t = 0;
                        s = 0;
                        b++;
                        if(b > (this.beat-1))
                            b = 0;
                    }
                }
            }
            pitchMaker.writeSound(sound);
        } while(play);
    }

    public void stop() {
        play = false;
        pitchMaker.destroyAudioTrack();
    }

    public double getBpm()
    {
        return bpm;
    }

    public void setBpm(double bpm)
    {
        this.bpm = bpm;
    }

    public int getBeat()
    {
        return beat;
    }

    public void setBeat(int beat)
    {
        this.beat = beat;
    }

    public int getNoteValue()
    {
        return noteValue;
    }

    public void setNoteValue(int noteValue)
    {
        this.noteValue = noteValue;
    }

    public int getSilence()
    {
        return silence;
    }

    public void setSilence(int silence)
    {
        this.silence = silence;
    }

    public double getuBeat()
    {
        return uBeat;
    }

    public void setuBeat(double uBeat)
    {
        this.uBeat = uBeat;
    }

    public double getdBeat()
    {
        return dBeat;
    }

    public void setdBeat(double dBeat)
    {
        this.dBeat = dBeat;
    }

    public int getTick()
    {
        return tick;
    }

    public boolean isPlay()
    {
        return play;
    }

    public void setPlay(boolean play)
    {
        this.play = play;
    }
}
