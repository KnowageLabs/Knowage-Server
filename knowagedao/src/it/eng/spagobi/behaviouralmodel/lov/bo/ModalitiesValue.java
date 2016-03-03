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
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Defines a Value object for the Predefined LOV
 *
 * @author sulis
 *
 */

/**
 * When serializing the LOV (ModalitiesValue) object ignore fields (properties) with the value of null in order to skip problems (inadequate value for
 * serialization/deserialization). Hence this annotation. "NON_NULL" value serves as indicator that tells Jackson that only fields with non-null value should be
 * included during serialization/deserialization.
 *
 * @author dristovski (danristo, danilo.ristovski@mht.net)
 */
@JsonInclude(Include.NON_NULL)
public class ModalitiesValue implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3600109325988207485L;

	private Integer id;
	@NotNull
	private String name = "";
	private String description = "";
	@NotNull
	private String label = "";
	@NotNull
	private String lovProvider = "";
	@NotNull
	private String iTypeCd = "";
	private String iTypeId = "";

	/**
	 * TODO: [IGNORED] This one is always an empty string !!!
	 */
	private String selectionType = "";

	/**
	 * TODO: [IGNORED] These are not used !!!
	 */
	private SbiDataSet dataset;
	private Integer datasetId;
	private boolean multivalue = true;

	public ModalitiesValue() {
	}

	/**
	 * Gets the description.
	 *
	 * @return Returns the description.
	 */

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id.
	 *
	 * @return Returns the id.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            The id to set.
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the i type cd.
	 *
	 * @return Returns the iTypeCd.
	 */
	public String getITypeCd() {
		return iTypeCd;
	}

	/**
	 * Sets the i type cd.
	 *
	 * @param typeCd
	 *            The iTypeCd to set.
	 */
	public void setITypeCd(String typeCd) {
		iTypeCd = typeCd;
	}

	/**
	 * Gets the lov provider.
	 *
	 * @return Returns the lovProvider.
	 */
	public String getLovProvider() {
		return lovProvider;
	}

	/**
	 * Sets the lov provider.
	 *
	 * @param lovProvider
	 *            The lovProvider to set.
	 */
	public void setLovProvider(String lovProvider) {
		this.lovProvider = lovProvider;
	}

	/**
	 * Gets the name.
	 *
	 * @return Returns the name.
	 */
	// @JsonProperty(value = "lov_name")
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            The name to set.
	 */
	// @JsonProperty(value = "lov_name")
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the i type id.
	 *
	 * @return Returns the iTypeId.
	 */
	public String getITypeId() {
		return iTypeId;
	}

	/**
	 * Sets the i type id.
	 *
	 * @param typeId
	 *            The iTypeId to set.
	 */
	public void setITypeId(String typeId) {
		iTypeId = typeId;
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
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the selection type.
	 *
	 * @return the selection type
	 */
	@JsonIgnore
	public String getSelectionType() {
		return selectionType;
	}

	/**
	 * Sets the selection type.
	 *
	 * @param selectionType
	 *            the new selection type
	 */
	@JsonIgnore
	public void setSelectionType(String selectionType) {
		this.selectionType = selectionType;
	}

	/**
	 * Checks if is multivalue.
	 *
	 * @return true, if is multivalue
	 */
	@JsonIgnore
	private boolean isMultivalue() {
		return multivalue;
	}

	/**
	 * Sets the multivalue.
	 *
	 * @param multivalue
	 *            the new multivalue
	 */
	@JsonIgnore
	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	/**
	 * @return the datasetID
	 */
	@JsonIgnore
	public Integer getDatasetID() {
		this.datasetId = new Integer(dataset.getId().getDsId());
		return datasetId;
	}

	/**
	 * @param datasetId
	 *            the datasetID to set
	 */
	@JsonIgnore
	public void setDatasetID(Integer datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * @return the dataset
	 */
	@JsonIgnore
	public SbiDataSet getDataset() {
		return dataset;
	}

	/**
	 * @param dataset
	 *            the dataset to set
	 */
	@JsonIgnore
	public void setDataset(SbiDataSet dataset) {
		this.dataset = dataset;
	}
}
