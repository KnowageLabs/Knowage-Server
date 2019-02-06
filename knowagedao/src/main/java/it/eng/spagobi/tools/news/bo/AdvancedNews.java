package it.eng.spagobi.tools.news.bo;

import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

public class AdvancedNews extends BasicNews {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static protected Logger logger = Logger.getLogger(AdvancedNews.class);

	private Integer type;
	private Date expirationDate;
	private String html;
	private Boolean active;
	private Set roles = null;

	public AdvancedNews() {

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

	public Set getRoles() {
		return roles;
	}

	public void setRoles(Set roles) {
		this.roles = roles;
	}

}
