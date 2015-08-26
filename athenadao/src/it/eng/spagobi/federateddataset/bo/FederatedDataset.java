package it.eng.spagobi.federateddataset.bo;

import java.io.Serializable;

public class FederatedDataset implements Serializable{
	
	private int id_sbi_federated_data_set;
	
	private String name;
	
	private String label;
	
	private String description;
	
	private String relationships;

	public String getRelationships() {
		return relationships;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	public int getId_sbi_federated_data_set() {
		return id_sbi_federated_data_set;
	}

	public void setId_sbi_federated_data_set(int id_sbi_federated_data_set) {
		this.id_sbi_federated_data_set = id_sbi_federated_data_set;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "FederatedDataset [id_sbi_federated_data_set="
				+ id_sbi_federated_data_set + ", name=" + name + ", label="
				+ label + ", description=" + description + ", relationships="
				+ relationships + "]";
	}


	
	
}
