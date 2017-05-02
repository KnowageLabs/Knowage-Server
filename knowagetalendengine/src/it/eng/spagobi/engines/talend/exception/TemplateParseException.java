/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.exception;

import it.eng.spago.base.SourceBean;

/**
 * @author Andrea Gioia
 *
 */
public class TemplateParseException extends TalendEngineException {
	
	private SourceBean template;
	
	public TemplateParseException(SourceBean template, String message) {
    	super(message);
    	setTemplate(template);
    }
	
   
    public TemplateParseException(SourceBean template, String message, Throwable ex) {
    	super(message, ex);
    	setTemplate(template);
    }
    
  
    public TemplateParseException(SourceBean template, Throwable ex) {
    	super(ex);
    	setTemplate(template);
    }
    
    public SourceBean getTemplate() {
		return template;
	}


	public void setTemplate(SourceBean template) {
		this.template = template;
	}
}
