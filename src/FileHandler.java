import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileHandler {

    private final String EXTENSION = ".is_xls";
    private ArrayList<Path> measurementFiles = new ArrayList<>();
    private Path path;

    public FileHandler(Path path) {
        this.path = path;
    }

    public void openFiles() throws IOException {
        if (!path.isAbsolute() || !Files.isDirectory(path)) {throw new FileNotFoundException();}
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            for (Path file : directoryStream) {
                if (Files.isRegularFile(file) && file.toString().toLowerCase().endsWith(EXTENSION)) {
                    measurementFiles.add(file);
                }
            }
        }
    }

    public ArrayList<Path> getMeasurementFiles() {
        return measurementFiles;
    }
}
