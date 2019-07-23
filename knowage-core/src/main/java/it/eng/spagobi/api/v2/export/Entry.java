package it.eng.spagobi.api.v2.export;

import java.util.Date;

/**
 * Entry of the list of files in resource directory.
 *
 * @author Marco Libanori
 */
public class Entry {

	private String filename;
	private Date startDate;
	private String id;

	public Entry(String filename, Date startDate, String id) {
		super();
		this.filename = filename;
		this.startDate = startDate;
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public String getId() {
		return id;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}