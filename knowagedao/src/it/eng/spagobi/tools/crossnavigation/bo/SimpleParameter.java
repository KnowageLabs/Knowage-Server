package it.eng.spagobi.tools.crossnavigation.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimpleParameter implements Serializable {

	private static final long serialVersionUID = 5806561890901779736L;
	/**
	 * 
	 */
	private Integer id;
	private String name;
	private String type;
	private List<SimpleParameter> links = new ArrayList<>();

	private static final String DEFAULT_TYPE = "input";

	public SimpleParameter() {
	}

	public SimpleParameter(Integer id, String name) {
		this(id, name, DEFAULT_TYPE);
	}

	public SimpleParameter(Integer id, String name, String type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
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
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the links
	 */
	public List<SimpleParameter> getLinks() {
		return links;
	}

	/**
	 * @param links
	 *            the links to set
	 */
	public void setLinks(List<SimpleParameter> links) {
		this.links = links;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object arg0) {
		return arg0 instanceof SimpleParameter && ((SimpleParameter) arg0).getId().equals(getId());
	}
}
