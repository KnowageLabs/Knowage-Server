/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.spagobi.meta.model.business.impl;

import it.eng.spagobi.commons.exception.SpagoBIPluginException;
import it.eng.spagobi.meta.model.business.BusinessColumnSet;
import it.eng.spagobi.meta.model.business.BusinessModelPackage;
import it.eng.spagobi.meta.model.business.CalculatedBusinessColumn;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Calculated Business Column</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class CalculatedBusinessColumnImpl extends BusinessColumnImpl implements CalculatedBusinessColumn {
	
	public static final String CALCULATED_COLUMN_EXPRESSION = "structural.expression";
    public static transient Logger logger = Logger.getLogger(CalculatedBusinessColumnImpl.class);

	
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CalculatedBusinessColumnImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.CALCULATED_BUSINESS_COLUMN;
	}
	
	public List<SimpleBusinessColumn> getReferencedColumns() throws SpagoBIPluginException{
		List<SimpleBusinessColumn> columnsReferenced = new ArrayList<SimpleBusinessColumn>();
		BusinessColumnSet businessColumnSet = this.getTable();

		//get Expression String
		String id = this.getPropertyType(CALCULATED_COLUMN_EXPRESSION).getId();
		String expression = this.getProperties().get(id).getValue();

		//retrieve columns objects from string
		StringTokenizer stk = new StringTokenizer(expression, "+-|*/()");
		while(stk.hasMoreTokens()){
			String operand = stk.nextToken().trim();
			List<SimpleBusinessColumn> businessColumns = businessColumnSet.getSimpleBusinessColumnsByName(operand);
			if (businessColumns.isEmpty()){
				//throws exception
				throw new SpagoBIPluginException("No columns using the name ["+operand+"] are found in the expression of Calculated Field ["+this.getName()+"]");
			}
			else{
				if (businessColumns.size() >1 ){
					logger.warn("More columns using the name ["+operand+"] are found in the expression of Calculated Field ["+this.getName()+"]");
				}
			}

			//always get first SimpleBusinessColumn found with that name (operand)
			SimpleBusinessColumn simpleBusinessColumn = businessColumns.get(0);
			if (simpleBusinessColumn != null){
				columnsReferenced.add(simpleBusinessColumn);
			}
		}
		return columnsReferenced;	
	}

} //CalculatedBusinessColumnImpl
