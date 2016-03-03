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

package it.eng.spagobi.tools.dataset.event;

import java.util.ArrayList;
import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * 
 * This class represent a event notified by the Dataset event manager
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class DataSetEvent {
	
	public final static int DELETE=0;
	public final static int INSERT=1;
	public final static int CHANGE=2;
	public final static int RESTORE_OLD_VERSION=3;
	
	/**
	 * List of datasets involved in the event
	 */
	private List<IDataSet> dataSets;
	private int code;
	
	protected DataSetEvent(int code){
		dataSets = new ArrayList<IDataSet>();
		this.code = code;
	}
	
	public DataSetEvent(int code, IDataSet dataSet ){
		this(code);
		dataSets.add(dataSet);
		
	}
	
	public DataSetEvent(int code, IDataSet dataSet1, IDataSet dataSet2 ){
		this(code);
		dataSets.add(dataSet1);
		dataSets.add(dataSet2);
	}

	public int getCode() {
		return code;
	}
	
	public IDataSet getDataSet(){
		return dataSets.get(0);
	}
	
	public IDataSet getSecondDataSet(){
		if(dataSets.size()>1){
			return dataSets.get(1);
		}
		return null;
	}
	
}
