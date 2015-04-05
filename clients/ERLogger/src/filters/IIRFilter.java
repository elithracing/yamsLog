package filters;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-12-16
 * Time: 08:57
 */

/**
 * IIR filter with the scale [0,1]. scale set to 1 equals all of the new value and non of the old value, 0 is only the old value and no part of the new.
 * Faster response then the FIR filter but harder to get a smooth graph
 */
public class IIRFilter implements FilterStrategy {

    private float lastOut = 0;

    private float scale;

    public IIRFilter(float scale) {
        this.scale = scale;
    }

    public float filter(float in){
        lastOut = lastOut * (1 - scale) + scale * in;
        return lastOut;
    }
}
