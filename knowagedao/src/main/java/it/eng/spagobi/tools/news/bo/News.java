package it.eng.spagobi.tools.news.bo;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.tools.news.metadata.NewsPriority;

public class News implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static protected Logger logger = Logger.getLogger(News.class);

	private Integer id;
	private String title;
	private String description;
	private String status;
	private Integer type;
	private Date expirationDate;
	private String html;
	private Boolean active;
	private Role[] roles = null;
	private NewsPriority priority;

	public News() {

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Role[] getRoles() {
		return roles;
	}

	public void setRoles(Role[] roles) {
		this.roles = roles;
	}

	public NewsPriority getPriority() {
		return priority;
	}

	public void setPriority(NewsPriority priority) {
		this.priority = priority;
	}

}
