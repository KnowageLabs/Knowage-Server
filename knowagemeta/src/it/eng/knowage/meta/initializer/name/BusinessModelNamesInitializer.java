/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
 **/
package it.eng.knowage.meta.initializer.name;

import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia
 *
 */
public class BusinessModelNamesInitializer {

	static public ModelFactory FACTORY = ModelFactory.eINSTANCE;

	private static Logger logger = LoggerFactory.getLogger(BusinessModelNamesInitializer.class);

	private void setUniqueName(ModelObject o) {

		if (o instanceof BusinessModel) {
			setModelUniqueName((BusinessModel) o);
		} else if (o instanceof BusinessTable) {
			setTableUniqueName((BusinessTable) o);
		} else if (o instanceof BusinessView) {
			setViewUniqueName((BusinessView) o);
		} else if (o instanceof SimpleBusinessColumn) {
			setColumnUniqueName((SimpleBusinessColumn) o);
		} else if (o instanceof CalculatedBusinessColumn) {
			setCalculatedColumnUniqueName((CalculatedBusinessColumn) o);
		} else if (o instanceof BusinessIdentifier) {
			setIdentifierUniqueName((BusinessIdentifier) o);
		} else if (o instanceof BusinessRelationship) {
			setRelationshipUniqueName((BusinessRelationship) o);
		} else {

		}
	}

	private void setName(ModelObject o) {

		if (o instanceof BusinessModel) {
			setModelName((BusinessModel) o);
		} else if (o instanceof BusinessTable) {
			setTableName((BusinessTable) o);
		} else if (o instanceof BusinessView) {
			setViewName((BusinessView) o);
		} else if (o instanceof SimpleBusinessColumn) {
			setColumnName((SimpleBusinessColumn) o);
		} else if (o instanceof CalculatedBusinessColumn) {
			setCalculatedColumnName((CalculatedBusinessColumn) o);
		} else if (o instanceof BusinessIdentifier) {
			setIdentifierName((BusinessIdentifier) o);
		} else if (o instanceof BusinessRelationship) {
			setRelationshipName((BusinessRelationship) o);
		} else {

		}
	}

	public void setModelName(BusinessModel o) {

		try {

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setModelUniqueName(BusinessModel o) {

		try {

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// =======================================================
	// TABLE
	// =======================================================

	// name
	public void setTableName(BusinessTable businessTable) {
		Assert.assertNotNull("Input parameter [businessTable] cannot be null", businessTable);
		Assert.assertNotNull("Input parameter [businessTable] is not associated to any physical table", businessTable.getPhysicalTable());
		String physicalTableName = businessTable.getPhysicalTable().getName();
		String baseName = StringUtils.capitalize(physicalTableName.replace("_", " "));
		setTableName(businessTable, baseName);
	}

	public void setTableName(BusinessTable businessTable, String baseName) {
		Assert.assertNotNull("Input parameter [businessTable] cannot be null", businessTable);
		Assert.assertNotNull("Input parameter [baseName] cannot be null", baseName);
		BusinessModel businessModel = businessTable.getModel();
		if (businessModel == null) {
			businessModel = businessTable.getPhysicalTable().getModel().getParentModel().getBusinessModels().get(0);
		}

		String name = baseName;
		while (businessModel.getBusinessTableByName(name).size() > 0) {
			name += " - Copy";
		}
		businessTable.setName(name);
	}

	// unique name
	public void setTableUniqueName(BusinessTable businessTable) {
		Assert.assertNotNull("Input parameter [businessTable] cannot be null", businessTable);
		Assert.assertNotNull("Input parameter [businessTable] is not associated to any physical table", businessTable.getPhysicalTable());

		String physicalTableName = businessTable.getPhysicalTable().getName();
		String baseUniqueName = physicalTableName.replace("_", " ");
		baseUniqueName = baseUniqueName.trim().replace(" ", "_");
		baseUniqueName = baseUniqueName.toLowerCase();
		setTableUniqueName(businessTable, baseUniqueName);

	}

	public void setTableUniqueName(BusinessTable businessTable, String baseUniqueName) {
		Assert.assertNotNull("Input parameter [businessTable] cannot be null", businessTable);
		Assert.assertNotNull("Input parameter [baseName] cannot be null", baseUniqueName);
		BusinessModel businessModel = businessTable.getModel();
		if (businessModel == null) {
			businessModel = businessTable.getPhysicalTable().getModel().getParentModel().getBusinessModels().get(0);
		}

		int index = 1;
		String uniqueName = baseUniqueName;
		while (businessModel.getBusinessTableByUniqueName(uniqueName) != null) {
			uniqueName = baseUniqueName + index++;
		}
		businessTable.setUniqueName(uniqueName);
	}

	// =======================================================
	// VIEW
	// =======================================================

	public void setViewName(BusinessView o) {

	}

	public void setViewUniqueName(BusinessView o) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =======================================================
	// COLUMN
	// =======================================================
	public void setColumnName(SimpleBusinessColumn businessColumn) {
		String baseName = StringUtils.capitalize(businessColumn.getPhysicalColumn().getName().replace("_", " "));
		businessColumn.setName(baseName);
	}

	public void setColumnUniqueName(SimpleBusinessColumn businessColumn) {

		try {
			String baseUniqueName;
			String physicalColumnName = businessColumn.getPhysicalColumn().getName();
			baseUniqueName = physicalColumnName.replace("_", " ");
			baseUniqueName = baseUniqueName.trim().replace(" ", "_");
			baseUniqueName.toLowerCase();
			int index = 1;
			String uniqueName = baseUniqueName;
			while (businessColumn.getTable().getSimpleBusinessColumnByUniqueName(uniqueName) != null) {
				uniqueName = baseUniqueName + index++;
			}
			businessColumn.setUniqueName(uniqueName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// =======================================================
	// CALCULATED COLUMN
	// =======================================================

	public void setCalculatedColumnName(CalculatedBusinessColumn o) {

	}

	public void setCalculatedColumnUniqueName(CalculatedBusinessColumn o) {

		try {

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setIdentifierName(BusinessIdentifier o) {

	}

	public void setIdentifierUniqueName(BusinessIdentifier o) {

	}

	public void setRelationshipName(BusinessRelationship o) {

	}

	public void setRelationshipUniqueName(BusinessRelationship o) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
