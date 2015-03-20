/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Custom tag that retrieves message using spago facilities
 * 
 * @author zoppello
 */
public class SpagoBIMessageTag extends TagSupport {

	private static final String DEFAULT_BUNDLE = "messages";
	
	/**
     * The arguments.
     */
    protected String args = null;
    
    /**
     * The message key of the message to be retrieved.
     */
    protected String key = null;
    
    /**
     * The servlet context attribute key for our resources.
     */
    protected String bundle = null;
        
    /**
     * Gets the args.
     * 
     * @return The arguments
     */
    public String getArgs() {
        return (this.args);
    }
    
    /**
     * Sets the args.
     * 
     * @param args The arguments to set
     */
    public void setArgs(String args) {
    	this.args = args;
    }
    
    /**
     * Gets the bundle.
     * 
     * @return The servlet context attribute key
     */
    public String getBundle() {
        return (this.bundle);
    }
    
    /**
     * Sets the bundle.
     * 
     * @param bundle The servlet context attribute key to set
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }
    
    /**
     * Gets the key.
     * 
     * @return The reference key
     */
    public String getKey() {
        return (this.key);
    }
    
    /**
     * Sets the key.
     * 
     * @param key The key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Process the start tag.
     * 
     * @return the int
     * 
     * @throws JspException the jsp exception
     * 
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {

        String key = this.key;
        // Construct the optional arguments array we will be using
        Object[] arguments = new Object[0];
        if(args!=null) {
        	arguments = args.split("\\|");
        }
        // get http request    
        HttpServletRequest httpRequest = (HttpServletRequest)pageContext.getRequest();
        // get message builder
        IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
        // get message 
        String message = null;        
        if (bundle != null) {
        	message = msgBuilder.getMessage(key, bundle, httpRequest);
        	//message = PortletUtilities.getMessage(key, bundle);
        } else {
        	message = msgBuilder.getMessage(key, DEFAULT_BUNDLE, httpRequest);
        	//message = getMessage(renderRequest, key); // Use the default spago bundle
        }
        // replace arguments into message
        for (int i=0; i<arguments.length; i++){
        	message = replace(message, i, arguments[i].toString());
        }
        // return message
        StringBuffer htmlStream = new StringBuffer();
        htmlStream.append(message);        
        try {
            pageContext.getOut().print(htmlStream);
        } 
        catch (Exception ex) {
            SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
            		               "doStartTag", "Impossible to elaborate pageContext stream");
            throw new JspException("Impossible to elaborate pageContext stream");
        } 
        return SKIP_BODY;
	}
	
	
    /**
     * Release any acquired resources.
     */
    public void release() {

        super.release();
        key = null;
    }
    /**
     * Substitutes the message value to the placeholders.
     * 
     * @param messageFormat The String representing the message format
     * @param iParameter	The numeric value defining the replacing string
     * @param value	Input object containing parsing information
     * @return	The parsed string
     */
    protected String replace(String messageFormat, int iParameter, Object value) {
		if (value != null) {
			String toParse = messageFormat;
			String replacing = "%" + iParameter;
			String replaced = value.toString();
			StringBuffer parsed = new StringBuffer();
			int parameterIndex = toParse.indexOf(replacing);
			while (parameterIndex != -1) {
				parsed.append(toParse.substring(0, parameterIndex));
				parsed.append(replaced);
				toParse = toParse.substring(
						parameterIndex + replacing.length(), toParse.length());
				parameterIndex = toParse.indexOf(replacing);
			} // while (parameterIndex != -1)
			parsed.append(toParse);
			return parsed.toString();
		} else {
			return messageFormat;
		}
	}
    
    
	
}