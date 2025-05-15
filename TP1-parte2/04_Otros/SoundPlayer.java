import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer
{
    private Clip audioClip;
    private FloatControl volumeControl;

    public void playSound(String filepath)
    {
        try
        {
            File soundFile = new File(filepath);
            if (!soundFile.exists())
            {
                System.err.println("Archivo no encontrado: " + filepath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);

            audioClip.addLineListener(event ->{
                if (event.getType() == LineEvent.Type.STOP) 
                {
                    audioClip.close();
                }
            });

            audioClip.open(audioStream);

            if (audioClip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            {
                volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            }

            audioClip.start();

            while (!audioClip.isRunning()) Thread.sleep(10);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    public void fadeOut(int duracionMs)
    {
        if (audioClip == null || volumeControl == null) return;

        new Thread(() -> {
            try
            {
                float max = volumeControl.getMaximum();
                float min = volumeControl.getMinimum();
                float step = (max - min) / 20;
                int delay = duracionMs / 20;

                for (int i = 0; i < 20; i++)
                {
                    float newVolume = volumeControl.getValue() - step;
                    volumeControl.setValue(Math.max(newVolume, min));
                    Thread.sleep(delay);
                }

                audioClip.stop();
                audioClip.close();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }).start();
    }
}
