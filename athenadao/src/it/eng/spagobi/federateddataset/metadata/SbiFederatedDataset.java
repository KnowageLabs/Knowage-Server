package it.eng.spagobi.federateddataset.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiFederatedDataset extends SbiHibernateModel {
	
	//fields
	private int id_sbi_federated_data_set;
	private String label;
	private String name;
	private String description;
	private String relationships;
	
	//constructors
	public SbiFederatedDataset(int id_sbi_federated_data_set, String label,
			String name, String description, String relationships) {
		
		this.id_sbi_federated_data_set = id_sbi_federated_data_set;
		this.label = label;
		this.name = name;
		this.description = description;
		this.relationships = relationships;
	}

	public String getRelationships() {
		return relationships;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	public SbiFederatedDataset(int id_sbi_federated_data_set) {
		
		this.id_sbi_federated_data_set = id_sbi_federated_data_set;
	}

	public SbiFederatedDataset() {
		
	}

	public int getId_sbi_federated_data_set() {
		return id_sbi_federated_data_set;
	}

	public void setId_sbi_federated_data_set(int id_sbi_federated_data_set) {
		this.id_sbi_federated_data_set = id_sbi_federated_data_set;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	
}
