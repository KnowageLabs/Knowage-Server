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
package it.eng.knowage.meta.initializer.utils;

import it.eng.knowage.meta.initializer.ModelSingleton;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.validator.ModelExtractor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;

/**
 * This class is used to retrieve objects in the Business Model that are related to object of the Physical Model
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */
public class PhysicalModelReferencesFinder {
	ModelSingleton modelSingleton;
	ECrossReferenceAdapter crossReferenceAdapter;

	public PhysicalModelReferencesFinder() {
		modelSingleton = ModelSingleton.getInstance();
		crossReferenceAdapter = modelSingleton.getCrossReferenceAdapter();

	}

	public Set<ModelObject> getDistinctBusinessObjects(ModelObject physicalObject) {
		Set<ModelObject> businessObjects = new LinkedHashSet<ModelObject>();
		Collection<Setting> settings = crossReferenceAdapter.getInverseReferences(physicalObject);
		for (Setting setting : settings) {
			EObject eobject = setting.getEObject();
			if (eobject instanceof BusinessViewInnerJoinRelationship) {
				BusinessViewInnerJoinRelationship businessViewInnerJoinRelationship = (BusinessViewInnerJoinRelationship) eobject;
				Collection<Setting> bwSettings = crossReferenceAdapter.getInverseReferences(businessViewInnerJoinRelationship);
				for (Setting bwSetting : bwSettings) {
					EObject bwEobject = bwSetting.getEObject();
					if (bwEobject instanceof BusinessView) {
						ModelObject modelObject = (ModelObject) bwEobject;
						Model model = ModelExtractor.getModel(modelObject);
						if (model != null) {
							businessObjects.add(modelObject);
						}

					}

				}

			} else if ((eobject instanceof BusinessRelationship) || (eobject instanceof BusinessTable) || (eobject instanceof BusinessColumn)
					|| (eobject instanceof BusinessColumnSet) || (eobject instanceof BusinessView)) {
				ModelObject modelObject = (ModelObject) eobject;
				Model model = ModelExtractor.getModel(modelObject);
				if (model != null) {
					businessObjects.add(modelObject);
				}
			}
		}
		return businessObjects;
	}

}