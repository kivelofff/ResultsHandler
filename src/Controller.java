import doi.IndividualParticleDataSet;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Controller {
    private FileHandler fileHandler;
    private final String MAIN_MARKER = "ISOTOPICS RATIO";



    public void getResultsFromFiles() throws IOException {
        ArrayList<IndividualParticleDataSet> results = new ArrayList<>();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();

        for (int i = 0; i < files.size(); i++) {
            Workbook workbook = WorkbookFactory.create(files.get(i).toFile());
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            Row row;
            Cell cell;
            String cellData;
            for (int j = 0; j < rowCount; j++) {
                row = sheet.getRow(j);
                cell = row.getCell(0);
                cellData = cell.getStringCellValue();
                if (cellData.equals(Ratios.R0)) {

                }
            }
        }
    }

    public void putInTheTable() {

    }
}
