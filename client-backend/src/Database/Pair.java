package Database;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-05-18
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class Pair<F, S> {
    protected F first;
    protected S second;
    public Pair(F f, S s){
        this.first = f;
        this.second = s;
    }
}