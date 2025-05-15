import java.io.File;
import java.io.IOException;

public class CameraProcess
{
    private static final String[] EVENTS =
    {
        "Sin actividad",
        "Movimiento detectado",
        "Anomalía térmica",
        "Sombra extraña",
        "Ruido detectado"
    };
    public static void main(String[] args) throws IOException, InterruptedException
    {

        int id = Integer.parseInt(args[0]);
        String zone = args[1];
        int duration = Integer.parseInt(args[2]);
        int frequency = Integer.parseInt(args[3]);

        cameraMonitoring(duration,frequency,zone, id);
    }

    private static void cameraMonitoring(Integer duration,Integer frequency, String zone, Integer id) throws InterruptedException
    {
        String CameraBuffer;
        int eventCount = 0;
        Integer monitoring = duration / frequency;
        Integer monitoringTimeLeft = duration % frequency;

        for (int i = 0; i < monitoring; i++)
        {

            Thread.sleep(frequency);

            int randomIndex = (int) (Math.random() * EVENTS.length);
            String selectedEvent = EVENTS[randomIndex];
            CameraBuffer = selectedEvent;
            if (!(CameraBuffer.equals("Sin actividad")))
            {
                eventCount++;
            }
            System.out.println(String.format("ID de Camara: %s | ZONA: %-13s | EVENTO: %s", id, zone, CameraBuffer));
        }
        Thread.sleep(monitoringTimeLeft + 1000);
        System.out.println("ID de Camara: " + id + " Ocurrencias: " + eventCount );
    }

}

    
