import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random; 

public class TruckOperations
{

    private static Semaphore loadFlourSemaphore = new Semaphore(1);
    private static Semaphore unloadFlourSemaphore = new Semaphore(1);
    private static Semaphore loadCoalSemaphore = new Semaphore(1);
    private static Semaphore unloadCoalSemaphore = new Semaphore(1);
    private static Semaphore loadFuelSemaphore = new Semaphore(2);
    private static Semaphore truckAvailable;
    private final static Integer FLOURTRUCK = 0;
    private final static Integer COALTRUCK = 1;
    private static int[] travels;
    private static Integer MILLISECONDS = 1000;

    public static void main(String[] args) throws InterruptedException
    {

        long startTime = System.currentTimeMillis(); 
        truckAvailable = trucksInitiate(args);

        if ( truckAvailable == null)
        {
            return;
        }

        travels = travelSimulator();

        System.out.println("=========Inicio de operaciones.=========");
        System.out.println("Cantidad de operaciones: " + travels.length);

        Thread[] threads = processTravels(travels);

        waitThreads(threads);

        System.out.println("=========Fin de operaciones.=========");

        printElapsedTime(startTime);

    }

    public static void printElapsedTime(long startTime)
    {
        long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime - startTime;
        double elapsedSeconds = elapsedMillis / 1000.0;
        double simulatedHours = elapsedSeconds;
        double simulatedDays = simulatedHours / 24.0;
    
        System.out.printf("Tiempo total transcurrido: %.2f segundos reales.%n", elapsedSeconds);
        System.out.printf("Tiempo simulado transcurrido: %.2f horas simuladas.%n", simulatedHours);
        System.out.printf("Tiempo simulado requerido: %.4f días simulados.%n", simulatedDays);
    }
    

    public static void waitThreads(Thread[] threads) throws InterruptedException
    {
        for (Thread t : threads)
        {
            t.join();
        }
    }

    public static Thread[] processTravels(int[] travels) throws InterruptedException
    {
        Thread[] threads = new Thread[travels.length];
        
        for (int i = 0; i < travels.length; i++)
        {
            final int index = i;
            Thread t = new Thread(() -> 
            {
                try
                {
                    if (travels[index] == FLOURTRUCK)
                    {
                        truckTripFlour();
                    }
                    else
                    {
                        truckTripCoal();
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            });
            t.start();
            threads[i] = t;
        }
        
        return threads;
    }

    public static int[] travelSimulator()
    {
        Random random = new Random();

        int size = random.nextInt(20) + 1;
        int[] vector = new int[size];

        for (int i = 0; i < size; i++)
        {
            vector[i] = random.nextBoolean() ? FLOURTRUCK : COALTRUCK;
        }

        return vector;
    }

    public static Semaphore trucksInitiate(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Uso: java TruckOperations <cantidad_camiones>");
            return null;
        }

        int trucks;
        try
        {
            trucks = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("El argumento debe ser un número entero.");
            return null;
        }

        if(trucks < 0)
        {
            System.out.println("¿A donde viste camiones negativos?");
            return null;
        }

        if(trucks == 0)
        {
            System.out.println("El argumento debe ser mayor a 0.");
            return null;
        }

        return new Semaphore(trucks);
    }

    public static void loadFlour() throws InterruptedException
    {
        loadFlourSemaphore.acquire();
        Thread.sleep(2 * MILLISECONDS);
        loadFlourSemaphore.release();
    }

    public static void unloadFlour() throws InterruptedException
    {
        unloadFlourSemaphore.acquire();
        Thread.sleep(2 * MILLISECONDS);
        unloadFlourSemaphore.release();
    }

    public static void loadCoal() throws InterruptedException
    {
        loadCoalSemaphore.acquire();
        Thread.sleep(2 * MILLISECONDS);
        loadCoalSemaphore.release();
    }

    public static void unloadCoal() throws InterruptedException
    {
        unloadCoalSemaphore.acquire();
        Thread.sleep(2 * MILLISECONDS);
        unloadCoalSemaphore.release();
    }

    public static void loadFuel() throws InterruptedException
    {
        loadFuelSemaphore.acquire();
        Thread.sleep(1 * MILLISECONDS);
        loadFuelSemaphore.release();
    }

    public static void travelToBA() throws InterruptedException
    {
        Thread.sleep(ThreadLocalRandom.current().nextInt(18, 25) * MILLISECONDS);
    }

    public static void travelToSE() throws InterruptedException
    {
        Thread.sleep(ThreadLocalRandom.current().nextInt(18, 25) * MILLISECONDS);
    }

    public static void acquireTruck() throws InterruptedException
    {
        truckAvailable.acquire();
    }

    public static void releaseTruck()
    {
        truckAvailable.release();
    }

    public static void truckTripFlour() throws InterruptedException
    {
        String threadName = Thread.currentThread().getName();
        String threadNumber = threadName.replaceAll("\\D+", "") + 1;
        acquireTruck();
        System.out.println("[Camión:" + threadNumber + "] tomado.");
        loadFlour();
        System.out.println("[Camión: "+ threadNumber + "] Harina cargada en BA.");
        travelToSE();
        System.out.println("[Camión: "+ threadNumber + "] Llegó a SE con harina.");
        unloadFlour();
        System.out.println("[Camión: "+ threadNumber + "] Harina descargada en SE.");
        travelToBA();
        System.out.println("[Camión: "+ threadNumber + "] Regresó a BA.");
        releaseTruck();
        System.out.println("[Camión: "+ threadNumber + "] liberado.");
    }

    public static void truckTripCoal() throws InterruptedException
    {
        String threadName = Thread.currentThread().getName();
        String threadNumber = threadName.replaceAll("\\D+", "");
        acquireTruck();
        System.out.println("[Camión:" + threadNumber + "] tomado.");
        travelToSE();
        System.out.println("[Camión: "+ threadNumber + "] Llegó a SE.");
        loadCoal();
        System.out.println("[Camión: "+ threadNumber + "] Carbon cargado en SE.");
        loadFuel();
        System.out.println("[Camión: "+ threadNumber + "] Combustible cargado.");
        travelToBA();
        System.out.println("[Camión: "+ threadNumber + "] Llegó a BA con carbon.");
        releaseTruck();
        System.out.println("[Camión: "+ threadNumber + "] liberado.");
    }

}
