package doi;

import java.math.BigDecimal;

public class IndividualParticleDataSet extends DataSet {
    private BigDecimal c234U;
    private BigDecimal c234UError;
    private BigDecimal c235U;
    private BigDecimal c235UError;
    private BigDecimal c236U;
    private BigDecimal c236UError;

    public IndividualParticleDataSet(double R_234Uto238U, double R_235Uto238U, double R_236Uto238U, double R_238U1Hto238U, double R_234Uto238UError, double R_235Uto238UError, double R_236Uto238UError, double R_238U1Hto238UError) {
        super(R_234Uto238U, R_235Uto238U, R_236Uto238U, R_238U1Hto238U, R_234Uto238UError, R_235Uto238UError, R_236Uto238UError, R_238U1Hto238UError);
        correctU236();
        calculateFinalValues();
    }


    @Override
    public void calculateFinalValues() {
        BigDecimal denominator = R_234Uto238U.add(R_235Uto238U.add(R_236Uto238Ucorr));
        c234U = divide(R_234Uto238U, denominator);
        c235U = divide(R_235Uto238U, denominator);
        c236U = divide(R_236Uto238Ucorr, denominator);
        calculateConcentrationError();
    }

    private void calculateConcentrationError() {
        BigDecimal otherFor234U = R_235Uto238U.add(R_236Uto238Ucorr);
        BigDecimal otherFor235U = R_234Uto238U.add(R_236Uto238Ucorr);
        BigDecimal otherFor236U = R_234Uto238U.add(R_235Uto238U);

        BigDecimal otherFor234UError = R_235Uto238UError.add(R_236Uto238UcorrError);
        BigDecimal otherFor235UError = R_234Uto238UError.add(R_236Uto238UcorrError);
        BigDecimal otherFor236UError = R_234Uto238UError.add(R_235Uto238UError);

        BigDecimal c234URelError = divide(otherFor234UError, otherFor234U).add(divide(R_234Uto238UError, R_234Uto238U));
        c234UError = c234U.multiply(c234URelError);
        BigDecimal c235URelError = divide(otherFor235UError, otherFor235U).add(divide(R_235Uto238UError, R_235Uto238U));
        c235UError = c235U.multiply(c235URelError);
        BigDecimal c236URelError = divide(otherFor236UError, otherFor236U).add(divide(R_236Uto238UcorrError, R_236Uto238Ucorr));
        c236UError = c236U.multiply(c236URelError);
    }

    public void exportData() {

    }
}