package nl.captcha.text.producer;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Produces text of a given length from a given array of characters. The default (no-args)
 * constructor produces five Latin characters from a specific set of alphanumerics. Characters
 * such as "1" (one) and "l" (lower-case 'ell') have been removed to reduce ambiguity in the
 * generated CAPTCHA.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class DefaultTextProducer implements TextProducer {

    private static final Random _gen = new SecureRandom();
    private static final int DEFAULT_LENGTH = 5;
    private static final char[] DEFAULT_CHARS = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };
    
    private final int _length;
    private final char[] _srcChars;

    public DefaultTextProducer() {
    	this(DEFAULT_LENGTH, DEFAULT_CHARS);
    }
    
    public DefaultTextProducer(int length) {
    	this(length, DEFAULT_CHARS);
    }
    
    public DefaultTextProducer(int length, char[] srcChars) {
    	_length = length;
    	_srcChars = srcChars != null ? copyOf(srcChars, srcChars.length) : DEFAULT_CHARS;
    }
    
    public String getText() {
        int car = _srcChars.length - 1;

        String capText = "";
        for (int i = 0; i < _length; i++) {
            capText += _srcChars[_gen.nextInt(car) + 1];
        }

        return capText;
    }
    
    public static char[] copyOf(char[] original, int newLength) {
        char[] copy = new char[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
}
