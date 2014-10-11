package Errors;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-03-05
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class BackendError extends Exception{
    public BackendError() {super();}
    public BackendError(String message){super(message);}
}
