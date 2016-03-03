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
package it.eng.spagobi.kpi.model.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;

import java.util.List;

public interface IModelResourceDAO extends ISpagoBIDao{

	
	/**
	 * Load all couples .
	 * 
	 * @param modelId
	 *            the id of modelInstance to check.

	 * @return list of modelResource Id
	 * 
	 * @throws EMFUserError
	 */
	List loadModelResourceByModelId(Integer modelInstId) throws EMFUserError;

	
	
	public Resource toResource(SbiKpiModelResources re) throws EMFUserError ;
	
	/**
	 * Check if a resources is associated with a modelInstance.
	 * 
	 * @param modelId
	 *            the id of modelInstance to check.
	 * @param resourceId
	 *            the id of resources to check.
	 * @return true if exist an association between the model instance id and
	 *         the resources id, false otherwise.
	 * 
	 * @throws EMFUserError
	 */
	boolean isSelected(Integer modelId, Integer resourceId) throws EMFUserError;

	/**
	 * Remove an association between a model instance and a resource.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * @param resourceId
	 *            the id of the resource.
	 * 
	 *@throws EMFUserError
	 */
	void removeModelResource(Integer modelId, Integer resourceId)
			throws EMFUserError;

	/**
	 * Add an association between a model instance and a resource.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * @param resourceId
	 *            the id of the resource.
	 * 
	 * @throws EMFUserError
	 */
	void addModelResource(Integer modelId, Integer resourceId)
			throws EMFUserError;
	
	/**
	 * Remove all association between a model and same resources.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * 
	 *@throws EMFUserError
	 */
	void removeAllModelResource(Integer modelId)
	throws EMFUserError;

}
