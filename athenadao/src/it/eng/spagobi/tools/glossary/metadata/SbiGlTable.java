package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlTable extends SbiHibernateModel {

	private static final long serialVersionUID = 5640081056393048360L;


	private Integer tableId;

	private String label;
	

	public SbiGlTable() {
	}


	/**
	 * @param tableId
	 * @param label
	 */
	public SbiGlTable(Integer tableId, String label) {
		super();
		this.tableId = tableId;
		this.label = label;
	}


	


	/**
	 * @return the tableId
	 */
	public Integer getTableId() {
		return tableId;
	}


	/**
	 * @param tableId the tableId to set
	 */
	public void setTableId(Integer tableId) {
		this.tableId = tableId;
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
