import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MonitoringProcess
{

    private static final int DEFAULT_cameras = 3;
    private static final int DEFAULT_DURATION = 10;
    private static final int DEFAULT_FREQUENCY = 5;
    private static final int REMOVE_FOLDERS_TIME = 10;
    private static final int MILLISECONDS  = 1000;
    private static final String[] ZONES =
    {
        "Mansión Derceto", "Sótano", "Ático", "Cocina", "Dormitorio", "Jardín", "Mausoleo",
        "Museo Privado", "Cuarto de Servicio", "Zaguán", "Vestíbulo Principal", "Sala de Reliquias",
        "Archivo Secreto", "Despensa Antigua", "Laboratorio Oculto", "Sala de Tortura", "Puente Elevadizo"
    };

    public static void main(String[] args) throws IOException, InterruptedException
    {
        
        int[] values = argumentsProcessing(args);
        int cameras = values[0];
        int duration = values[1];
        int frequency = values[2];
        
        CameraSetup camera_setup = new CameraSetup();
        ArrayList<String> zonesPaths = camera_setup.SetupCameras(ZONES, cameras);
        List<String> selectedZonesPaths = zonesPaths.subList(0, Math.min(cameras, zonesPaths.size()));
        String zonesPathString = String.join(",", selectedZonesPaths);
        List<Process> childProcess = new ArrayList<>();
        System.out.println("\n########### Monitoreo iniciado #############");
        System.out.println("Duracion: " + duration + " segundos\nFrecuencia: " + frequency + " segundos");
        System.out.println("############################################");

        childProcess.add(
            startProcess("EventProcess", String.valueOf(duration * MILLISECONDS), String.valueOf(frequency * MILLISECONDS), zonesPathString)
        );

        for (int i = 0; i < cameras; i++)
        {
            childProcess.add(
                startProcess("CameraProcess",
                              String.valueOf(i),
                              zonesPaths.get(i),
                              String.valueOf(duration * MILLISECONDS),
                              String.valueOf(frequency * MILLISECONDS))
            );
        }

        for (Process p : childProcess)
        {
            p.waitFor();
        }

        System.out.println("######### Monitoreo finalizado #############");
        System.out.println("La mansion se destruira en 10 segundos");
        Thread.sleep(REMOVE_FOLDERS_TIME * MILLISECONDS);
        File rootFolder = new File(ZONES[0]);

        ExplosionEffect explosion = new ExplosionEffect();
        ExplosionEffect.explosionEffect();
        deleteFolder(rootFolder);
    }

    public static Process startProcess(String... arguments) throws IOException
    {
        List<String> command = new ArrayList<>();
        command.add("java");
        for (String arg : arguments)
        {
            command.add(arg);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return processBuilder.inheritIO().start();
    }

    public static int[] argumentsProcessing(String[] args)
    {
        int duration = DEFAULT_DURATION;
        int frequency = DEFAULT_FREQUENCY;
        int cameras = DEFAULT_cameras;
        if (args.length < 3)
        {
            System.out.println("Se usarán los valores por defecto.");
            return new int[] 
            {
                cameras,duration, frequency 
            };
        }
        try
        {
            int cam =  Integer.parseInt(args[0]);
            int dur = Integer.parseInt(args[1]);
            int freq = Integer.parseInt(args[2]);

            if (cam <= 0)
            {
                System.out.println("Instala las camaras!. Se usarán los valores por defecto.");
            }
            else if (cam > 16)
            {
                System.out.println("Solo tenemos 16 camaras ): . Se usarán los valores por defecto.");
            }
            else
            {
                cameras = cam;
            }
            
            if (dur <= 0 || freq <= 0)
            {
                System.out.println("Frecuencia y duración deben ser números positivos. Se usarán los valores por defecto.");
            }
            else if (freq > dur)
            {
                System.out.println("La frecuencia no puede ser mayor que la duración. Se usarán los valores por defecto.");
            }
            else
            {
                duration = dur;
                frequency = freq;
            }
        }
        catch (NumberFormatException e)
        {
            System.out.println("Parámetros inválidos. Se usarán los valores por defecto.");
            System.out.println("Sintaxis de ejecución: java MonitoringProcess.java <cameras> <duration> <frequency>");
        }
        return new int[] 
        { 
            cameras, duration, frequency 
        };
    }

    public static boolean deleteFolder(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null) 
        {
            for (File file : files) 
            {
                if (file.isDirectory())
                {
                    deleteFolder(file);
                } 
                else 
                {
                    file.delete();
                }
            }
        }
        return folder.delete();
    }
}

