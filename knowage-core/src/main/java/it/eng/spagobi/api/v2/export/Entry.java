package it.eng.spagobi.api.v2.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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

	public Entry(File file) throws IOException {
		this.filename = file.getName();
		this.id = file.toPath().getParent().getFileName().toString();
		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		this.startDate = new Date(attr.creationTime().toMillis());
	}

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