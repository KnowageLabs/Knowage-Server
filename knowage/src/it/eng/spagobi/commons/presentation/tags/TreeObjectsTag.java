/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * Creates the tag for tree objects
 * 
 * @author sulis
 */
public class TreeObjectsTag extends TagSupport {

	private String moduleName = null;
	private String htmlGeneratorClass = null;
	private String treeName = null;
	HttpServletRequest httpRequest = null;
	private String attributeToRender = null;
	
	/**
	 * Starting tag.
	 * 
	 * @return the int
	 * 
	 * @throws JspException the jsp exception
	 */
	public int doStartTag() throws JspException {
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		RequestContainer requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		ResponseContainer responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		SourceBean serviceRequest = requestContainer.getServiceRequest();
		SourceBean serviceResponse = responseContainer.getServiceResponse();
		SourceBean moduleResponse = (SourceBean)serviceResponse.getAttribute(moduleName);

		
		List functionalitiesList = null;
		if (attributeToRender == null || attributeToRender.trim().equals("")) {
			functionalitiesList = (List) moduleResponse.getAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST);
		} else {
			functionalitiesList = (List) moduleResponse.getAttribute(attributeToRender);
		}
		String initialPath = (String) moduleResponse.getAttribute(TreeObjectsModule.PATH_SUBTREE);
        ITreeHtmlGenerator gen = null;
        try{
        	gen = (ITreeHtmlGenerator)Class.forName(htmlGeneratorClass).newInstance();
        } catch(Exception e) {
        	return -1;
        }
        
        StringBuffer htmlStream = null;
        if(treeName==null) {
        	htmlStream = gen.makeTree(functionalitiesList, httpRequest, initialPath);
        } else {
        	htmlStream = gen.makeTree(functionalitiesList, httpRequest, initialPath, treeName);
        }
        
		try {
			pageContext.getOut().print(htmlStream);
		} catch(IOException ioe) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					            "doStartTag", "cannot start object tree tag: IOexception occurred",ioe);
		}
		return SKIP_BODY;
	}
	

	/**
	 * ending tag.
	 * 
	 * @return the int
	 * 
	 * @throws JspException the jsp exception
	 */
	public int doEndTag() throws JspException {
		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION, "TitleTag::doEndTag:: invocato");
		return super.doEndTag();
	}

	/**
	 * Gets the module name.
	 * 
	 * @return the module name
	 */
	public String getModuleName() {
		return moduleName;
	}
	
	/**
	 * Sets the module name.
	 * 
	 * @param moduleName the module name to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	/**
	 * Gets the html generator class.
	 * 
	 * @return the html generator class
	 */
	public String getHtmlGeneratorClass() {
		return htmlGeneratorClass;
	}
	
	/**
	 * Sets the html generator class.
	 * 
	 * @param htmlGeneratorClass the html generator class to set
	 */
	public void setHtmlGeneratorClass(String htmlGeneratorClass) {
		this.htmlGeneratorClass = htmlGeneratorClass;
	}


	/**
	 * Gets the tree name.
	 * 
	 * @return the tree name
	 */
	public String getTreeName() {
		return treeName;
	}


	/**
	 * Sets the tree name.
	 * 
	 * @param treeName the new tree name
	 */
	public void setTreeName(String treeName) {
		this.treeName = treeName;
	}


	/**
	 * Gets the attribute to render.
	 * 
	 * @return the attribute to render
	 */
	public String getAttributeToRender() {
		return attributeToRender;
	}


	/**
	 * Sets the attribute to render.
	 * 
	 * @param attributeToRender the new attribute to render
	 */
	public void setAttributeToRender(String attributeToRender) {
		this.attributeToRender = attributeToRender;
	}
		
	
}
