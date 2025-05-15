import java.util.Random;

public class NarutoTraining 
{
    
    static final int FAILURE_RATIO = 25;
    static final int CLONES_QUANTITY = 107;
    static int[] clonesLevels;
    static final ImageWindowManager window = new ImageWindowManager("BG.png", 540, 412);

    public static final String[] CHARGE = { "Naruto/C1.png", "Naruto/C2.png", "Naruto/C3.png", "Naruto/C4.png" };
    public static final String[] DESPAWN = { "Naruto/D1.png", "Naruto/D2.png", "Naruto/D3.png", "Naruto/D4.png", "Naruto/D5.png", "Naruto/D6.png", "Naruto/D7.png", "Naruto/D8.png", "Naruto/D9.png", "Naruto/D10.png" };
    public static final String[] FAIL = { "Naruto/F1.png", "Naruto/F2.png", "Naruto/F3.png", "Naruto/F4.png", "Naruto/F5.png", "Naruto/F6.png", "Naruto/F7.png", "Naruto/F8.png", "Naruto/F9.png", "Naruto/F10.png", "Naruto/F11.png", "Naruto/F12.png" };
    public static final String[] SPAWN = { "Naruto/K1.png", "Naruto/K2.png", "Naruto/K3.png", "Naruto/K4.png", "Naruto/K5.png", "Naruto/K6.png", "Naruto/O5.png" };
    public static final String[] ORIGINAL = { "Naruto/O1.png", "Naruto/O2.png", "Naruto/O3.png", "Naruto/O4.png", "Naruto/O5.png" };
    public static final String[] SUCCESS = { "Naruto/S1.png", "Naruto/S2.png", "Naruto/S3.png", "Naruto/S4.png", "Naruto/S5.png", "Naruto/S6.png", "Naruto/S7.png", "Naruto/S8.png", "Naruto/S9.png", "Naruto/S10.png", "Naruto/S11.png" };
    public static final String[] RASENGAN = { "Naruto/R1.png", "Naruto/R2.png", "Naruto/R3.png", "Naruto/R4.png", "Naruto/R5.png", "Naruto/R6.png", "Naruto/R7.png", "Naruto/R8.png", "Naruto/R9.png", "Naruto/R10.png", "Naruto/R11.png", "Naruto/R12.png", "Naruto/R13.png" };
    public static final String[] DISAPPEAR = { "Naruto/D9.png" };

    public static class TrainingKageBunshin extends Thread
    {
        private final int position, chakra, x, y;
        private final AnimatedSprite cloneSprite;

        public TrainingKageBunshin(int pos, int x, int y)
        {
            this.position = pos;
            this.chakra = new Random().nextInt(6) + 5;
            this.x = x;
            this.y = y;
            this.cloneSprite = new AnimatedSprite(SPAWN, 100);
            cloneSprite.setLoop(false);
        }

        public void run()
        {
            try
            {
                training();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        private void training() throws InterruptedException
        {
            Random rand = new Random();
            SoundPlayer player = new SoundPlayer();
            window.addSprite(cloneSprite, x, y);
            Thread.sleep(rand.nextInt(3000) + 1000);

            cloneSprite.setLoop(true);
            cloneSprite.changeAnimation(CHARGE);
            cloneSprite.start();

            for (int i = chakra; i > 0; i--)
            {
                Thread.sleep(rand.nextInt(3000) + 1000);
                int success = rand.nextInt(101);

                cloneSprite.setLoop(false);
                if (success > FAILURE_RATIO)
                {
                    clonesLevels[position]++;
                    cloneSprite.changeAnimation(SUCCESS);
                } else
                {
                    cloneSprite.changeAnimation(FAIL);
                }

                Thread.sleep(rand.nextInt(1000));
                cloneSprite.setLoop(true);
                cloneSprite.changeAnimation(CHARGE);
            }

            String nivelStr = (clonesLevels[position] == 1) ? "nivel" : "niveles";
            System.out.println("Clon #" + String.format("%03d", position)
                + " subio " + clonesLevels[position] + " " + nivelStr);
            cloneSprite.setLoop(false);
            cloneSprite.changeAnimation(DESPAWN);
            player.playSound("sfx/Despawn.wav");
            Thread.sleep(5000);
            cloneSprite.destroy();
        }
    }

    public static SoundPlayer playSound(String ruta)
    {
        SoundPlayer player = new SoundPlayer();
        new Thread(() -> player.playSound(ruta)).start();
        return player;
    }

    private static int levelsCount()
    {
        int total = 0;
        for (int lvl : clonesLevels) total += lvl;
        return total;
    }

    public static void main(String[] args) throws InterruptedException, NumberFormatException
    {
        long startTime = System.currentTimeMillis();
        int clonesQuantity;
        if (args.length == 0)
        {
            System.out.println("Se usaran los valores por defecto.");
            clonesQuantity = CLONES_QUANTITY;
        }
        else
        {
            clonesQuantity = Integer.parseInt(args[0]);
            if (clonesQuantity <= 0 || clonesQuantity >= 108)
            {
                System.out.println("La cantidad de clones debe ser mayor que 0 y menor que 108.\nSe usaran los valores por defecto.");
                clonesQuantity = CLONES_QUANTITY;
            }
        }
        clonesLevels = new int[clonesQuantity];
        SoundPlayer ost = playSound("sfx/Complete.wav");
        AnimatedSprite spriteOriginal = mostrarNarutoOriginal();
        System.out.println("==== INICIO DEL ENTRENAMIENTO DE LOS CLONES ====");
        Thread[] clones = initClones(clonesQuantity);
        SoundPlayer ChakraLong = playSound("sfx/ChakraLong.wav");
        waitClones(clones);
        ChakraLong.fadeOut(1000);
        System.out.println("==== FIN DEL ENTRENAMIENTO DE LOS CLONES ====\n");
        finalizarEntrenamiento(spriteOriginal, ost, startTime);
    }

    private static AnimatedSprite mostrarNarutoOriginal() throws InterruptedException
    {
        AnimatedSprite sprite = new AnimatedSprite(ORIGINAL, 100);
        window.addSprite(sprite, 181, 149);
        sprite.stop();
        Thread.sleep(3800);
        sprite.start();
        Thread.sleep(1500);
        return sprite;
    }

    private static Thread[] initClones(int clonesQuantity)
    {
        int spriteWidth = 30, spriteHeight = 45, separation = 3;
        int windowWidth = 540, windowHeight = 412, startY = 50;
        int maxPerRow = Math.max(1, (windowWidth - separation) / (spriteWidth + separation));
        int narutoColumn = 6;
        int narutoRow = 2;
        int narutoIndex = narutoRow * maxPerRow + narutoColumn;
        int overNaruto = narutoIndex - maxPerRow;
        int underNaruto = narutoIndex + maxPerRow;
        int leftNaruto = narutoIndex - 1;
        int rightNaruto = narutoIndex + 1;

        Thread[] clones = new Thread[clonesQuantity];
        int count = 0;
        for (int i = 0; count < clonesQuantity; i++)
        {
            if (i == narutoIndex || i == overNaruto || i == underNaruto || i == leftNaruto || i == rightNaruto)
                continue;

            int columna = i % maxPerRow;
            int fila = i / maxPerRow;
            int x = separation + columna * (spriteWidth + separation) - 20;
            int y = startY + separation + fila * (spriteHeight + separation);
            clones[count] = new TrainingKageBunshin(count, x, y);
            clones[count].start();
            count++;
        }
        return clones;
    }

    private static void waitClones(Thread[] clones) throws InterruptedException
    {
        for (Thread t : clones)
        {
            t.join();
        }
    }

    private static void finalizarEntrenamiento(AnimatedSprite sprite, SoundPlayer ost, long startTime) throws InterruptedException
    {
        Thread.sleep(600);
        sprite.setLoop(true);
        sprite.start();
        sprite.changeAnimation(CHARGE);
        playSound("sfx/ChakraShort.wav");
        int total = levelsCount();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double seconds = duration / 1000.0;
        String totalStr = (total == 1) ? "nivel" : "niveles";
        System.out.println("\n========================================");
        System.out.println("¡Naruto subió " + total + " " + totalStr + "!");
        System.out.printf("Tiempo total de entrenamiento: %.2f segundos\n", seconds);
        System.out.println("========================================\n");

        Thread.sleep(1500);
        
        SoundPlayer rasengan = playSound("sfx/Rasengan.wav");
        sprite.changeAnimation(DISAPPEAR);
        sprite.start();
        AnimatedSprite sprite2 = new AnimatedSprite(RASENGAN, 100);
        window.addSprite(sprite2, 181, 149);
        Thread.sleep(500);
        window.moveSprite(sprite2, 15, 0, 50, 2000);
        ost.fadeOut(1000);
        Thread.sleep(2500);
        sprite.destroy();
        sprite2.destroy();
        window.closeWindow();
    }
}