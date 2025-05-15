
import java.io.File;
import java.util.ArrayList;


public class CameraSetup
{
    public ArrayList<String> SetupCameras(String[] zones, Integer quantity)
    {

        ArrayList<String> subFoldersPaths = new ArrayList<>();

        String rootFolderName = zones[0];
        File rootFolder = new File(rootFolderName);
        rootFolder.mkdir();
    
        for (int i = 1; i <= quantity; i++)
        {
            String subFolderName = zones[i];
            File subFolder = new File(rootFolder, subFolderName);
            subFolder.mkdir();
            subFoldersPaths.add(subFolder.getAbsolutePath());
        }
        return subFoldersPaths;
    }
    

}
