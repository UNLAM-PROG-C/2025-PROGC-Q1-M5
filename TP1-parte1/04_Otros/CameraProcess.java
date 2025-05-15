import java.io.File;
import java.io.IOException;

public class CameraProcess
{
    private static final int MILLISECONDS  = 1000;
    public static void main(String[] args) throws IOException, InterruptedException
    {

        int id = Integer.parseInt(args[0]);
        String zone = args[1];
        int duration = Integer.parseInt(args[2]);
        int frequency = Integer.parseInt(args[3]);

        File file = new File(zone,"camara" + id);
        file.createNewFile();

        cameraMonitoring(duration,frequency,zone, id);
    }

    private static void cameraMonitoring(Integer duration,Integer frequency, String zone, Integer id) throws InterruptedException
    {

        Integer monitoring = duration / frequency;
        Integer monitoringTimeLeft  = duration % frequency;
        int eventsCount = 0;
        for (int i = 0; i < monitoring; i++)
        {

            Thread.sleep(frequency);

            File folder = new File(zone);

            if (folder.isDirectory())
            {
                File[] files = folder.listFiles();
        
                if (files != null) 
                {
                    for (File file : files)
                    {
                        if (file.isFile())
                        {
                            if (!(file.getName().equals("camara"+id)))
                            {
                                System.out.println( "ID de Camara: " + id + " zone: " + file.getParentFile().getName() + " EVENTO: " + file.getName().replaceFirst("[.][^.]+$", "") );
                                
                                if (!(file.getName().equals("Sin actividad")))
                                {
                                    eventsCount++;
                                }
                                file.delete();
                            }
                        }
                    }
                } 
            }
            

        }
        Thread.sleep(monitoringTimeLeft  + MILLISECONDS);
        System.out.println("CAMARA: " + id + " OCURRENCIAS: " + eventsCount );

    }

}

    
