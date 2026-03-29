package S2.Statistics;

public class WeightedStatistic extends Statistic {
    private double sumWeightedValues;
    private double weightedSum;

    public WeightedStatistic() {
        super();
        this.sumWeightedValues = 0;
        this.weightedSum = 0;
    }

    @Override
    public void addValue(double value, double valueWeight) {
        this.sumWeightedValues += valueWeight;
        this.weightedSum += value * valueWeight;
        this.increaseCounter();
        this.setValue(weightedSum / this.sumWeightedValues);
    }
}
