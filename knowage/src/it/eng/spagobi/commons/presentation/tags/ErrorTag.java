/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.ResponseContainer;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * It is used when any errors occurs during the execution of a tag. 
 * It handles errors throwing exceptions.
 */
public class ErrorTag extends TagSupport  {
	
	/**
	 * Do start tag.
	 * 
	 * @return the int
	 * 
	 * @throws JspException the jsp exception
	 * 
	 * @see it.eng.spagobi.commons.presentation.tags.ListTag#doStartTag()
	 */
	public int doStartTag() throws JspException {
        TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG, 
        		            "ErrorTag::doStartTag:: invoked");
        if (pageContext == null) {
            TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.CRITICAL,
                				"ErrorTag::doStartTag:: pageContext null");
            throw new JspException("pageContext nullo");
        } // if (_httpRequest == null)
        HttpServletRequest httpRequest = null;
        try {
            httpRequest = (HttpServletRequest)pageContext.getRequest();
        } // try
        catch (Exception ex) {
            TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.CRITICAL, "ErrorTag::doStartTag::", ex);
            throw new JspException(ex.getMessage());
        } // catch (Exception ex)
        if (httpRequest == null) {
            TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.CRITICAL,
                "ErrorTag::doStartTag:: httpRequest null");
            throw new JspException("httpRequest nullo");
        } // if (_httpRequest == null)
        //ResponseContainer responseContainer = ResponseContainerPortletAccess.getResponseContainer(httpRequest);
        ResponseContainer responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
        if (responseContainer == null) {
            TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.CRITICAL,
                "ErrorTag::doStartTag:: responseContainer null");
            throw new JspException("responseContainer null");
        } // if (responseContainer == null)
        StringBuffer output = new StringBuffer();
        EMFErrorHandler engErrorHandler = responseContainer.getErrorHandler();

    	Collection errors = engErrorHandler.getErrors();
    	
    	
    	if (!errors.isEmpty()) {
    		output.append("<div class='errors-object-details-div'>\n");
	    	output.append("	<div class='div_detail_errors'>\n");
	    	output.append("		<div class='portlet-section-header'>\n");
	    	output.append("			ERRORS:\n");
	    	output.append("		</div>\n");
	    	output.append("		<div class='portlet-msg-error'>\n");
	    	output.append("			<ul class='ul_detail_error'>\n");
	    	EMFAbstractError error = null;
	    	String description = "";
	    	Iterator iter = errors.iterator();
	    	while(iter.hasNext()) {
	    		error = (EMFAbstractError)iter.next();
	    	 	description = error.getDescription();
	    	 	description = StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(description));
	    	 	output.append("			<li>"+description+"</li>\n");
	    	}
	    	output.append("			</ul>\n");
	    	output.append("		</div>\n");
	    	output.append("	</div>\n");
	    	output.append("</div>\n");
    	}
    	
        
        try {
            pageContext.getOut().print(output.toString());
        } // try
        catch (Exception ex) {
            TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.CRITICAL, "ErrorTag::doStartTag::", ex);
            throw new JspException(ex.getMessage());
        } // catch (Exception ex)
        return SKIP_BODY;
    } // public int doStartTag() throws JspException

    /**
     * Do end tag.
     * 
     * @return the int
     * 
     * @throws JspException the jsp exception
     * 
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() throws JspException {
        TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG, "ErrorTag::doEndTag:: invocato");
        return super.doEndTag();
    } // public int doEndTag() throws JspException
} // public class ErrorTag extends TagSupport
