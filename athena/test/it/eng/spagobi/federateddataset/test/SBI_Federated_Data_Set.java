package it.eng.spagobi.federateddataset.test;
import javax.persistence.*;

@Entity
@Table(name= "SBI_Federated_Data_Set")
public class SBI_Federated_Data_Set {
	@Id @GeneratedValue
	@Column(name = "id_sbi_federated_data_set")
	private int id_sbi_federated_data_set;
	
	@Column(name= "label")
	private String label;
	
	@Column(name= "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	public SBI_Federated_Data_Set(){}

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
