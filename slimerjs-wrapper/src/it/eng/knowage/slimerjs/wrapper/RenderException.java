package it.eng.knowage.slimerjs.wrapper;

/**
 * An exception bubbled up from the render script
 */
public class RenderException extends Exception {
    
	private static final long serialVersionUID = 8583601554087541964L;

	RenderException(String s) {
        super(s);
    }
}
