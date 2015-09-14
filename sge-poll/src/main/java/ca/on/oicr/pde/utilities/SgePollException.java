package ca.on.oicr.pde.utilities;

/**
 * An exception for the SGE polling class. This exception should be thrown when 
 * an error occurs while polling SGE jobs.
 * @author Morgan Taschuk
 */
public class SgePollException extends Exception {

    public SgePollException(Throwable cause) {
        super(cause);
    }
    
    public SgePollException(String message, Throwable cause) {
        super(message, cause);
    }

    public SgePollException(String message) {
        super(message);
    }

}
