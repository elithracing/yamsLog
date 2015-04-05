package filters;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-12-26
 * Time: 20:57
 */

/**
 * Used for unfiltered data.
 */
public class DefaultFilter implements FilterStrategy {
    @Override
    public float filter(float in) {
        return in;
    }
}
