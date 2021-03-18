package it.eng.knowage.knowageapi.resource.dto;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WidgetGalleryDTO {

	private UUID id;
	private String author;
	private String label;
	private String type;
	private List<String> tags = new ArrayList<String>();
	// TODO correctly image handling
	private byte[] imageBase64Content = null;
	private String image = null;

	private Code code = new Code();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public WidgetGalleryDTO(UUID id, String author, String label, String type, List<String> tags, String html, String javaScript, String python, String css,
			String image) {
		super();
		this.id = id;
		this.author = author;
		this.label = label;
		this.type = type;
		this.tags = tags;
		this.code.setCss(css);
		this.code.setHtml(html);
		this.code.setJavascript(javaScript);
		this.code.setPython(python);
		this.imageBase64Content = image != null ? image.getBytes(Charset.forName("utf8")) : null;
		this.image = image;
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

}
