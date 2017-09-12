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

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.metamodel.HierarchyWrapper;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * This class is the Measure for the MeasureCatalogue
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class MeasureCatalogueMeasure implements IMeasureCatalogueField {
	
	public static transient Logger logger = Logger.getLogger(MeasureCatalogueMeasure.class);
	
	private String alias;
	private String label;
	//private Integer id;
	private String columnName;
	private Class dataType;
	private IDataSet dataset;
	private Set<MeasureCatalogueDimension> datasetDimensions;
	private MetaModelWrapper metaModel;
	private IFieldMetaData fieldMetadata;
	private Map<String, Object> measureProperties;

	
	private MeasureCatalogueMeasure(MetaModelWrapper metaModel){
		this.metaModel = metaModel;
		datasetDimensions = new HashSet<MeasureCatalogueDimension>();
		measureProperties = new HashMap<String, Object>();
	}
	
	
	public MeasureCatalogueMeasure( IFieldMetaData fieldMetadata, MetaModelWrapper metaModel, IDataSet ds, Set<MeasureCatalogueDimension> datasetDimension){
		this(metaModel);
		Assert.assertNotNull("The field metadata of the measure is null",fieldMetadata);
		this.fieldMetadata = fieldMetadata;
		this.alias=fieldMetadata.getAlias();
		if(this.alias==null){
			this.alias=fieldMetadata.getName();
		}
		Assert.assertNotNull("Teh alias of the field is null",this.alias);
		this.label = this.alias+ds.getLabel();
		this.dataType = fieldMetadata.getType();
		columnName = this.alias;
		if(datasetDimension!=null){
			logger.debug("The list of dimensions of the dataset is not null..");
			this.dataset = ds;
			this.datasetDimensions = datasetDimension;
		}else{
			logger.debug("The list of dimensions of the dataset is null.. Create a new dimensions set");
			refreshDataSet(ds);
		}
		logger.debug("Measure ["+fieldMetadata.getName()+"] generated for the dataset["+ds.getName()+"]");
	}
	
	
	/**
	 * Refresh the list of dimensions linked to the measure and the dataset
	 * @param ds
	 */
	public void refreshDataSet(IDataSet ds){
		logger.debug("Updating the dimension of dataset "+ds.getName());
		this.dataset = ds;
		Set<MeasureCatalogueDimension> dimensions = new HashSet<MeasureCatalogueDimension>();
		int fields = ds.getMetadata().getFieldCount();
		for(int i=0; i<fields; i++){
			IFieldMetaData aFieldMetadata = ds.getMetadata().getFieldMeta(i);
			if(!isMeasure(aFieldMetadata)){
				logger.debug("Adding the dimension ["+aFieldMetadata.getName()+"] to the dataset +["+ds.getName()+"]");
				dimensions.add(new MeasureCatalogueDimension(aFieldMetadata,metaModel, ds));
			}
		}
		logger.debug("OUT");
		datasetDimensions = dimensions;
	}
	
	/**
	 * Check if this measure is equal to a field of a dataset
	 * @param measure IFieldMetaData
	 * @return
	 */
	public boolean isEqual(IFieldMetaData measure, IDataSet ds) {
		String alias = measure.getAlias();
		if(alias==null){
			alias = measure.getName();
		}
		return ds.equals(dataset) && alias.equals(this.alias);
	}

	
	/**
	 * Utility method that check if a field of a dataset is a measure
	 * @param fieldMetadata
	 * @return
	 */
	public static boolean isMeasure(IFieldMetaData fieldMetadata){
		boolean isMeasure = (fieldMetadata.getFieldType()!=null && fieldMetadata.getFieldType().name().equals(MeasureCatalogueCostants.MEASURE));
		logger.debug("The model field ["+fieldMetadata.getAlias()+"] is a measure? "+isMeasure);
		return isMeasure;
	}
	


	public Object getProperty(String prop){
		Object obj = measureProperties.get(prop);
		if(obj==null && fieldMetadata!=null){
			obj = fieldMetadata.getProperty(prop);
		}
		return obj;
	}

	
	//*************************************************
	//RESOURCES TO IGNORE IN THE SERIALIZATION
	//*************************************************
	@JsonIgnore
	public IFieldMetaData getFieldMetadata() {
		return fieldMetadata;
	}
	

	@JsonIgnore
	public Set<MeasureCatalogueDimension> getDatasetDimension() {
		return datasetDimensions;
	}
	

	/**
	 * Get the hierarchies of the associated dataset
	 * @return
	 */
	@JsonIgnore
	public Set<HierarchyWrapper> getHierarchies(){
		Set<HierarchyWrapper> hierarchies = new HashSet<HierarchyWrapper>();
		for (Iterator<MeasureCatalogueDimension> iterator = datasetDimensions.iterator(); iterator.hasNext();) {
			MeasureCatalogueDimension dimensionWrapper = (MeasureCatalogueDimension) iterator.next();
			hierarchies.add(dimensionWrapper.getHierarchy());
		}
		return hierarchies;
	}
	
	@JsonIgnore
	public IDataSet getDataSet(){
		return dataset;
	}
	
	@JsonIgnore
	public boolean isVisibleToUser(IEngUserProfile profile){
		return DataSetUtilities.isExecutableByUser(dataset, profile);
	}


	
	//*************************************************
	//RESOURCES TO INCLUDE IN THE SERIALIZATION
	//*************************************************

	public String getColumnName() {
		return columnName;
	}

	public String getAlias() {
		return alias;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String getClassType() {
		String type = "";
		if(dataType!=null){
			String  s = dataType.getName();
			if(s!=null){
				int pos = s.lastIndexOf(".");
				if(pos>0){
					type = s.substring(pos+1);
				}
			}
			
			
		}
		return type;
	}
	

	public String getDsLabel() {
		return dataset.getLabel();
	}
	
	public String getDsName() {
		return dataset.getName();
	}
	
	public String getDsCategory() {
		return dataset.getCategoryCd();
	}

	public int getDsId() {
		return dataset.getId();
	}
	
	public String getDsType() {
		return dataset.getDsType();
	}

	//*************************************************
	//CLASS METHODS
	//*************************************************
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
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
		MeasureCatalogueMeasure other = (MeasureCatalogueMeasure) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (dataset == null) {
			if (other.dataset != null)
				return false;
		} else if (!dataset.equals(other.dataset))
			return false;
		return true;
	}

    public String toString(){
		ObjectMapper mapper = new ObjectMapper();    
		try {
			
//			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
//			simpleModule.addSerializer(IDataSet.class, new IDataSetJaksonSerializer());
//			mapper.registerModule(simpleModule);
		
			return  mapper.writeValueAsString(this);
		} catch (Exception e) {
			logger.error("Error serializing the measure catalogue",e);
			throw new SpagoBIRuntimeException("Error serializing the measure catalogue",e);
		}
    }

	

	
}
