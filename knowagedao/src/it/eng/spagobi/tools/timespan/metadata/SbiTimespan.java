package it.eng.spagobi.tools.timespan.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiTimespan extends SbiHibernateModel {

	private static final long serialVersionUID = 7345354715151435192L;


	private Integer id;

	private String name;
	private String type;
	private String category;
	private Boolean staticFilter = false;
	private String definition;


	public SbiTimespan(){
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the staticFilter
	 */
	public Boolean getStaticFilter() {
		return staticFilter;
	}

	/**
	 * @param staticFilter the staticFilter to set
	 */
	public void setStaticFilter(Boolean staticFilter) {
		this.staticFilter = staticFilter;
	}

	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}
}
