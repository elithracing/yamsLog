package common;

/**
 * Used for passing data around.
 *
 * @author jonasbromo
 * edited by jakob lövhall
 */
public class ERDataField {
    private float value = 0;

    private final String _attribute;
    private final String _sensor;

    public ERDataField(String sensor, String attribute) {
        _sensor = sensor;
        _attribute = attribute;
    }

    public void setValue(short n) {
        value = n;
    }

    public void setValue(float n) {
        value = n;
    }

    public float getValue() {
        return value;
    }

    public String getSensor() {
        return _sensor;
    }

    public String getAttribute() {
        return _attribute;
    }
}
