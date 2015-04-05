package common;

import java.util.Iterator;

/**
 * Created by max on 2015-03-13.
 */
public class ERSensorConfig implements Iterable<String> {

    public String sensor;
    public String[] attributes;

    @Override
    public Iterator<String> iterator() {
        return new ERSensorConfigIterator();
    }

    private class ERSensorConfigIterator implements Iterator<String> {
        private int iter = 0;
        @Override
        public boolean hasNext() {
            return attributes.length > iter;
        }

        @Override
        public String next() {
            return attributes[iter++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
