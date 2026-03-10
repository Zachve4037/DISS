package timeTracker;

public class TimeTracker {
    private int hours;
    private int minutes;
    private int seconds;

    public TimeTracker(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public void addTime(int hours, int minutes, int seconds) {
        this.hours += hours;
        this.minutes += minutes;
        this.seconds += seconds;
        normalizeTime();
    }

    public void addMinutes(double minutes) {
        int wholeMinutes = (int) minutes;
        int additionalSeconds = (int) ((minutes - wholeMinutes) * 60);
        addTime(0, wholeMinutes, additionalSeconds);
    }

    private void normalizeTime() {
        if (this.seconds >= 60) {
            this.minutes += this.seconds / 60;
            this.seconds = this.seconds % 60;
        }
        if (this.minutes >= 60) {
            this.hours += this.minutes / 60;
            this.minutes = this.minutes % 60;
        }
    }

    public String getTime() {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public TimeTracker copy() {
        return new TimeTracker(this.hours, this.minutes, this.seconds);
    }

    public boolean isAfter(int h, int m, int s) {
        return toTotalSeconds() > (h * 3600 + m * 60 + s);
    }

    public int toTotalSeconds() {
        return hours * 3600 + minutes * 60 + seconds;
    }
}
