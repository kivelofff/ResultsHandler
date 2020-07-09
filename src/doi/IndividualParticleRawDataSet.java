package doi;

import java.math.BigDecimal;
import java.util.Arrays;

public class IndividualParticleRawDataSet extends IndividualParticleDataSet {
    protected final int BLOCK_SIZE = 4;

    protected BigDecimal[] R234Uto238U;
    protected BigDecimal[] R235Uto238U;
    protected BigDecimal[] R236Uto238U;
    protected BigDecimal[] R238U1Hto238U;

    private BigDecimal[] I234U;
    private BigDecimal[] I235U;
    private BigDecimal[] I236U;
    private BigDecimal[] I238U;
    private BigDecimal[] I238U1H;

    private double waiting234U = 0.56;
    private double counting234U = 5.52;
    private double waiting235U = 0.56;
    private double counting235U = 2.48;
    private double waiting236U = 0.56;
    private double counting236U = 4.0;
    private double waiting238U = 0.56;
    private double counting238U = 2.0;
    private double waiting238U1H = 0.56;
    private double counting238U1H = 4.0;

    public IndividualParticleRawDataSet(String id, double[] I234U, double[] I235U, double[] I236U, double[] I238U, double[] I238U1H, BigDecimal mbCoeff234U, BigDecimal mbCoeff235U, BigDecimal mbCoeff236U) throws WrongRawDataException {
        super(id);
        if (I234U.length != I235U.length || I235U.length != I236U.length || I236U.length != I238U.length || I238U.length != I238U1H.length) {
            throw new WrongRawDataException();
        }
        int numberOfCycles = I234U.length;
        this.I234U = new BigDecimal[numberOfCycles];
        this.I235U = new BigDecimal[numberOfCycles];
        this.I236U = new BigDecimal[numberOfCycles];
        this.I238U = new BigDecimal[numberOfCycles];
        this.I238U1H = new BigDecimal[numberOfCycles];


        for (int i = 0; i < numberOfCycles; i++) {
            this.I234U[i] = new BigDecimal(I234U[i]);
            this.I235U[i] = new BigDecimal(I235U[i]);
            this.I236U[i] = new BigDecimal(I236U[i]);
            this.I238U[i] = new BigDecimal(I238U[i]);
            this.I238U1H[i] = new BigDecimal(I238U1H[i]);
        }

       // applyYieldCorrection(1, 0.0);
       // applyDeadTimeCorrection(27e-9);
       // applyLinearDriftCorrection();

        R234Uto238U = new BigDecimal[numberOfCycles];
        R235Uto238U = new BigDecimal[numberOfCycles];
        R236Uto238U = new BigDecimal[numberOfCycles];
        R238U1Hto238U = new BigDecimal[numberOfCycles];

        for (int i = 0; i < numberOfCycles; i++) {
            R234Uto238U[i] = divide(this.I234U[i],this.I238U[i]);
            R235Uto238U[i] = divide(this.I235U[i], this.I238U[i]);
            R236Uto238U[i] = divide(this.I236U[i], this.I238U[i]);
            R238U1Hto238U[i] = divide(this.I238U1H[i], this.I238U[i]);

        }

        applyMassBias(mbCoeff234U, mbCoeff235U, mbCoeff236U);

        R_234Uto238U  = calculateAverageValue(R234Uto238U);
        R_235Uto238U  = calculateAverageValue(R235Uto238U);
        R_236Uto238U  = calculateAverageValue(R236Uto238U);
        R_238U1Hto238U  = calculateAverageValue(R238U1Hto238U);
        R_234Uto238UError = calculateSTDEValue(R234Uto238U);
        R_235Uto238UError = calculateSTDEValue(R235Uto238U);
        R_236Uto238UError = calculateSTDEValue(R236Uto238U);
        R_238U1Hto238UError = calculateSTDEValue(R238U1Hto238U);
        correctU236();
        calculateFinalValues();



    }

    protected BigDecimal calculateAverageValue (BigDecimal[] data) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < data.length; i++) {
            sum = sum.add(data[i]);
        }
        return divide(sum, new BigDecimal(data.length));
    }

    protected BigDecimal calculateSTDEValue (BigDecimal[] data) {
        if (data == null) return BigDecimal.ZERO;
        int numberOfCycles = data.length;
        BigDecimal[] dataOverBlocks;
        if (numberOfCycles % BLOCK_SIZE >0) {
            dataOverBlocks = new BigDecimal[numberOfCycles/BLOCK_SIZE+1];
        } else {
            dataOverBlocks = new BigDecimal[numberOfCycles/BLOCK_SIZE];
        }

        int blockNumber = 0;
        for (int i = 0; i < numberOfCycles; i+=BLOCK_SIZE) {
            BigDecimal[] currentBlock = Arrays.copyOfRange(data, i,i+BLOCK_SIZE);
            if (i+BLOCK_SIZE > numberOfCycles) { currentBlock = Arrays.copyOfRange(data, i,data.length);}
            dataOverBlocks[blockNumber] = calculateAverageValue(currentBlock);
            blockNumber++;
        }

        BigDecimal averageOverAnalysis = calculateAverageValue(dataOverBlocks);
        BigDecimal powSum = BigDecimal.ZERO;
        for (int i = 0; i < dataOverBlocks.length; i++) {
            powSum = powSum.add((dataOverBlocks[i].subtract(averageOverAnalysis)).pow(2));
        }
        BigDecimal stdeSquared = divide(powSum, new BigDecimal(blockNumber-1));
        return new BigDecimal(Math.sqrt(stdeSquared.doubleValue()));

    }

    public void applyYieldCorrection(int efficiency, double background) throws WrongRawDataException {
        checkRawData();
        BigDecimal bckg = new BigDecimal(background);
        BigDecimal eff = new BigDecimal(efficiency);
        for (int i = 0; i < I234U.length; i++) {
            I234U[i] = divide(I234U[i].subtract(bckg), eff);
            I235U[i] = divide(I235U[i].subtract(bckg), eff);
            I236U[i] = divide(I236U[i].subtract(bckg), eff);
            I238U[i] = divide(I238U[i].subtract(bckg), eff);
            I238U1H[i] = divide(I238U1H[i].subtract(bckg), eff);
        }
    }

    public void applyDeadTimeCorrection(double tau) throws WrongRawDataException {
        checkRawData();
        BigDecimal t = new BigDecimal(tau);
        for (int i = 0; i < I234U.length; i++) {

            I234U[i] = divide(I234U[i], (BigDecimal.ONE.subtract(I234U[i].multiply(t))));
            I235U[i] = divide(I235U[i], (BigDecimal.ONE.subtract(I235U[i].multiply(t))));
            I236U[i] = divide(I236U[i], (BigDecimal.ONE.subtract(I236U[i].multiply(t))));
            I238U[i] = divide(I238U[i], (BigDecimal.ONE.subtract(I238U[i].multiply(t))));
            I238U1H[i] = divide(I238U1H[i], (BigDecimal.ONE.subtract(I238U1H[i].multiply(t))));
        }
    }

    public void applyLinearDriftCorrection() throws WrongRawDataException {
        checkRawData();
        int numberOfcycles = I234U.length;
        double cycleDuration = waiting234U + counting234U + waiting235U + counting235U + waiting236U + counting236U + waiting238U + counting238U + waiting238U1H + counting238U1H;
        BigDecimal[] I234Ucorr = new BigDecimal[numberOfcycles];
        BigDecimal[] I235Ucorr = new BigDecimal[numberOfcycles];
        BigDecimal[] I236Ucorr = new BigDecimal[numberOfcycles];
        BigDecimal[] I238Ucorr = new BigDecimal[numberOfcycles];
        BigDecimal[] I238U1Hcorr = new BigDecimal[numberOfcycles];

        I234Ucorr[0] = I234U[0].add(divide(I234U[1].subtract(I234U[0]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U)));
        I235Ucorr[0] = I235U[0].add(divide(I235U[1].subtract(I235U[0]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U)));
        I236Ucorr[0] = I236U[0].add(divide(I236U[1].subtract(I236U[0]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U)));
        I238Ucorr[0] = I238U[0].add(divide(I238U[1].subtract(I238U[0]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U-waiting238U - counting238U)));
        I238U1Hcorr[0] = I238U1H[0].add(divide(I238U1H[1].subtract(I238U1H[0]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-cycleDuration)));

        for (int i = 1; i < numberOfcycles-1; i++) {
            I234Ucorr[i] = I234U[i].add(divide(I234U[i+1].subtract(I234U[i-1]), new BigDecimal(2*cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U)));
            I235Ucorr[i] = I235U[i].add(divide(I235U[i+1].subtract(I235U[i-1]), new BigDecimal(2*cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U)));
            I236Ucorr[i] = I236U[i].add(divide(I236U[i+1].subtract(I236U[i-1]), new BigDecimal(2*cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U)));
            I238Ucorr[i] = I238U[i].add(divide(I238U[i+1].subtract(I238U[i-1]), new BigDecimal(2*cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U-waiting238U - counting238U)));
            I238U1Hcorr[i] = I238U1H[i].add(divide(I238U1H[i+1].subtract(I238U1H[i-1]), new BigDecimal(2*cycleDuration)).multiply(new BigDecimal(cycleDuration/2-cycleDuration)));
        }

        I234Ucorr[numberOfcycles-1] = I234U[numberOfcycles-1].add(divide(I234U[numberOfcycles-1].subtract(I234U[numberOfcycles-2]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U)));
        I235Ucorr[numberOfcycles-1] = I235U[numberOfcycles-1].add(divide(I235U[numberOfcycles-1].subtract(I235U[numberOfcycles-2]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U)));
        I236Ucorr[numberOfcycles-1] = I236U[numberOfcycles-1].add(divide(I236U[numberOfcycles-1].subtract(I236U[numberOfcycles-2]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U)));
        I238Ucorr[numberOfcycles-1] = I238U[numberOfcycles-1].add(divide(I238U[numberOfcycles-1].subtract(I238U[numberOfcycles-2]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-waiting234U-counting234U-waiting235U-counting235U-waiting236U-counting236U-waiting238U - counting238U)));
        I238U1Hcorr[numberOfcycles-1] = I238U1H[numberOfcycles-1].add(divide(I238U1H[numberOfcycles-1].subtract(I238U1H[numberOfcycles-2]), new BigDecimal(cycleDuration)).multiply(new BigDecimal(cycleDuration/2-cycleDuration)));

        I234U = I234Ucorr;
        I235U = I235Ucorr;
        I236U = I236Ucorr;
        I238U = I238Ucorr;
        I238U1H = I238U1Hcorr;
    }

    @Override
    public void applyMassBias(BigDecimal mbCoeff234U, BigDecimal mbCoeff235U, BigDecimal mbCoeff236U) throws WrongRawDataException {
        checkRawData();
        int numberOfcycles = I234U.length;
        for (int i = 0; i < numberOfcycles; i++) {
            R234Uto238U[i] = divide(R234Uto238U[i],mbCoeff234U);
            R235Uto238U[i] = divide(R235Uto238U[i],mbCoeff235U);
            R236Uto238U[i] = divide(R236Uto238U[i],mbCoeff236U);
        }
    }

    private void checkRawData() throws WrongRawDataException {
        if (I234U == null || I235U == null || I236U == null || I238U == null || I238U1H == null) {
            throw new WrongRawDataException();
        }
    }




}
