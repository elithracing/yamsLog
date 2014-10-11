package Database.Sensors;

/**
 * Implementation of a lock, using both readlock and writelock
 */
public class ReadWriteLock {

    private short readLock;     //When this variable is 0, the object is allowed to write to the lists.
    private boolean writeLock;  //When this is true, a thread is writing to the data.

    /**
     * Constructor initialising the values of writeLock and readLock
     */
    public ReadWriteLock(){
        readLock = 0;
        writeLock = false;
    }

    /**
     * Releases the lock
     * @param willWrite <Code>TRUE</Code> iff the caller has writen to the database
     *                  <Code>FALSE</Code> otherwise
     * @return
     */
    public void releaseLock(boolean willWrite){
        changeLock(false, willWrite);
    }
    /**
     * Tries to get a lock
     * @param willWrite <Code>TRUE</Code> iff the caller will write to the database
     *                  <Code>FALSE</Code> otherwise
     * @return <Code>TRUE</Code> if lock was acquired
     *         <Code>FALSE</Code> otherwise
     */
    public boolean getLock(boolean willWrite){
        return changeLock(true, willWrite);
    }

    /**
     * Internal representation of an attempt to change the lock,
     * synchronized so only one caller will be allowed at the time
     * @param lockRequest does the caller request the lock or does the caller want to release the lock
     * @param isWritingThread will/have the thread write/written
     * @return returns success of request
     */
    private synchronized boolean changeLock(boolean lockRequest,boolean isWritingThread){
        if(isWritingThread){
            if (writeLock) {
                writeLock = false;
                return false;
            } else if(readLock==0){
                writeLock = true;
                return true;
            } else {
                return false;
            }
        } else {
            if(writeLock) return false;
            if(lockRequest) readLock--;
            else readLock++;
            return true;
        }
    }

}
