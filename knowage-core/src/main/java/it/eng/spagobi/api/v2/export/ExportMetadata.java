package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Contains metadata for exported files.
 *
 * @author Marco Libanori
 */
public class ExportMetadata {

	public static ExportMetadata readFromJsonFile(final Path metadata) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(metadata.toFile(), ExportMetadata.class);
	}

	public static void writeToJsonFile(final ExportMetadata instance, final Path metadata) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(metadata.toFile(), instance);
	}

	private String dataSetName;
	private String fileName;
	private UUID id;
	private String mimeType;
	private Date startDate;

	public String getDataSetName() {
		return dataSetName;
	}

	public String getFileName() {
		return fileName;
	}

	public UUID getId() {
		return id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
