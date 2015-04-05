package filters;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob Lövhall
 * Date: 2014-04-03
 */

/**
 * XML marshal helper class. Unused functions are used by the marshal
 */
public class FilterParameters {
    private FilterType type;
    private int average;
    private float scale;

    public FilterParameters(FilterType type, int average, float scale) {
        this.type = type;
        this.average = average;
        this.scale = scale;
    }

    public FilterParameters() {
        type = null;
    }

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public int getAverage() {
        return average;
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
