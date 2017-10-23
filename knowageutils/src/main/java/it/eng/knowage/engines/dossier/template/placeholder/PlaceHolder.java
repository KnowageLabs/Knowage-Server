package it.eng.knowage.engines.dossier.template.placeholder;

import javax.xml.bind.annotation.XmlAttribute;

public class PlaceHolder {

	String value;

	public String getValue() {
		return value;
	}
	@XmlAttribute( name = "value", required = true )
	public void setValue(String value) {
		this.value = value;
	}
	
}
