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
package it.eng.spagobi.dataset.cache.impl.sqldbcache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class DataType {
	
	//occupation in byte
	private static String Object_type_size = "8";
	private static String Float_type_size = "16";
	private static String Double_type_size = "16";
	private static String Integer_type_size = "16";
	private static String Long_type_size = "16";
	private static String BigInteger_type_size = "56";
	private static String BigDecimal_type_size = "72";
	private static String Boolean_type_size = "16";
	private static String Date_type_size = "24";
	private static String Timestamp_type_size = "32";		
	private static String String_type_size = "4038";
	private static String BLOB_type_size = "1048576" ;//<!-- BLOB default: 1024x1024 -->
	private static String CLOB_type_size = "10000" ;   //<!-- CLOB -->
	
	private List<Properties> props;

	
	
	public DataType(){
		props = new ArrayList<Properties>();
		
		addProperty("Object",Object_type_size);
		addProperty("Float",Float_type_size);
		addProperty("Double",Double_type_size);
		addProperty("Integer",Integer_type_size);
		addProperty("Long",Long_type_size);
		addProperty("BigInteger",BigInteger_type_size);
		addProperty("BigDecimal",BigDecimal_type_size);
		addProperty("Boolean",Boolean_type_size);
		addProperty("Date",Date_type_size);
		addProperty("Timestamp",Timestamp_type_size);
		addProperty("String",String_type_size);
		addProperty("[B",BLOB_type_size);
		addProperty("[C",CLOB_type_size);

	}



	/**
	 * @return the props
	 */
	public List<Properties> getProps() {
		return props;
	}



	/**
	 * @param props the props to set
	 */
	public void setProps(List<Properties> props) {
		this.props = props;
	}
	
	public void addProperty(String name, String bytes){
		Properties property = new Properties();
		if(name != null){
			property.setProperty("name", name);
		}
		if(bytes != null) {
			property.setProperty("bytes", bytes);
		}
		props.add(property);
	}
	
	


}
