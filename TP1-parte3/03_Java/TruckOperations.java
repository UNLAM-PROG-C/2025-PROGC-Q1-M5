
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TruckOperations {

    private static final Semaphore loadFlourSemaphore = new Semaphore(1);
    private static final Semaphore unloadFlourSemaphore = new Semaphore(1);
    private static final Semaphore loadCoalSemaphore = new Semaphore(1);
    private static final Semaphore unloadCoalSemaphore = new Semaphore(1);
    private static final Semaphore loadFuelSemaphore = new Semaphore(2);

    private static Semaphore truckAvailable;
    private static BlockingQueue<Integer> truckIds;

    private static final int FLOURTRUCK = 0;
    private static final int COALTRUCK = 1;
    private static final int MILLISECONDS = 1000;

    private static int[] travels;

    public static void main(String[] args) throws InterruptedException {

        if (args.length == 0 || args.length > 2) {
            System.out.println("Uso: java TruckOperations <camiones> [viajes]");
            return;
        }

        int trucks;
        try {
            trucks = Integer.parseInt(args[0]);
            if (trucks <= 0) {
                System.out.println("La cantidad de camiones debe ser un entero positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("El argumento <camiones> debe ser un número entero.");
            return;
        }

        Integer tripsRequested = null;
        if (args.length == 2) {
            try {
                tripsRequested = Integer.parseInt(args[1]);
                if (tripsRequested <= 0) {
                    System.out.println("La cantidad de viajes debe ser un entero positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("El argumento [viajes] debe ser un número entero.");
                return;
            }
        }

        long startTime = System.currentTimeMillis();
        truckAvailable = new Semaphore(trucks);
        initTruckIds(trucks);
        travels = simulateTravels(tripsRequested);

        System.out.println("========= Inicio de operaciones =========");
        System.out.println("Cantidad de viajes a procesar: " + travels.length);

        Thread[] threads = processTravels(travels);
        for (Thread t : threads) t.join();

        System.out.println("========= Fin de operaciones =========");
        printElapsedTime(startTime);
    }

    private static void initTruckIds(int count) {
        truckIds = new ArrayBlockingQueue<>(count);
        for (int i = 1; i <= count; i++) {
            truckIds.add(i);
        }
    }

    private static void printElapsedTime(long startTime) {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        double elapsedSeconds = elapsedMillis / 1000.0;
        System.out.printf("Tiempo real transcurrido: %.2f s%n", elapsedSeconds);
        System.out.printf("Tiempo simulado transcurrido: %.2f h%n", elapsedSeconds);
        System.out.printf("Equivalente simulado: %.4f días%n", elapsedSeconds / 24.0);
    }

    private static Thread[] processTravels(int[] travels) {
        Thread[] threads = new Thread[travels.length];
        for (int i = 0; i < travels.length; i++) {
            final int type = travels[i];
            threads[i] = new Thread(() -> {
                try {
                    if (type == FLOURTRUCK) truckTripFlour();
                    else truckTripCoal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Trip-" + (i + 1));
            threads[i].start();
        }
        return threads;
    }

    private static int[] simulateTravels(Integer total) {
        Random r = new Random();
        int size = (total != null) ? total : r.nextInt(20) + 1;
        int[] v = new int[size];
        for (int i = 0; i < size; i++)
            v[i] = r.nextBoolean() ? FLOURTRUCK : COALTRUCK;
        return v;
    }

    private static void loadFlour() throws InterruptedException {
        op(loadFlourSemaphore, 2);
    }

    private static void unloadFlour() throws InterruptedException {
        op(unloadFlourSemaphore, 2);
    }

    private static void loadCoal() throws InterruptedException {
        op(loadCoalSemaphore, 2);
    }

    private static void unloadCoal() throws InterruptedException {
        op(unloadCoalSemaphore, 2);
    }

    private static void loadFuel() throws InterruptedException {
        op(loadFuelSemaphore, 1);
    }

    private static void op(Semaphore s, int seconds) throws InterruptedException {
        s.acquire();
        Thread.sleep(seconds * MILLISECONDS);
        s.release();
    }

    private static void travelToBA() throws InterruptedException {
        travelRandom();
    }

    private static void travelToSE() throws InterruptedException {
        travelRandom();
    }

    private static void travelRandom() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(18, 25) * MILLISECONDS);
    }

    private static int acquireTruck() throws InterruptedException {
        truckAvailable.acquire();
        return truckIds.take(); // obtiene un camión real (1, 2, 3, ...)
    }

    private static void releaseTruck(int id) throws InterruptedException {
        truckIds.put(id); // vuelve a ponerlo disponible
        truckAvailable.release();
    }

    private static void truckTripFlour() throws InterruptedException {
        int id = acquireTruck();
        System.out.println("[Camión " + id + "] tomado.");
        loadFlour();
        System.out.println("[Camión " + id + "] Harina cargada en BA.");
        travelToSE();
        System.out.println("[Camión " + id + "] Llegó a SE con harina.");
        unloadFlour();
        System.out.println("[Camión " + id + "] Harina descargada en SE.");
        travelToBA();
        System.out.println("[Camión " + id + "] Regresó a BA.");
        releaseTruck(id);
        System.out.println("[Camión " + id + "] liberado.");
    }

    private static void truckTripCoal() throws InterruptedException {
        int id = acquireTruck();
        System.out.println("[Camión " + id + "] tomado.");
        travelToSE();
        System.out.println("[Camión " + id + "] Llegó a SE.");
        loadCoal();
        System.out.println("[Camión " + id + "] Carbón cargado en SE.");
        loadFuel();
        System.out.println("[Camión " + id + "] Combustible cargado.");
        travelToBA();
        System.out.println("[Camión " + id + "] Llegó a BA con carbón.");
        releaseTruck(id);
        System.out.println("[Camión " + id + "] liberado.");
    }

}
