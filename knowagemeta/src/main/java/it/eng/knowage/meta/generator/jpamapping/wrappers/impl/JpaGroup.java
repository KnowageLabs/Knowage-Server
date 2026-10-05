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
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import java.util.ArrayList;
import java.util.List;

import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessDomain;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.spagobi.utilities.assertion.Assert;

public class JpaGroup {

	private final BusinessDomain businessDomain;

	public JpaGroup(BusinessDomain businessDomain) {
		Assert.assertNotNull(businessDomain, "Parameter [businessDomain] cannot be null");
		this.businessDomain = businessDomain;
	}

	public String getModelName() {
		if (businessDomain.getModel() == null) {
			return null;
		}
		if (businessDomain.getModel().getParentModel() != null) {
			return businessDomain.getModel().getParentModel().getName();
		}
		return businessDomain.getModel().getName();
	}

	public String getName() {
		return businessDomain.getName();
	}

	public String getUniqueName() {
		return businessDomain.getUniqueName();
	}

	public String getDescription() {
		return businessDomain.getDescription();
	}

	public List<String> getEntityTypes() {
		List<String> entityTypes = new ArrayList<>();

		for (BusinessColumnSet businessColumnSet : businessDomain.getTables()) {
			String entityType = null;
			if (businessColumnSet instanceof BusinessTable) {
				entityType = new JpaTable((BusinessTable) businessColumnSet).getQualifiedClassName();
			} else if (businessColumnSet instanceof BusinessView) {
				entityType = new JpaView((BusinessView) businessColumnSet).getQualifiedClassName();
			}

			if (entityType != null && !entityTypes.contains(entityType)) {
				entityTypes.add(entityType);
			}
		}

		return entityTypes;
	}
}
