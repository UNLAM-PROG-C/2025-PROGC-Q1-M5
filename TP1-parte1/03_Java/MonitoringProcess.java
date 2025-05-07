import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonitoringProcess
{
    private static final int CAMERAS = 6;
    private static final int DEFAULT_DURATION = 10;
    private static final int DEFAULT_FREQUENCY = 5;
    private static final String[] ZONES = {
            "Sótano", "Ático", "Cocina", "Dormitorio", "Jardín", "Mausoleo"
    };

    public static void main(String[] args) throws InterruptedException, IOException
    {
        int duration = DEFAULT_DURATION;
        int frequency = DEFAULT_FREQUENCY;
        argumentsProcessing(args, duration, frequency);
        List<Process> childProcesses = new ArrayList<>();
        System.out.println("\n########### Monitoreo iniciado #############");
        System.out.println("Duracion: " + duration + " segundos\nFrecuencia: " + frequency + " segundos");
        System.out.println("############################################");
        for (int i = 0; i < CAMERAS; i++)
        {
            childProcesses.add(
                            startProcess("CameraProcess",
                            String.valueOf(i),
                            ZONES[i],
                            String.valueOf(duration * 1000),
                            String.valueOf(frequency * 1000))
                            );
        }
        for (Process child : childProcesses)
        {
            child.waitFor();
        }
        System.out.println("######### Monitoreo finalizado #############");
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

    public static void argumentsProcessing(String[] args, Integer duration, Integer frequency)
    {
        if (args.length < 2)
        {
            System.out.println("No se proporcionaron suficientes argumentos. Se usarán los valores por defecto.");
            System.out.println("Sintaxis de ejecución: java MonitoringProcess.java  <duration> <frequency>");
            return;
        }
        try
        {
            int dur = Integer.parseInt(args[0]);
            int freq = Integer.parseInt(args[1]);
    
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
            System.out.println("Sintaxis de ejecución: java MonitoringProcess.java  <duration> <frequency>");
        }
    }
    
}
