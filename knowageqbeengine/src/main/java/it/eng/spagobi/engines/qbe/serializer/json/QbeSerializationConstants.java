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
package it.eng.spagobi.engines.qbe.serializer.json;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QbeSerializationConstants {
	public static transient String SLOT_NAME = "name";
	public static transient String SLOT_VALUESET = "valueset";	
	public static transient String SLOT_VALUESET_TYPE = "type";
	public static transient String SLOT_VALUESET_TYPE_PUNCTUAL  = "punctual";
	public static transient String SLOT_VALUESET_TYPE_RANGE  = "range";
	public static transient String SLOT_VALUESET_TYPE_DEFAULT  = "default";
	public static transient String SLOT_VALUESET_VALUES = "values";	
	public static transient String SLOT_VALUESET_FROM = "from";
	public static transient String SLOT_VALUESET_INCLUDE_FROM = "includeFrom";
	public static transient String SLOT_VALUESET_TO = "to";
	public static transient String SLOT_VALUESET_INCLUDE_TO = "includeTo";
}
