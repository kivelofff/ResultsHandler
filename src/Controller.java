import doi.IndividualParticleDataSet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.file.FileSystemException;
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
        }
        System.out.println("Sucess. Done.");


    }

    public void getResultsFromFiles() throws IOException {
        fileHandler.openFiles();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();

        for (int i = 0; i < files.size(); i++) {
            String name = files.get(i).getFileName().toString();
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(files.get(i).toFile()));
            HSSFSheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            HSSFRow row;
            HSSFCell cell;
            String cellData;
            for (int j = 0; j < rowCount; j++) {
                row = sheet.getRow(j);
                cell = row.getCell(0);
                cellData = cell.getStringCellValue();
                if (cellData.equals(Ratios.R0)) {
                    row = sheet.getRow(j+1);
                    double r1 = row.getCell(1).getNumericCellValue();
                    double r1Stde = row.getCell(2).getNumericCellValue();
                    row = sheet.getRow(j+2);
                    double r2 = row.getCell(1).getNumericCellValue();
                    double r2Stde = row.getCell(2).getNumericCellValue();
                    row = sheet.getRow(j+3);
                    double r3 = row.getCell(1).getNumericCellValue();
                    double r3Stde = row.getCell(2).getNumericCellValue();
                    row = sheet.getRow(j+4);
                    double r4 = row.getCell(1).getNumericCellValue();
                    double r4Stde = row.getCell(2).getNumericCellValue();

                    IndividualParticleDataSet particleResult = new IndividualParticleDataSet(name, r1, r2, r3, r4, r1Stde, r2Stde, r3Stde, r4Stde);
                    results.add(particleResult);
                    break;

                }
            }
            workbook.close();
        }
    }

    public void putInTheTable(String filePath) throws IOException {
        File outputFile = new File(filePath);
        if (outputFile.createNewFile()) {
            Workbook outputWorkbook = WorkbookFactory.create(outputFile);
            Sheet sheet = outputWorkbook.createSheet("Rep");
            createHeaderRow(sheet);
            String sequence;
            String partId;
            IndividualParticleDataSet currentResult;
            for (int i = 0; i < results.size(); i++) {
                currentResult = results.get(i);
                sequence = currentResult.getId();
                partId = "N/A";
                if (sequence.contains("@")) {
                    partId = sequence.substring(sequence.lastIndexOf("@")-1, sequence.lastIndexOf("."));
                    sequence = sequence.substring(0, sequence.lastIndexOf("@"));
                }
                Row row = sheet.createRow(sheet.getLastRowNum());
                Cell cell = row.createCell(1);
                cell.setCellValue(sequence);
                cell = row.createCell(2);
                cell.setCellValue(partId);
                cell = row.createCell(3);
                cell.setCellValue(currentResult.getC234U().doubleValue());
                cell = row.createCell(4);
                cell.setCellValue(currentResult.getC234UError().doubleValue());
                cell = row.createCell(5);
                cell.setCellValue(currentResult.getC235U().doubleValue());
                cell = row.createCell(6);
                cell.setCellValue(currentResult.getC235UError().doubleValue());
                cell = row.createCell(7);
                cell.setCellValue(currentResult.getC236U().doubleValue());
                cell = row.createCell(8);
                cell.setCellValue(currentResult.getC236UError().doubleValue());
                cell = row.createCell(9);
                cell.setCellFormula("=100-RC[-2]-RC[-4]-RC[-6]");
                cell = row.createCell(10);
                cell.setCellFormula("=RC[-1]*RC[-3]/RC[-4]");
            }
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
