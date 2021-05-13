package it.eng.knowage.knowageapi.resource.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.eng.spagobi.services.validation.AlphanumericNoSpaces;
import it.eng.spagobi.services.validation.CodeConstraint;
import it.eng.spagobi.services.validation.ListStringConstraint;
import it.eng.spagobi.services.validation.UUIDAlphanumericNoSpaces;
import it.eng.spagobi.services.validation.Xss;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WidgetGalleryDTO {

	@UUIDAlphanumericNoSpaces
	private String id;

	private String author;

	@AlphanumericNoSpaces
	@NotNull
	@Xss
	private String name;

	@Xss
	private String description;

	private String type;

	@ListStringConstraint(message = "One or more tag is not valid")
	private List<String> tags = new ArrayList<String>();

	private String image = null;

	private String organization;
	private String sbiversion;

	private String template;

	private Timestamp timestamp;

	private int usageCounter;

	@CodeConstraint
	private Code code = new Code();

	private String outputType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getTags() {
		return tags;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public WidgetGalleryDTO(String id, String author, String name, String type, List<String> tags, String html, String javaScript, String python, String css,
			byte[] image) {
		super();
		this.id = id;
		this.author = author;
		this.name = name;
		this.type = type;
		this.tags = tags;
		this.code.setCss(css);
		this.code.setHtml(html);
		this.code.setJavascript(javaScript);
		this.code.setPython(python);
		this.image = image != null ? new String(image) : null;

	}

	public WidgetGalleryDTO() {
		super();
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getSbiversion() {
		return sbiversion;
	}

	public void setSbiversion(String sbiversion) {
		this.sbiversion = sbiversion;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public int getUsageCounter() {
		return usageCounter;
	}

	public void setUsageCounter(int usageCounter) {
		this.usageCounter = usageCounter;
	}

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

}
