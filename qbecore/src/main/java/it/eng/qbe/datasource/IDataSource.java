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
package it.eng.qbe.datasource;

import java.util.Locale;

import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.transaction.ITransaction;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author Andrea Gioia
 */
public interface IDataSource {

	String getName();

	IDataSourceConfiguration getConfiguration();

	IModelStructure getModelStructure();

	IModelStructure getModelStructure(UserProfile profile);

	IModelStructure getModelStructure(boolean getFullModel);

	IModelAccessModality getModelAccessModality();

	void setDataMartModelAccessModality(IModelAccessModality modelAccessModality);

	IModelProperties getModelI18NProperties(Locale locale);

	void open();

	boolean isOpen();

	void close();

	IStatement createStatement(Query query);

	ITransaction getTransaction();

	IPersistenceManager getPersistenceManager();

}
