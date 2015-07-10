/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.metadata;

import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.services.validation.Alphanumeric;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This is the class used by the DAO to map the table 
 * <code>sbi_meta_data</code>. Given the current implementation
 * of the DAO this is the class used by Hibernate to map the table
 * <code>sbi_meta_data</code>. The following snippet of code, for example, shows
 * how the <code>DataSetDAOImpl</code> load a dataset whose id is equal to datasetId...
 * 
 * <code>hibernateSession.load(SbiDataSet.class, datasetId);</code>
 * 
 * @authors
 * 		Angelo Bernabei (angelo.bernabei@eng.it)
 * 		Andrea Gioia (andrea.gioia@eng.it)
 * 		Antonella Giachino (antonella.giachino@eng.it)
 */
public class SbiDataSet extends SbiHibernateModel {
	
	/**
	 * default version UID
	 */
	private static final long serialVersionUID = 1L;
	
	private SbiDataSetId id;

	@ExtendedAlphanumeric
	@Size(max = 50)
	private String name = null;

	@ExtendedAlphanumeric
	@Size(max = 160)
	private String description = null;

	@NotEmpty
	@Alphanumeric
	@Size(max = 50)
	private String label = null;

	private boolean active = true;

	private SbiDomains category  = null;
	private String parameters=null;
	private String dsMetadata=null;
	private String type = null;
	@JsonRawValue
	private String configuration = null;
	
	private SbiDomains transformer = null;
	private String pivotColumnName=null;
	private String pivotRowName=null;
	private String pivotColumnValue=null;
	private boolean numRows = false;

	private boolean persisted = false;
	private String persistTableName = null;
	
	private String owner=null;
	private boolean publicDS = false;
	
	private String userIn=null;
	private String userUp=null;
	private String userDe=null;
	private String sbiVersionIn=null;
	private String sbiVersionUp=null;
	private String sbiVersionDe=null;
	private String metaVersion=null;

	private Date timeIn = null;
	private Date timeUp = null;
	private Date timeDe = null;
	
	private SbiDomains scope = null;
    	
	/**
	 * default constructor.
	 */
	public SbiDataSet() {
	}

	/**
	 * constructor with id.
	 * 
	 * @param id the id
	 */
	@JsonIgnore
	public SbiDataSet(SbiDataSetId id) {
		this.id = id;
	}
    


	public SbiDomains getScope() {
		return scope;
	}

	public void setScope(SbiDomains scope) {
		this.scope = scope;
	}

	public Integer getScopeId() {
		if (scope != null)
			return scope.getValueId();
		else
			return null;
	}

	public void setScopeId(Integer id) {
		scope = getDomain(id);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
	    return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
	    this.name = name;
	}
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
	    return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
	    this.description = description;
	}
	
	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
	    return label;
	}
	
	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
	    this.label = label;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}	

	public String getUserIn() {
		return userIn;
	}

	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	public String getSbiVersionIn() {
		return sbiVersionIn;
	}

	public void setSbiVersionIn(String sbiVersionIn) {
		this.sbiVersionIn = sbiVersionIn;
	}

	public String getMetaVersion() {
		return metaVersion;
	}

	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}

	public Date getTimeIn() {
		return timeIn;
	}

	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}

	/**
	 * @return the numRows
	 */
	public boolean isNumRows() {
		return numRows;
	}

	/**
	 * @param numRows the numRows to set
	 */
	public void setNumRows(boolean numRows) {
		this.numRows = numRows;
	}

	/**
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	@JsonIgnore
	public String getParameters() {
	    return parameters;
	}
	
	/**
	 * Sets the parameters.
	 * 
	 * @param parameters the new parameters
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	@JsonProperty(value = "parameters")
	public List<DataSetParameterItem> getParametersList() {
		if (parameters != null) {
			try {
				DataSetParametersList list = DataSetParametersList.fromXML(parameters);
				return list.getItems();
			} catch (SourceBeanException e) {
				throw new SpagoBIRuntimeException("Error while getting dataset's parameters", e);
			}
		}
		return null;
	}

	public void setParametersList(List<DataSetParameterItem> parameters) {
		if (parameters != null) {
			DataSetParametersList list = new DataSetParametersList();
			list.setPars(parameters);
			this.parameters = list.toXML();
		}
	}
	
	/**
	 * Gets the pivot column name.
	 * 
	 * @return the pivot column name
	 */
	public String getPivotColumnName() {
		return pivotColumnName;
	}
	
	/**
	 * Sets the pivot column name
	 * 
	 * @param pivotColumnName the new pivot column name
	 */
	public void setPivotColumnName(String pivotColumnName) {
		this.pivotColumnName = pivotColumnName;
	}

	/**
	 * Gets the pivot column value.
	 * 
	 * @return the pivot column value
	 */
	public String getPivotColumnValue() {
		return pivotColumnValue;
	}

	/**
	 * Sets the pivot column value
	 * 
	 * @param pivotColumnValue the new pivot column value
	 */
	public void setPivotColumnValue(String pivotColumnValue) {
		this.pivotColumnValue = pivotColumnValue;
	}

	public String getPivotRowName() {
		return pivotRowName;
	}

	public void setPivotRowName(String pivotRowName) {
		this.pivotRowName = pivotRowName;
	}

	@JsonIgnore
	public SbiDomains getCategory() {
		return category;
	}

	public void setCategory(SbiDomains category) {
		this.category = category;
	}

	public Integer getCategoryId() {
		if (category != null)
			return category.getValueId();
		else
			return null;
	}

	public void setCategoryId(Integer id) {
		category = getDomain(id);
	}

	/**
	 * Gets the transformer.
	 * 
	 * @return the transformer
	 */
	@JsonIgnore
	public SbiDomains getTransformer() {
		return this.transformer;
	}

	/**
	 * Sets the transformer.
	 *
	 * @param transformer
	 *            the new transformer
	 */
	public void setTransformer(SbiDomains transformer) {
		this.transformer = transformer;
	}

	public Integer getTransformerId() {
		if (transformer != null)
			return transformer.getValueId();
		else
			return null;
	}

	public void setTransformerId(Integer id) {
		transformer = getDomain(id);
	}

	/**
	 * Gets the metadata.
	 *
	 * @return metadata
	 */
	@JsonIgnore
	public String getDsMetadata() {
		return dsMetadata;
	}

    /**
     *  the metadata.
     * 
     * @param transformer the new metadata
     */
	public void setDsMetadata(String dsMetadata) {
		this.dsMetadata = dsMetadata;
	}
    	

	public MetaData getMetadata() {
		if (dsMetadata != null) {
			DatasetMetadataParser parser = new DatasetMetadataParser();

			try {
				return (MetaData) parser.xmlToMetadata(dsMetadata);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error while getting dataset's metadata", e);
			}
		}
		return null;
	}

	public void setMetadata(MetaData metadata) {
		if (metadata != null) {
			DatasetMetadataParser parser = new DatasetMetadataParser();
			dsMetadata = parser.metadataToXML(metadata);
		}
	}

	/**
	 * @return the isPersisted
	 */
	public boolean isPersisted() {
		return persisted;
	}

	/**
	 * @param isPersisted the isPersisted to set
	 */
	public void setPersisted(boolean isPersisted) {
		this.persisted = isPersisted;
	}
	
	/**
	 * @param persistTableName the persistTableName to set
	 */
	public void setPersistTableName(String persistTableName) {
		this.persistTableName = persistTableName;
	}

	/**
	 * @return the persistTableName
	 */
	public String getPersistTableName() {
		return persistTableName;
	}

	/**
	 * @return the configuration
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	@JsonDeserialize(using = JsonRawDeserializer.class)
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	/**
	 * Used to deserialize raw json data as is
	 * */
	private static class JsonRawDeserializer extends JsonDeserializer<String> {

		@Override
		public String deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {

			TreeNode tree = parser.getCodec().readTree(parser);
			return tree.toString();
		}
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the userUp
	 */
	public String getUserUp() {
		return userUp;
	}

	/**
	 * @param userUp the userUp to set
	 */
	public void setUserUp(String userUp) {
		this.userUp = userUp;
	}

	/**
	 * @return the userDe
	 */
	public String getUserDe() {
		return userDe;
	}

	/**
	 * @param userDe the userDe to set
	 */
	public void setUserDe(String userDe) {
		this.userDe = userDe;
	}

	/**
	 * @return the sbiVersionUp
	 */
	public String getSbiVersionUp() {
		return sbiVersionUp;
	}

	/**
	 * @param sbiVersionUp the sbiVersionUp to set
	 */
	public void setSbiVersionUp(String sbiVersionUp) {
		this.sbiVersionUp = sbiVersionUp;
	}

	/**
	 * @return the sbiVersionDe
	 */
	public String getSbiVersionDe() {
		return sbiVersionDe;
	}

	/**
	 * @param sbiVersionDe the sbiVersionDe to set
	 */
	public void setSbiVersionDe(String sbiVersionDe) {
		this.sbiVersionDe = sbiVersionDe;
	}

	/**
	 * @return the timeUp
	 */
	public Date getTimeUp() {
		return timeUp;
	}

	/**
	 * @param timeUp the timeUp to set
	 */
	public void setTimeUp(Date timeUp) {
		this.timeUp = timeUp;
	}

	/**
	 * @return the timeDe
	 */
	public Date getTimeDe() {
		return timeDe;
	}

	/**
	 * @param timeDe the timeDe to set
	 */
	public void setTimeDe(Date timeDe) {
		this.timeDe = timeDe;
	}

	
	

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public SbiDataSetId getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param SbiDataSetId the new id
	 */
	public void setId(SbiDataSetId id) {
		this.id = id;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the isPublic
	 */
	public boolean isPublicDS() {
		return publicDS;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setPublicDS(boolean publicDS) {
		this.publicDS = publicDS;
	}

	private SbiDomains getDomain(Integer id) {
		if (id != null) {
			try {
				SbiDomains sbiDomain = new SbiDomains();
				sbiDomain.setValueId(id);

				return sbiDomain;
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Impossible to load domain with id [" + id + "]", e);
			}
		} else
			return null;
	}
}
