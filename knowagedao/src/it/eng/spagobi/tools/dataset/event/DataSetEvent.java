/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
