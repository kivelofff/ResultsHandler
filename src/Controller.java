import doi.DataSet;
import doi.IndividualParticleDataSet;
import doi.IndividualParticleRawDataSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Controller {
    private FileHandler fileHandler;
    private final String MAIN_MARKER = "ISOTOPICS RATIO";
    ArrayList<IndividualParticleDataSet> results = new ArrayList<>();
    private BigDecimal mbCoeff234U = BigDecimal.ONE;
    private BigDecimal mbCoeff235U = BigDecimal.ONE;
    private BigDecimal mbCoeff236U = BigDecimal.ONE;

    private BigDecimal CertR234U = new BigDecimal(5.46552978e-5);
    private BigDecimal CertR235U = new BigDecimal(0.0101400226);
    private BigDecimal CertR236U = new BigDecimal(6.8798998e-5);

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        Scanner sc = new Scanner(System.in);
        System.out.println("This small tool allow to make common table of SIMS U-results.");
        System.out.println("Please, enter full path to the folder with mass bias measurement results or type 'skip' to skip:");
        String inputPath = sc.nextLine();
        if (!inputPath.equals("skip")) {
            controller.setFileHandler(new FileHandler(Paths.get(inputPath)));
            try {
                controller.calculateMassBiasCoefficientsFromDP();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("mass bias coefficients was calculated successfully");
        }
        System.out.println("Please, enter full path to the folder with results:");
        inputPath = sc.nextLine();
        controller.setFileHandler(new FileHandler(Paths.get(inputPath)));
        try {
            controller.getResultsFromDPFiles();

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

    public void getResultsFromDPFiles() throws IOException {
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
            IndividualParticleDataSet result = new IndividualParticleDataSet(ParticleId, r_234Uto238U, r_235Uto238U, r_236Uto238U, r_238U1Hto238U, r_234Uto238UErr, r_235Uto238UErr, r_236Uto238UErr, r_238U1Hto238UErr, mbCoeff234U, mbCoeff235U, mbCoeff236U);
            results.add(result);
            System.out.println("Readed: " + result);
        }
    }

    public void getResultsFromCKBFiles() throws IOException {
        results.clear();
        fileHandler.openFiles();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();
        for (int i = 0; i < files.size(); i++) {
            BufferedReader stringReader = new BufferedReader(new FileReader(files.get(i).toFile()));
            String currentString;
            String[] currentStringAsArr;
            currentString = stringReader.readLine();
            while (!currentString.startsWith("Raw data file name")) {
                currentString = stringReader.readLine();
            }
            currentStringAsArr = currentString.split(";");
            String particleId =currentStringAsArr[1];
            while (!currentString.startsWith("Time[s]")) {
                currentString = stringReader.readLine();
            }
            ArrayList<Double> I234U = new ArrayList<>(20);
            ArrayList<Double> I235U = new ArrayList<>(20);
            ArrayList<Double> I236U = new ArrayList<>(20);
            ArrayList<Double> I238U = new ArrayList<>(20);
            ArrayList<Double> I238U1H = new ArrayList<>(20);
            currentString = stringReader.readLine();
            while (!currentString.equals("")) {

                currentStringAsArr = currentString.split(";");
                I234U.add(Double.valueOf(currentStringAsArr[2]));
                I235U.add(Double.valueOf(currentStringAsArr[5]));
                I236U.add(Double.valueOf(currentStringAsArr[8]));
                I238U.add(Double.valueOf(currentStringAsArr[11]));
                I238U1H.add(Double.valueOf(currentStringAsArr[14]));
                currentString = stringReader.readLine();
            }
            int nuberofCycles = I234U.size();

            double[] i234U = new double[nuberofCycles];
            double[] i235U = new double[nuberofCycles];
            double[] i236U = new double[nuberofCycles];
            double[] i238U = new double[nuberofCycles];
            double[] i238U1H = new double[nuberofCycles];

            for (int j = 0; j < nuberofCycles; j++) {
                i234U[j] = I234U.get(j);
                i235U[j] = I235U.get(j);
                i236U[j] = I236U.get(j);
                i238U[j] = I238U.get(j);
                i238U1H[j] = I238U1H.get(j);
            }

            IndividualParticleRawDataSet individualParticleRawDataSet;
            individualParticleRawDataSet = new IndividualParticleRawDataSet(particleId, i234U, i235U, i236U, i238U, i238U1H, mbCoeff234U, mbCoeff235U, mbCoeff236U);
            results.add(individualParticleRawDataSet);
        }

    }

    private void calculateMassBiasCoefficientsFromDP() throws IOException {
        fileHandler.openFiles();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();
        mbCoeff234U = BigDecimal.ONE;
        mbCoeff235U = BigDecimal.ONE;
        mbCoeff236U = BigDecimal.ONE;
        results.clear();
        getResultsFromDPFiles();
        BigDecimal sumR234U = BigDecimal.ZERO;
        BigDecimal sumR235U = BigDecimal.ZERO;
        BigDecimal sumR236U = BigDecimal.ZERO;
        int numberofMBMeasurements = results.size();
        for (int i = 0; i < numberofMBMeasurements; i++) {
            sumR234U = sumR234U.add(results.get(i).getR_234Uto238U());
            sumR235U = sumR235U.add(results.get(i).getR_235Uto238U());
            sumR236U = sumR236U.add(results.get(i).getR_236Uto238U());
        }

        mbCoeff234U = DataSet.divide(DataSet.divide(sumR234U, new BigDecimal(numberofMBMeasurements)), CertR234U);
        mbCoeff235U = DataSet.divide(DataSet.divide(sumR235U, new BigDecimal(numberofMBMeasurements)), CertR235U);
        mbCoeff236U = DataSet.divide(DataSet.divide(sumR236U, new BigDecimal(numberofMBMeasurements)), CertR236U);
    }

    private void calculateMassBiasCoefficientsFromCKB() throws IOException {
        fileHandler.openFiles();
        ArrayList<Path> files = fileHandler.getMeasurementFiles();
        mbCoeff234U = BigDecimal.ONE;
        mbCoeff235U = BigDecimal.ONE;
        mbCoeff236U = BigDecimal.ONE;
        results.clear();
        getResultsFromCKBFiles();
        BigDecimal sumR234U = BigDecimal.ZERO;
        BigDecimal sumR235U = BigDecimal.ZERO;
        BigDecimal sumR236U = BigDecimal.ZERO;
        int numberofMBMeasurements = results.size();
        for (int i = 0; i < numberofMBMeasurements; i++) {
            sumR234U = sumR234U.add(results.get(i).getR_234Uto238U());
            sumR235U = sumR235U.add(results.get(i).getR_235Uto238U());
            sumR236U = sumR236U.add(results.get(i).getR_236Uto238U());
        }

        mbCoeff234U = DataSet.divide(DataSet.divide(sumR234U, new BigDecimal(numberofMBMeasurements)), CertR234U);
        mbCoeff235U = DataSet.divide(DataSet.divide(sumR235U, new BigDecimal(numberofMBMeasurements)), CertR235U);
        mbCoeff236U = DataSet.divide(DataSet.divide(sumR236U, new BigDecimal(numberofMBMeasurements)), CertR236U);
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
