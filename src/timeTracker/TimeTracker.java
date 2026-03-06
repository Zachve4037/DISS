package timeTracker;

public class TimeTracker {
    private int hours;
    private int minutes;

    public TimeTracker() {
        this.hours = 0;
        this.minutes = 0;
    }

    public TimeTracker(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    public void addTime(int hours, int minutes) {
        this.hours += hours;
        this.minutes += minutes;
        normalizeTime();
    }

    private void normalizeTime() {
        if (this.minutes >= 60) {
            this.hours += this.minutes / 60;
            this.minutes = this.minutes % 60;
        }
    }

    public String getTime() {
        return String.format("%02d:%02d", hours, minutes);
    }
}
