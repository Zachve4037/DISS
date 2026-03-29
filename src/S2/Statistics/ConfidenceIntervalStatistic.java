package S2.Statistics;

public class ConfidenceIntervalStatistic extends Statistic {
    private double mean;
    private double m2;

    public ConfidenceIntervalStatistic() {
        super();
        this.mean = m2 = 0;
    }

    @Override
    public void addValue(double value, double valueWeight) {
        this.increaseCounter();
        double delta = value - this.mean;
        this.mean += delta / this.getCounter();
        this.m2 += delta * (value - this.mean);

        double variance = m2 / (this.getCounter() - 1);
        double stdev = Math.sqrt(variance);

        double halfInterval = 1.96 * stdev / Math.sqrt(this.getCounter());
        this.setValue(halfInterval);
    }
}
