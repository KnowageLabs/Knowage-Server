/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.template.loaders;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public abstract class AbstractWorksheetXMLTemplateLoader implements IWorksheetXMLTemplateLoader {
	
	IWorksheetXMLTemplateLoader nextLoader;
	
	public AbstractWorksheetXMLTemplateLoader() {}

	public AbstractWorksheetXMLTemplateLoader(IWorksheetXMLTemplateLoader loader) {
		setNextLoader(loader);
	}
	
	public SourceBean load(String rowData) {
		SourceBean result;
		
		try {
			// load data
			result = SourceBean.fromXMLString(rowData);
			result = this.load(result);
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + rowData + "]", t);
		}
		
		return result;
	}
	
	public SourceBean load(SourceBean xml) {
		SourceBean result;
		
		try {
			result = this.convert(xml);
			// make next converts
			if (nextLoader != null) {
				result = nextLoader.load(result);
			}
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from XML [" + xml + "]", t);
		}
		
		return result;
	}
	
	abstract public SourceBean convert(SourceBean xml);
	
	
	public IWorksheetXMLTemplateLoader getNextLoader() {
		return nextLoader;
	}

	void setNextLoader(IWorksheetXMLTemplateLoader nextLoader) {
		this.nextLoader = nextLoader;
	}
}
