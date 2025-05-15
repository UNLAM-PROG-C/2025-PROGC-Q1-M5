import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimatedSprite extends JLabel {
    private ImageIcon[] frames;
    private int currentFrame = 0;
    private final Timer animationTimer;
    private boolean isAnimating = false;
    private boolean loop = false;
    private String[] newAnimationFrames = null;

    public AnimatedSprite(String[] imagePaths, int delayMillis) {
        frames = new ImageIcon[imagePaths.length];
        for (int i = 0; i < imagePaths.length; i++) {
            frames[i] = new ImageIcon(imagePaths[i]);
        }

        setIcon(frames[0]);
        setSize(frames[0].getIconWidth(), frames[0].getIconHeight());

        animationTimer = new Timer(delayMillis, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFrame++;

                if (currentFrame >= frames.length) {
                    if (newAnimationFrames != null) {
                        startNewAnimation(newAnimationFrames);
                        newAnimationFrames = null;
                        return;
                    }

                    if (loop) {
                        currentFrame = 0;
                    } else {
                        stop(); // detiene la animación
                        return;
                    }
                }

                setIcon(frames[currentFrame]);
            }
        });
    }

    public void setLoop(boolean loop) {
    this.loop = loop;
    }

    public void start() {
        if (!isAnimating) {
            isAnimating = true;
            animationTimer.start();
        }
    }

    public void stop() {
        if (isAnimating) {
            animationTimer.stop();
            isAnimating = false;
        }
    }

    public void changeAnimation(String[] newImagePaths) {
        if (isAnimating) {
            newAnimationFrames = newImagePaths;
        } else {
            startNewAnimation(newImagePaths);
        }
    }

    private void startNewAnimation(String[] newImagePaths) {
        frames = new ImageIcon[newImagePaths.length];
        for (int i = 0; i < newImagePaths.length; i++) {
            frames[i] = new ImageIcon(newImagePaths[i]);
        }

        currentFrame = 0;
        setIcon(frames[currentFrame]);
    }

    public void destroy() {
    stop();                  // Detiene animación
    animationTimer.stop();  // Asegura detener Timer
    animationTimer.setRepeats(false); // Ya no repite más
    }
}
