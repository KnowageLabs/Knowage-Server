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
package it.eng.spagobi.engines.qbe.crosstable.serializer;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.ISerializerFactory;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.engines.qbe.crosstable.serializer.json.CrosstabJSONSerializer;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CrosstabSerializerFactory implements ISerializerFactory{

	static CrosstabSerializerFactory instance;
	
	static {
		instance = new CrosstabSerializerFactory();
		SerializationManager.registerSerializerFactory(CrosstabDefinition.class, instance);	
	}
	
	
	public static CrosstabSerializerFactory getInstance() {
		if (instance == null) {
			instance = new CrosstabSerializerFactory();
		}
		return instance;
	}
	
	private CrosstabSerializerFactory() {}

	
	public ISerializer getSerializer(String mimeType) {
		return new CrosstabJSONSerializer();
	}

}
