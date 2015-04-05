package filters;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-12-16
 * Time: 08:57
 */



/**
 * Gives a smooth graph but has harder to get to max/min points compared to IIR filters.
 * moving average filter.
 */
public class FIRFilter implements FilterStrategy {

    private float[] _in;

    /**
     * Initiates a moving average filter with a dynamic number of elements
     * @param number the number of elements in the moving average filter
     */
    public FIRFilter(int number) {
        _in = new float[number];
    }

    /**
     * Places the new in value first in the _in array and then takes the average of the entire array
     * @param in the new in value
     * @return the average of the last values
     */
    @Override
    public float filter(float in) {
        float out = 0;

        System.arraycopy(_in, 0, _in, 1, _in.length - 1);

        _in[0] = in;

        for (float v : _in) {
            out+=v;
        }

        out /= _in.length;

        return out;
    }
}
