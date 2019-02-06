package it.eng.spagobi.tools.news.bo;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class BasicNews implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static protected Logger logger = Logger.getLogger(BasicNews.class);

	private Integer id;
	private String title;
	private String description;

	public BasicNews() {

	}

	public BasicNews(Integer id, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
