/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/* 
 * Created on 21-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.validation;

import org.apache.commons.validator.UrlValidator;
import org.apache.oro.text.perl.Perl5Util;

/**
 * @author AZ
 *
 * This class contains code from Jakarta Commons Validator
 * It'is implemented as a subclass of URLValidator to solve the bugs described in
 * http://issues.apache.org/bugzilla/attachment.cgi?id=14369 so in Spago Validator we can use SpagoURLValidator to
 * validate URL
 * 
 */
public class SpagoURLValidator extends UrlValidator {
	/**
     * Allows all validly formatted schemes to pass validation instead of supplying a
     * set of valid schemes.
     */
    public static final int ALLOW_ALL_SCHEMES = 1 << 0;
    
    /**
     * Allow two slashes in the path component of the URL.
     */
    public static final int ALLOW_2_SLASHES = 1 << 1;

    /**
     * Enabling this options disallows any URL fragments.
     */
    public static final int NO_FRAGMENTS = 1 << 2;
    /**
     * Allows alphabetic chars
     */
    private static final String ALPHA_CHARS = "a-zA-Z";
    /**
     * Allows alphanumeric chars
     */
    private static final String ALPHA_NUMERIC_CHARS = ALPHA_CHARS + "\\d";
    /**
     * Define all special chars
     */
    private static final String SPECIAL_CHARS = ";/@&=,.?:+$";
    /**
     * The set of all valid chars
     */
    private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";

    private static final String SCHEME_CHARS = ALPHA_CHARS;

    // Drop numeric, and  "+-." for now
    private static final String AUTHORITY_CHARS = ALPHA_NUMERIC_CHARS + "\\-\\.";

    private static final String ATOM = VALID_CHARS + '+';

    /**
     * This expression derived/taken from the BNF for URI (RFC2396).
     */
    private static final String URL_PATTERN =
            "/^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?/";
    //                                                                      12            3  4          5       6   7        8 9

    /**
     * Schema/Protocol (ie. http:, ftp:, file:, etc).
     */
    private static final int PARSE_URL_SCHEME = 2;

    /**
     * Includes hostname/ip and port number.
     */
    private static final int PARSE_URL_AUTHORITY = 4;

    private static final int PARSE_URL_PATH = 5;

    private static final int PARSE_URL_QUERY = 7;

    private static final int PARSE_URL_FRAGMENT = 9;

    /**
     * Protocol (ie. http:, ftp:,https:).
     */
    private static final String SCHEME_PATTERN = "/^[" + SCHEME_CHARS + "]/";

    private static final String AUTHORITY_PATTERN =
            "/^([" + AUTHORITY_CHARS + "]*)(:\\d*)?(.*)?/";
    //                                                                            1                          2  3       4

    private static final int PARSE_AUTHORITY_HOST_IP = 1;

    private static final int PARSE_AUTHORITY_PORT = 2;

    /**
     * Should always be empty.
     */
    private static final int PARSE_AUTHORITY_EXTRA = 3;

    // Error as described in http://issues.apache.org/bugzilla/attachment.cgi?id=14369
    //private static final String PATH_PATTERN ="/^(/[-a-zA-Z0-9_:@&?=+,.!/~*'%$]*)$/";
    private static final String PATH_PATTERN="/^(/[-\\w:@&?=+,.!/~*'%$]*)*$/";

    private static final String QUERY_PATTERN = "/^(.*)$/";

    private static final String LEGAL_ASCII_PATTERN = "/^[\\000-\\177]+$/";

    private static final String IP_V4_DOMAIN_PATTERN =
            "/^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$/";

    private static final String DOMAIN_PATTERN =
            "/^" + ATOM + "(\\." + ATOM + ")*$/";

    private static final String PORT_PATTERN = "/^:(\\d{1,5})$/";

    private static final String ATOM_PATTERN = "/(" + ATOM + ")/";

    private static final String ALPHA_PATTERN = "/^[" + ALPHA_CHARS + "]/";
    
   

 
   
    /**
     * Controls if a String at imput is a valid URL.
     * 
     * @param value The input string
     * 
     * @return True if the string is a valid URL, else false.
     */
	public boolean isValid(String value) {
		if (value == null) {
            return false;
        }

        Perl5Util matchUrlPat = new Perl5Util();
        Perl5Util matchAsciiPat = new Perl5Util();

        if (!matchAsciiPat.match(LEGAL_ASCII_PATTERN, value)) {
            return false;
        }

        // Check the whole url address structure
        if (!matchUrlPat.match(URL_PATTERN, value)) {
            return false;
        }

        if (!isValidScheme(matchUrlPat.group(PARSE_URL_SCHEME))) {
            return false;
        }
        
        /*
        if (!isValidAuthority(matchUrlPat.group(PARSE_URL_AUTHORITY))) {
            return false;
        }
        */

        if (!isValidPath(matchUrlPat.group(PARSE_URL_PATH))) {
            return false;
        }

        if (!isValidQuery(matchUrlPat.group(PARSE_URL_QUERY))) {
            return false;
        }

        if (!isValidFragment(matchUrlPat.group(PARSE_URL_FRAGMENT))) {
            return false;
        }

        return true;
	}
	/**
	 * Controls if the input string represents a valid path.
	 * 
	 * @param path The input string path
	 * @return True if the string represents a valid path, else false
	 * 
	 */
	 protected boolean isValidPath(String path) {
        if (path == null) {
            return false;
        }

        Perl5Util pathMatcher = new Perl5Util();

        if (!pathMatcher.match(PATH_PATTERN, path)) {
            return false;
        }

        //if (path.endsWith("/")) {
        //    return false;
        //}

        int slash2Count = countToken("//", path);
        //if (this.options.isOff(ALLOW_2_SLASHES) && (slash2Count > 0)) {
        //    return false;
        //}

        int slashCount = countToken("/", path);
        int dot2Count = countToken("..", path);
        if (dot2Count > 0) {
            if ((slashCount - slash2Count - 1) <= dot2Count) {
                return false;
            }
        }

        return true;
    }
}
