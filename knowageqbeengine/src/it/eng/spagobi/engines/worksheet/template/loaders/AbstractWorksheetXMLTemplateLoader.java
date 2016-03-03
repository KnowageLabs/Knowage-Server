/*
* Knowage, Open Source Business Intelligence suite
* Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
* 
* Knowage is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Knowage is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
