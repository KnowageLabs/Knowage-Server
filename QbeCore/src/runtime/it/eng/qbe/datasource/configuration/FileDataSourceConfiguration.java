/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration;


import it.eng.qbe.datasource.configuration.dao.fileimpl.CalculatedFieldsDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.ModelI18NPropertiesDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.ModelPropertiesDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.RelationshipsDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.ViewsDAOFileImpl;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FileDataSourceConfiguration extends DelegatingDataSourceConfiguration {
	
	File file;
	
	public FileDataSourceConfiguration(String modelName, File file) {
		super(modelName);
		this.file = file;
		this.modelPropertiesDAO = new ModelPropertiesDAOFileImpl(file);
		this.modelLabelsDAOFileImpl = new ModelI18NPropertiesDAOFileImpl(file);
		this.calculatedFieldsDAO = new CalculatedFieldsDAOFileImpl(file);	
		this.viewsDAO = new ViewsDAOFileImpl(file);	
		this.relationshipsDAO = new RelationshipsDAOFileImpl(file);	
		this.functionsDAO = new InLineFunctionsDAOFileImpl();
	}
	
	public File getFile() {
		return file;
	}
}
