package S2.Statistics;

public abstract class Statistic {
    private double value;
    private int counter;

    public abstract void addValue(double value, double valueWeight);

    public Statistic() {
        this.value = this.counter = 0;
    }

    protected void setValue(double value) {
        this.value = value;
    }

    protected void increaseCounter() {
        this.counter++;
    }

    public double getValue() {
        return value;
    }

    public int getCounter() {
        return this.counter;
    }
}
