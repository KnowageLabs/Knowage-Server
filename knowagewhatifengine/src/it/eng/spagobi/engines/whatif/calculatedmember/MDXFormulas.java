package it.eng.spagobi.engines.whatif.calculatedmember;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "formulas")
public class MDXFormulas {

	List<MDXFormula> formulas = new ArrayList<MDXFormula>();

	@XmlElement(name = "formula")
	public List<MDXFormula> getFormulas() {
		return formulas;
	}

	@Override
	public String toString() {
		return "Hello from formulas";
	}

	public MDXFormulas() {

	}
}
