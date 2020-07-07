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



    public IndividualParticleRawDataSet(String id, double[] I234U, double[] I235U, double[] I236U, double[] I238U, double[] I238U1H) throws WrongRawDataException {
        super(id);
        if (I234U.length != I235U.length || I235U.length != I236U.length || I236U.length != I238U.length || I238U.length != I238U1H.length) {
            throw new WrongRawDataException();
        }

        for (int i = 0; i < I234U.length; i++) {
            this.I234U[i] = new BigDecimal(I234U[i]);
            this.I235U[i] = new BigDecimal(I235U[i]);
            this.I236U[i] = new BigDecimal(I236U[i]);
            this.I238U[i] = new BigDecimal(I238U[i]);
            this.I238U1H[i] = new BigDecimal(I238U1H[i]);
        }


        R234Uto238U = new BigDecimal[I234U.length];
        R235Uto238U = new BigDecimal[I235U.length];
        R236Uto238U = new BigDecimal[I235U.length];
        R238U1Hto238U = new BigDecimal[I238U1H.length];

        for (int i = 0; i < I234U.length; i++) {
            R234Uto238U[i] = divide(this.I234U[i],this.I238U[i]);
            R235Uto238U[i] = divide(this.I235U[i], this.I238U[i]);
            R236Uto238U[i] = divide(this.I236U[i], this.I238U[i]);
            R238U1Hto238U[i] = divide(this.I238U1H[i], this.I238U[i]);

        }

        R_234Uto238U  = calculateAverageValue(R234Uto238U);
        R_235Uto238U  = calculateAverageValue(R235Uto238U);
        R_236Uto238U  = calculateAverageValue(R236Uto238U);
        R_238U1Hto238U  = calculateAverageValue(R238U1Hto238U);
        R_234Uto238UError = calculateSTDEValue(R234Uto238U);
        R_235Uto238UError = calculateSTDEValue(R235Uto238U);
        R_236Uto238UError = calculateSTDEValue(R236Uto238U);
        R_238U1Hto238UError = calculateSTDEValue(R238U1Hto238U);




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

    public void applyLinearDriftCorrection() {

    }

    public void applyMassBias() {

    }

    private void checkRawData() throws WrongRawDataException {
        if (I234U == null || I235U == null || I236U == null || I238U == null || I238U1H == null) {
            throw new WrongRawDataException();
        }
    }




}
