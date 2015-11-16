package it.eng.spagobi.images.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.sql.Blob;

public class SbiImages extends SbiHibernateModel {
	private static final long serialVersionUID = -7525846956199531737L;

	private Integer imageId;
	private String name;
	private Blob content;
	private Blob contentIco;

	/**
	 * @return the imageId
	 */
	public Integer getImageId() {
		return imageId;
	}

	/**
	 * @param imageId
	 *            the imageId to set
	 */
	public void setImageId(Integer imageId) {
		this.imageId = imageId;
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
	 * @return the content
	 */
	public Blob getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Blob content) {
		this.content = content;
	}

	/**
	 * @return the contentIco
	 */
	public Blob getContentIco() {
		return contentIco;
	}

	/**
	 * @param contentIco
	 *            the contentIco to set
	 */
	public void setContentIco(Blob contentIco) {
		this.contentIco = contentIco;
	}

}
