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
package it.eng.spagobi.utilities.comparator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;


/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * This class provide some utility for compare two objects
 */
public class ObjectComparator {
	
	
	public static final int DESCENDING = 1;
	private DateFormat dateFormatter;
	private DateFormat timestampFormatter;
	
	/**
	 * Build the comparator object
	 * @param dateFormatter the date formatter for the dates
	 * @param timestampFormatter the date formatter for the timestamps
	 */
	public ObjectComparator(DateFormat dateFormatter, DateFormat timestampFormatter){
		this.dateFormatter = dateFormatter;
		this.timestampFormatter = timestampFormatter;
	}
	
	/**
	 * Compare two strings that contain two object of type "type"
	 * @param obj1 the first object 
	 * @param obj2 the first object 
	 * @param type the type of the two objects
	 * @param direction the direction for the comparison (1 for descending)
	 * @return the comparison result
	 * @throws ParseException if an exception occurs during parsing of the dates
	 */
	public int compare(String obj1, String obj2, Class type, int direction) throws ParseException {
		int rc = 1;
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -1;
		}
		if(obj1==null || obj1==""){
			return 100;
		}
		if (obj2==null || obj2==""){
			return (-100);
		}
		if(type.isAssignableFrom(BigDecimal.class)){
			BigDecimal d1 = new BigDecimal(obj1);
			BigDecimal d2 = new BigDecimal(obj2);
			return rc*d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(BigInteger.class)){
			BigInteger d1 = new BigInteger(obj1);
			BigInteger d2 = new BigInteger(obj2);
			return rc*d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)){
			Byte d1 = new Byte(obj1);
			Byte d2 = new Byte(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)){
			Float d1 = new Float(obj1);
			Float d2 = new Float(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)){
			Double d1 = new Double(obj1);
			Double d2 = new Double(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)){
			Integer d1 = new Integer(obj1);
			Integer d2 = new Integer(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)){ 
			Long d1 = new Long(obj1);
			Long d2 = new Long(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)){
			Long d1 = new Long(obj1);
			Long d2 = new Long(obj2);
			return rc* d1.compareTo(d2);
		} 
		if(type.isAssignableFrom(Date.class)){
			Date d1 = dateFormatter.parse(obj1);
			Date d2 = dateFormatter.parse(obj2);
			return rc* d1.compareTo(d2);
		} else if(type.isAssignableFrom(Timestamp.class)){
			Date d1 = timestampFormatter.parse(obj1);
			Date d2 = timestampFormatter.parse(obj2);
			return rc* d1.compareTo(d2);
		}
		return rc*obj1.compareTo(obj2);
	}
}
