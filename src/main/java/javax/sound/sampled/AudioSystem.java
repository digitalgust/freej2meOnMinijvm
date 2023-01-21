package javax.sound.sampled;

import java.io.InputStream;

public class AudioSystem {

    public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding,
                                                       AudioInputStream sourceStream) {
        return new AudioInputStream();
    }

    public static AudioInputStream getAudioInputStream(InputStream stream){
        return new AudioInputStream();
    }


    public static Clip getClip(){
        return new Clip();
    }
}
