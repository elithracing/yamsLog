package Errors;

/**
 * Created by Johan on 2014-03-27.
 */
public class SensorDoesNotExistError extends BackendError {
    public SensorDoesNotExistError() {super();}
    public SensorDoesNotExistError(Integer message) { super(message.toString());}
}
