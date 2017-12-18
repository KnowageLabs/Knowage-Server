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
package it.eng.spagobi.tools.dataset.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractDatasetMetadataXMLTemplateLoader implements IDatasetMetadataXMLTemplateLoader {

	IDatasetMetadataXMLTemplateLoader nextLoader;
	
	public AbstractDatasetMetadataXMLTemplateLoader() {}

	public AbstractDatasetMetadataXMLTemplateLoader(IDatasetMetadataXMLTemplateLoader loader) {
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
	
	
	public IDatasetMetadataXMLTemplateLoader getNextLoader() {
		return nextLoader;
	}

	void setNextLoader(IDatasetMetadataXMLTemplateLoader nextLoader) {
		this.nextLoader = nextLoader;
	}
}
