package it.eng.spagobi.kpi.bo;

import java.util.Date;
import java.util.List;

public class Scorecard {
	private Integer id;
	private String name;
	private Date creationDate;
	private String author;

	private List<ScorecardPerspective> perspectives;

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
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the perspectives
	 */
	public List<ScorecardPerspective> getPerspectives() {
		return perspectives;
	}

	/**
	 * @param perspectives
	 *            the perspectives to set
	 */
	public void setPerspectives(List<ScorecardPerspective> perspectives) {
		this.perspectives = perspectives;
	}

}
