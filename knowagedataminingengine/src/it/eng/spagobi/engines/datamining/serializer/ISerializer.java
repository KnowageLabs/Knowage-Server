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
package it.eng.spagobi.engines.datamining.serializer;

import com.fasterxml.jackson.core.type.TypeReference;



/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public interface ISerializer extends Versionable{
	Object serialize(Object object) throws SerializationException;
	Object deserialize(String toDeserialize, Class object) throws SerializationException ;
	Object deserialize(String toDeserialize, TypeReference object) throws SerializationException ;
	String getFormat();
}
