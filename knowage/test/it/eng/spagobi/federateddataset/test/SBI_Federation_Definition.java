package it.eng.spagobi.federateddataset.test;
import javax.persistence.*;

@Entity
@Table(name= "SBI_FEDERATION_DEFINITION")
public class SBI_Federation_Definition {
	@Id @GeneratedValue
	@Column(name = "federation_id")
	private int federation_id;
	
	@Column(name= "label")
	private String label;
	
	@Column(name= "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	public SBI_Federation_Definition(){}

	public int getFederation_id() {
		return federation_id;
	}

	public void setFederation_id(int federation_id) {
		this.federation_id = federation_id;
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
