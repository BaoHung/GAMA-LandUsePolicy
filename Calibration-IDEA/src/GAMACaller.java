import java.io.File;
import java.io.IOException;

public class GAMACaller {

    String XMLFilePath;
    String resultsPath;

    public GAMACaller(String xml, String results) {
        XMLFilePath = xml;
        resultsPath = results;
    }

    public void runGAMA() {
        // For Windows,
        //     String[] command = {"CMD", "/C", "dir"};
        //     ProcessBuilder pb = new ProcessBuilder( command );

		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "gama-headless.sh", XMLFilePath, resultsPath);

        pb.directory(new File(Constants.headlessFolder));
        Process p;
        try {
            p = pb.start();

            // Make the program wait until the process is not finished
            while (p.isAlive()) {
            }

            System.out.println("FIN du process !!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
