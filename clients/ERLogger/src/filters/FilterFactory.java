package filters;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob Lövhall
 * Date: 2014-03-28
 */

public class FilterFactory {
    private HashMap<String,FilterParameters> parameterMap;

    public FilterFactory() {
        parameterMap = new HashMap<>();
    }

    public HashMap<String, FilterParameters> getParameterMap() {
        return parameterMap;
    }

    public FilterStrategy createFilter(String sensor){
        FilterParameters fp = parameterMap.get(sensor);

        FilterStrategy f;

        if (fp != null) {
            f = getFilter(fp);
        }
        else {
            f = new DefaultFilter();
        }

        return f;
    }

    private FilterStrategy getFilter(FilterParameters fp){
        switch (fp.getType()) {
            case FIR:
                return new FIRFilter(fp.getAverage());
            case IIR:
                return new IIRFilter(fp.getScale());
            default:
                return new DefaultFilter();
        }
    }
}
