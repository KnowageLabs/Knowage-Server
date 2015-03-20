/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ISelectField extends IQueryField{
	public final static String SIMPLE_FIELD = "datamartField";
	public final static String CALCULATED_FIELD = "calculated.field";
	public final static String IN_LINE_CALCULATED_FIELD = "inline.calculated.field";
	

	void setAlias(String alias);
	

	void setName(String name);
	
	String getType();	
	void setType(String type);	
	boolean isSimpleField();
	boolean isInLineCalculatedField();
	boolean isCalculatedField();
	
	boolean isGroupByField() ;
	void setGroupByField(boolean groupByField) ;
	boolean isOrderByField() ;
	boolean isAscendingOrder() ;
	String getOrderType() ;
	void setOrderType(String orderType) ;
	
	boolean isVisible() ;
	void setVisible(boolean visible);

	boolean isIncluded();
	void setIncluded(boolean include) ;
	
	String getNature();
	void setNature(String nature);
	
	ISelectField copy();
}
