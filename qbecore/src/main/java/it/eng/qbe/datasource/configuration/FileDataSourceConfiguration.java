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
package it.eng.qbe.datasource.configuration;


import it.eng.qbe.datasource.configuration.dao.fileimpl.CalculatedFieldsDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.HierarchiesDAOFileImpl;
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
		this.hierarchiesDAO = new HierarchiesDAOFileImpl(file);
		this.viewsDAO = new ViewsDAOFileImpl(file);
		this.relationshipsDAO = new RelationshipsDAOFileImpl(file);
		this.functionsDAO = new InLineFunctionsDAOFileImpl();
	}

	public File getFile() {
		return file;
	}
}
