import doi.IndividualParticleDataSet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Controller {
    private FileHandler fileHandler;
    private final String MAIN_MARKER = "ISOTOPICS RATIO";
    ArrayList<IndividualParticleDataSet> results = new ArrayList<>();

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        Scanner sc = new Scanner(System.in);
        System.out.println("This small tool allow to make common table of SIMS U-results.");
        System.out.println("Please, enter full path to the folder with results:");
        String inputPath = sc.nextLine();
        controller.setFileHandler(new FileHandler(Paths.get(inputPath)));
        try {
            controller.getResultsFromFiles();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Results was successfully calculated");
        System.out.println("Please type full path to output file (with extension):");
        String outputPath = sc.nextLine();
        try {
            controller.putInTheTable(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        System.out.println("Sucess. Done.");


    }

    public void getResultsFromFiles() throws IOException {
        fileHandler.openFiles();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();

        for (int i = 0; i < files.size(); i++) {
            BufferedReader stringReader = new BufferedReader(new FileReader(files.get(i).toFile()));
            String currentString;
            String[] currentStringAsArr;
            currentString = stringReader.readLine();
            String ParticleId = currentString.substring(currentString.lastIndexOf("\\") + 1);
            while (!(currentString = stringReader.readLine()).startsWith("R0  \t")) {

            }
            currentString = stringReader.readLine();
            //R1 is here
            currentStringAsArr = currentString.split("\t\\s");
            double r_234Uto238U = Double.valueOf(currentStringAsArr[1]);
            double r_234Uto238UErr = Double.valueOf(currentStringAsArr[2]);
            currentString = stringReader.readLine();
            //R2 is here
            currentStringAsArr = currentString.split("\t\\s");
            double r_235Uto238U = Double.valueOf(currentStringAsArr[1]);
            double r_235Uto238UErr = Double.valueOf(currentStringAsArr[2]);
            currentString = stringReader.readLine();
            //R2 is here
            currentStringAsArr = currentString.split("\t\\s");
            double r_236Uto238U = Double.valueOf(currentStringAsArr[1]);
            double r_236Uto238UErr = Double.valueOf(currentStringAsArr[2]);
            currentString = stringReader.readLine();
            //R2 is here
            currentStringAsArr = currentString.split("\t\\s");
            double r_238U1Hto238U = Double.valueOf(currentStringAsArr[1]);
            double r_238U1Hto238UErr = Double.valueOf(currentStringAsArr[2]);
            IndividualParticleDataSet result = new IndividualParticleDataSet(ParticleId, r_234Uto238U, r_235Uto238U, r_236Uto238U, r_238U1Hto238U, r_234Uto238UErr, r_235Uto238UErr, r_236Uto238UErr, r_238U1Hto238UErr);
            results.add(result);
            System.out.println("Readed: " + result);
        }
    }

    public void putInTheTable(String filePath) throws IOException, InvalidFormatException {
        File outputFile = new File(filePath + ".xlsx");
        if (outputFile.createNewFile()) {
            XSSFWorkbook outputWorkbook = new XSSFWorkbook();
            XSSFSheet sheet = outputWorkbook.createSheet("Rep");
            createHeaderRow(sheet);
            String sequence;
            String partId;
            IndividualParticleDataSet currentResult;
            for (int i = 0; i < results.size(); i++) {
                currentResult = results.get(i);
                System.out.println("Read: " + currentResult);
                sequence = currentResult.getId();
                partId = "N/A";
                if (sequence.contains("@")) {
                    partId = sequence.substring(sequence.lastIndexOf("@")+1, sequence.lastIndexOf("."));
                    sequence = sequence.substring(0, sequence.lastIndexOf("@"));
                }
                Row row = sheet.createRow(sheet.getLastRowNum()+1);
                Cell cell = row.createCell(1);
                cell.setCellValue(sequence);
                cell = row.createCell(2);
                cell.setCellValue(partId);
                cell = row.createCell(3);
                cell.setCellValue(currentResult.getC234U().doubleValue());
                CellReference ref234U = new CellReference(cell);
                String thisRef234U = ref234U.getCellRefParts()[2]+ref234U.getCellRefParts()[1];
                cell = row.createCell(4);
                cell.setCellValue(currentResult.getC234UError().doubleValue());
                cell = row.createCell(5);
                cell.setCellValue(currentResult.getC235U().doubleValue());
                CellReference ref235U = new CellReference(cell);
                String thisRef235U = ref235U.getCellRefParts()[2]+ref235U.getCellRefParts()[1];
                cell = row.createCell(6);
                cell.setCellValue(currentResult.getC235UError().doubleValue());
                CellReference ref235UErr = new CellReference(cell);
                String thisRef235UErr = ref235UErr.getCellRefParts()[2]+ref235UErr.getCellRefParts()[1];
                cell = row.createCell(7);
                cell.setCellValue(currentResult.getC236U().doubleValue());
                CellReference ref236U = new CellReference(cell);
                String thisRef236U = ref236U.getCellRefParts()[2]+ref235UErr.getCellRefParts()[1];
                cell = row.createCell(8);
                cell.setCellValue(currentResult.getC236UError().doubleValue());
                cell = row.createCell(9);
                cell.setCellFormula("100-"+thisRef234U+"-"+thisRef235U+"-"+thisRef236U);
                CellReference ref238U = new CellReference(cell);
                String thisRef238U = ref238U.getCellRefParts()[2]+ref238U.getCellRefParts()[1];
                cell = row.createCell(10);
                cell.setCellFormula(thisRef238U+ "*" + thisRef235UErr + "/" + thisRef235U);
            }
            outputWorkbook.write(new FileOutputStream(outputFile));
            outputWorkbook.close();

        } else throw new FileNotFoundException();

    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("Date");
        cell = headerRow.createCell(1);
        cell.setCellValue("Sequence");
        cell = headerRow.createCell(2);
        cell.setCellValue("Part ID");
        cell = headerRow.createCell(3);
        cell.setCellValue("U234 At %");
        cell = headerRow.createCell(4);
        cell.setCellValue("U234 Err");
        cell = headerRow.createCell(5);
        cell.setCellValue("U235 At %");
        cell = headerRow.createCell(6);
        cell.setCellValue("U235 Err");
        cell = headerRow.createCell(7);
        cell.setCellValue("U236 At %");
        cell = headerRow.createCell(8);
        cell.setCellValue("U236 Err");
        cell = headerRow.createCell(9);
        cell.setCellValue("U238 At %");
        cell = headerRow.createCell(10);
        cell.setCellValue("U238 Err");

    }
}
