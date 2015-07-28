package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlBnessCls extends SbiHibernateModel {

	private static final long serialVersionUID = 5640081056393048360L;


	private Integer bcId;

	private String label;
	

	public SbiGlBnessCls() {
	}


	/**
	 * @param bcId
	 * @param label
	 */
	public SbiGlBnessCls(Integer bcId, String label) {
		super();
		this.bcId = bcId;
		this.label = label;
	}


	/**
	 * @return the bcId
	 */
	public Integer getBcId() {
		return bcId;
	}


	/**
	 * @param bcId the bcId to set
	 */
	public void setBcId(Integer bcId) {
		this.bcId = bcId;
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	

	
}
