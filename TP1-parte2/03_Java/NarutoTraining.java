import java.util.Random;

public class NarutoTraining
{

    static final int FAILURE_RATIO = 25;
    static final int CLONES_QUANTITY = 15;
    static int[] clonesLevels;

    public static class TrainingKageBunshin extends Thread
    {
        private final int position;
        private final int chakra;

        public TrainingKageBunshin(int pos)
        {
            this.position = pos;
            this.chakra = new Random().nextInt(6) + 5;
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
            for (int i = chakra; i > 0; i--)
            {
                Thread.sleep(rand.nextInt(100) + 100);
                int success = rand.nextInt(101);
                if (success > FAILURE_RATIO)
                {
                    clonesLevels[position]++;
                }
            }

            String nivelStr = (clonesLevels[position] == 1) ? "nivel" : "niveles";
            System.out.println("Clon #" + String.format("%03d", position)
                + " subió " + clonesLevels[position] + " " + nivelStr);
        }
    }

    private static int contarNivelesTotales()
    {
        int total = 0;
        for (int lvl : clonesLevels) total += lvl;
        return total;
    }

    public static void main(String[] args) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        int clonesQuantity;

        if (args.length == 0)
        {
            System.out.println("Se usarán los valores por defecto.");
            clonesQuantity = CLONES_QUANTITY;
        } else
        {
            clonesQuantity = Integer.parseInt(args[0]);
            if (clonesQuantity <= 0 || clonesQuantity >= 108)
            {
                System.out.println("La cantidad de clones debe ser mayor que 0 y menor que 108.\nSe usarán los valores por defecto.");
                clonesQuantity = CLONES_QUANTITY;
            }
        }

        clonesLevels = new int[clonesQuantity];
        System.out.println("==== INICIO DEL ENTRENAMIENTO DE LOS CLONES ====");

        Thread[] clones = new Thread[clonesQuantity];
        for (int i = 0; i < clonesQuantity; i++)
        {
            clones[i] = new TrainingKageBunshin(i);
            clones[i].start();
        }

        for (Thread t : clones)
        {
            t.join();
        }

        System.out.println("==== FIN DEL ENTRENAMIENTO DE LOS CLONES ====");

        int total = contarNivelesTotales();
        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        String totalStr = (total == 1) ? "nivel" : "niveles";
        System.out.println("\n========================================");
        System.out.println("¡Naruto subió " + total + " " + totalStr + "!");
        System.out.printf("Tiempo total de entrenamiento: %.2f segundos\n", seconds);
        System.out.println("========================================\n");
    }
}
