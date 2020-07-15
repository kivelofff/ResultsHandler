package doi;

import java.math.BigDecimal;

public abstract class DataSet {
    protected final BigDecimal ZERO = new BigDecimal(0);
    protected static final int SCALE_FOR_DIVIDING = 19;
    protected BigDecimal R_234Uto238U;
    protected BigDecimal R_235Uto238U;
    protected BigDecimal R_236Uto238U;
    protected BigDecimal R_236Uto238Ucorr;
    protected BigDecimal R_238U1Hto238U;
    protected BigDecimal R_234Uto238UError;
    protected BigDecimal R_235Uto238UError;
    protected BigDecimal R_236Uto238UError;
    protected BigDecimal R_236Uto238UcorrError;
    protected BigDecimal R_238U1Hto238UError;

    public DataSet(double R_234Uto238U, double R_235Uto238U, double R_236Uto238U, double R_238U1Hto238U, double R_234Uto238UError, double R_235Uto238UError, double R_236Uto238UError, double R_238U1Hto238UError) {
        this.R_234Uto238U = new BigDecimal(R_234Uto238U);
        this.R_235Uto238U = new BigDecimal(R_235Uto238U);
        this.R_236Uto238U = new BigDecimal(R_236Uto238U);
        this.R_238U1Hto238U = new BigDecimal(R_238U1Hto238U);
        this.R_234Uto238UError = new BigDecimal(R_234Uto238UError);
        this.R_235Uto238UError = new BigDecimal(R_235Uto238UError);
        this.R_236Uto238UError = new BigDecimal(R_236Uto238UError);
        this.R_238U1Hto238UError = new BigDecimal(R_238U1Hto238UError);

    }

    public DataSet(BigDecimal r_234Uto238U, BigDecimal r_235Uto238U, BigDecimal r_236Uto238U, BigDecimal r_238U1Hto238U, BigDecimal r_234Uto238UError, BigDecimal r_235Uto238UError, BigDecimal r_236Uto238UError, BigDecimal r_238U1Hto238UError) {
        R_234Uto238U = r_234Uto238U;
        R_235Uto238U = r_235Uto238U;
        R_236Uto238U = r_236Uto238U;
        R_238U1Hto238U = r_238U1Hto238U;
        R_234Uto238UError = r_234Uto238UError;
        R_235Uto238UError = r_235Uto238UError;
        R_236Uto238UError = r_236Uto238UError;
        R_238U1Hto238UError = r_238U1Hto238UError;
    }

    public DataSet() {
    }

    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {

        return numerator.divide(denominator, SCALE_FOR_DIVIDING, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getR_234Uto238U() {
        return R_234Uto238U;
    }

    public BigDecimal getR_235Uto238U() {
        return R_235Uto238U;
    }

    public BigDecimal getR_236Uto238U() {
        return R_236Uto238U;
    }

    public BigDecimal getR_236Uto238Ucorr() {
        return R_236Uto238Ucorr;
    }

    public BigDecimal getR_238U1Hto238U() {
        return R_238U1Hto238U;
    }

    public BigDecimal getR_234Uto238UError() {
        return R_234Uto238UError;
    }

    public BigDecimal getR_235Uto238UError() {
        return R_235Uto238UError;
    }

    public BigDecimal getR_236Uto238UError() {
        return R_236Uto238UError;
    }

    public BigDecimal getR_236Uto238UcorrError() {
        return R_236Uto238UcorrError;
    }

    public BigDecimal getR_238U1Hto238UError() {
        return R_238U1Hto238UError;
    }

    protected void correctU236() {
        BigDecimal hydrideOf235U = R_235Uto238U.multiply(R_238U1Hto238U);
        R_236Uto238Ucorr = R_236Uto238U.subtract(hydrideOf235U);
        if (R_236Uto238Ucorr.compareTo(ZERO) <0 ) {
            R_236Uto238Ucorr = ZERO;
        }
        BigDecimal hydrideOf235UError = divide(R_235Uto238UError, R_235Uto238U).add(divide(R_238U1Hto238UError, R_238U1Hto238U));
        R_236Uto238UcorrError = R_236Uto238UError.add(hydrideOf235U.multiply(hydrideOf235UError));
    }

    public abstract void calculateFinalValues();
}
