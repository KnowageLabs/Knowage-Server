package it.eng.knowage.engines.dossier.template.ppt;

import javax.xml.bind.annotation.XmlAttribute;

public class PptTemplate {
	String name;

	public String getName() {
		return name;
	}
	 @XmlAttribute( name = "name", required = true )
	public void setName(String name) {
		this.name = name;
	}
}
