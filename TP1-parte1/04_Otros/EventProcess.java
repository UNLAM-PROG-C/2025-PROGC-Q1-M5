import java.io.File;
import java.io.IOException;

public class EventProcess 
{
    public static final String[] EVENTS = 
    {
        "Sin actividad",
        "Movimiento detectado",
        "Anomalía térmica",
        "Sombra extraña",
        "Ruido detectado"
    };
    public static void main(String[] args) throws InterruptedException, IOException
    {
        int duration = Integer.parseInt(args[0]);
        int frecuencia = Integer.parseInt(args[1]);
        String zonesPath = args[2];

        String[] zones = zonesPath.split(",");

        Integer monitoring = duration / frecuencia;
        Integer monitoringTimeLeft = duration % frecuencia;

        

        for (int i = 0; i < monitoring; i++)
        {
            Thread.sleep(frecuencia);
            
            for (String zona : zones) 
            {

                int randomIndex = (int) (Math.random() * EVENTS.length);
                String selectedEvent = EVENTS[randomIndex];
                File file = new File(zona, selectedEvent);
                file.createNewFile();
            }
        }
        Thread.sleep(monitoringTimeLeft + 1000);

    }

}


