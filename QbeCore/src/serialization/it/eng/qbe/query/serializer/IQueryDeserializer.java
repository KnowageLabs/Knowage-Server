/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.serializer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.serializer.SerializationException;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IQueryDeserializer {
	public Query deserializeQuery(Object o, IDataSource m) throws SerializationException;
	//public CrosstabDefinition deserializeCrosstabDefinition(Object o) throws SerializationException;
}
