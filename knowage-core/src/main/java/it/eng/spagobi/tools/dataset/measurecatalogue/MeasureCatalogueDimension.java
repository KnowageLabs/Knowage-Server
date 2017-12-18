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


package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.metamodel.HierarchyWrapper;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.List;

import org.apache.log4j.Logger;



public class MeasureCatalogueDimension implements IMeasureCatalogueField{
	IFieldMetaData dimensionMetadata;
	HierarchyWrapper hierarchy;
	String hierarchyLevel;
	int hierarchyLevelPosition;
	public static transient Logger logger = Logger.getLogger(MeasureCatalogueDimension.class);

	public MeasureCatalogueDimension(IFieldMetaData dimensionMetadata, MetaModelWrapper metaModel, IDataSet ds){
		this.dimensionMetadata = dimensionMetadata;
		hierarchyLevelPosition = -1;
		
		if(dimensionMetadata.getProperties()!=null){
			String hierarchyName = (String) dimensionMetadata.getProperty(MeasureCatalogueCostants.dimensionHierarchyMetadata);
			if(hierarchyName!=null){
				setHierarchy(hierarchyName, metaModel);
				Assert.assertNotNull(hierarchy, "Can not find the hierachy with name "+hierarchyName+" in the dataset with label "+ds.getLabel());
				hierarchyLevel =  (String) dimensionMetadata.getProperty(MeasureCatalogueCostants.dimensionHierarchyMetadataLevel);
				hierarchyLevelPosition = hierarchy.getLevelPosition(hierarchyLevel);
			}
		}

	}
	
	public String getAlias(){
		if(dimensionMetadata.getAlias()!=null){
			return dimensionMetadata.getAlias();
		}
		return dimensionMetadata.getName();
	}
	
	public void setHierarchy(String hierarchyName, MetaModelWrapper metaModel){
		List<HierarchyWrapper> hierarchies = metaModel.getHierarchies();
		for(int i=0; i<hierarchies.size(); i++){
			if(hierarchies.get(i).getName().equals(hierarchyName)){
				logger.debug("Hierarchy with name "+hierarchyName+" found in the hierarcies model");
				hierarchy = hierarchies.get(i);
				break;
			}
		}
		logger.debug("Hierarchy with name "+hierarchyName+" not found in the hierarcies model");
	}
	
	public boolean hasHierarchy() {
		return hierarchy!=null;
	}

	public HierarchyWrapper getHierarchy() {
		return hierarchy;
	}

	public String getHierarchyLevel() {
		return hierarchyLevel;
	}

	public int getHierarchyLevelPosition() {
		return hierarchyLevelPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hierarchy == null) ? 0 : hierarchy.hashCode());
		result = prime * result
				+ ((hierarchyLevel == null) ? 0 : hierarchyLevel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeasureCatalogueDimension other = (MeasureCatalogueDimension) obj;
		if (hierarchy == null) {
			if (other.hierarchy != null)
				return false;
		} else if (!hierarchy.equals(other.hierarchy))
			return false;
		if (hierarchyLevel == null) {
			if (other.hierarchyLevel != null)
				return false;
		} else if (!hierarchyLevel.equals(other.hierarchyLevel))
			return false;
		return true;
	}
	
	
	

}
