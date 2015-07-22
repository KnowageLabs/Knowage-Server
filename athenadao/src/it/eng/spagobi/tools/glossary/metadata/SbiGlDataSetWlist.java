package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

public class SbiGlDataSetWlist extends SbiHibernateModel {

	private static final long serialVersionUID = -7917478737232664376L;

	private SbiGlDataSetWlistId id;
	
	private SbiGlWord word;
	private SbiDataSet dataset;
	private String organization;

	public SbiGlDataSetWlist() {

	}

	/**
	 * @param content
	 * @param word
	 * @param document
	 */
	public SbiGlDataSetWlist( SbiGlWord word,
			SbiDataSet dataset) {
		super();
		this.word = word;
		this.dataset = dataset;
	}






	/**
	 * @return the dataset
	 */
	public SbiDataSet getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(SbiDataSet dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the word
	 */
	public SbiGlWord getWord() {
		return word;
	}



	/**
	 * @param word the word to set
	 */
	public void setWord(SbiGlWord word) {
		this.word = word;
	}



	

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the id
	 */
	public SbiGlDataSetWlistId getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(SbiGlDataSetWlistId id) {
		this.id = id;
	}


	



}
