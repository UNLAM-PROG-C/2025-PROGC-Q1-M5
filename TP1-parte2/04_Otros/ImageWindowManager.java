import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImageWindowManager
{
    private final JFrame frame;
    private final JLayeredPane layeredPane;
    private final List<Timer> timers = new ArrayList<>();

    public ImageWindowManager(String backgroundPath, int width, int height)
    {
        frame = new JFrame("Naruto Training");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setResizable(false);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(width, height));

        ImageIcon bgIcon = new ImageIcon(backgroundPath);
        JLabel bgLabel = new JLabel(bgIcon);
        bgLabel.setBounds(0, 0, width, height);
        layeredPane.add(bgLabel, JLayeredPane.DEFAULT_LAYER);

        frame.setContentPane(layeredPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void addSprite(AnimatedSprite sprite, int x, int y)
    {
        sprite.setLocation(x, y);
        layeredPane.add(sprite, JLayeredPane.PALETTE_LAYER);
        sprite.start();
        layeredPane.repaint();
    }

    public void moveSprite(AnimatedSprite sprite, int dx, int dy, int velocity, int duration) {
        Timer timer = new Timer(velocity, null);
        timers.add(timer);
        final int[] elapsedTime = {0};

        timer.addActionListener(e -> {
            if (elapsedTime[0] >= duration) {
                timer.stop();
                return;
            }

            Point position = sprite.getLocation();
            sprite.setLocation(position.x + dx, position.y + dy);
            layeredPane.repaint();

            elapsedTime[0] += velocity;
        });

        timer.start();
    }

    public void closeWindow() {
        for (Timer timer : timers) {
            timer.stop();
        }
        timers.clear();

        layeredPane.removeAll();
        layeredPane.repaint();
        frame.dispose();
    }
}
