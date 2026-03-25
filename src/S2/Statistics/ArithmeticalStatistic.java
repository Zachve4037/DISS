package S2.Statistics;

public class ArithmeticalStatistic extends Statistic {
    private double sum;
    public ArithmeticalStatistic() {
        super();
        this.sum = 0;
    }

    @Override
    public void addValue(double value, double weight) {
        this.sum += value;
        this.increaseCounter();
        this.setValue(this.sum / this.getCounter());
    }
}
