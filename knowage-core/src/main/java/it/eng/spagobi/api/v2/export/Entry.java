package it.eng.spagobi.api.v2.export;

import java.util.Date;

/**
 * Entry of the list of files in resource directory.
 *
 * @author Marco Libanori
 */
public class Entry {

	private boolean alreadyDownloaded = false;
	private String filename;
	private String id;
	private Date startDate;

	public Entry(String filename, Date startDate, String id, boolean alreadyDownloaded) {
		super();
		this.filename = filename;
		this.startDate = startDate;
		this.id = id;
		this.alreadyDownloaded = alreadyDownloaded;
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

	public boolean isAlreadyDownloaded() {
		return alreadyDownloaded;
	}

	public void setAlreadyDownloaded(boolean alreadyDownloaded) {
		this.alreadyDownloaded = alreadyDownloaded;
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