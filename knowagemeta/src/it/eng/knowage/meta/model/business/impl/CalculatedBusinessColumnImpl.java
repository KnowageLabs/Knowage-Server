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
package it.eng.knowage.meta.model.business.impl;

import it.eng.knowage.meta.exception.KnowageMetaException;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModelPackage;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Calculated Business Column</b></em>'. <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class CalculatedBusinessColumnImpl extends BusinessColumnImpl implements CalculatedBusinessColumn {

	public static final String CALCULATED_COLUMN_EXPRESSION = "structural.expression";
	public static transient Logger logger = Logger.getLogger(CalculatedBusinessColumnImpl.class);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected CalculatedBusinessColumnImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BusinessModelPackage.Literals.CALCULATED_BUSINESS_COLUMN;
	}

	@Override
	public List<SimpleBusinessColumn> getReferencedColumns() throws KnowageMetaException {
		List<SimpleBusinessColumn> columnsReferenced = new ArrayList<SimpleBusinessColumn>();
		BusinessColumnSet businessColumnSet = this.getTable();

		// get Expression String
		String id = this.getPropertyType(CALCULATED_COLUMN_EXPRESSION).getId();
		String expression = this.getProperties().get(id).getValue();

		// retrieve columns objects from string
		StringTokenizer stk = new StringTokenizer(expression, "+-|*/()");
		while (stk.hasMoreTokens()) {
			String operand = stk.nextToken().trim();
			List<SimpleBusinessColumn> businessColumns = businessColumnSet.getSimpleBusinessColumnsByName(operand);
			if (businessColumns.isEmpty()) {
				// throws exception
				throw new KnowageMetaException("No columns using the name [" + operand + "] are found in the expression of Calculated Field [" + this.getName()
						+ "]");
			} else {
				if (businessColumns.size() > 1) {
					logger.warn("More columns using the name [" + operand + "] are found in the expression of Calculated Field [" + this.getName() + "]");
				}
			}

			// always get first SimpleBusinessColumn found with that name (operand)
			SimpleBusinessColumn simpleBusinessColumn = businessColumns.get(0);
			if (simpleBusinessColumn != null) {
				columnsReferenced.add(simpleBusinessColumn);
			}
		}
		return columnsReferenced;
	}

} // CalculatedBusinessColumnImpl
