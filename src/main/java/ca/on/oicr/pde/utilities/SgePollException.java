/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities;

/**
 * An exception for the SGE polling class.
 * @author mtaschuk
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
