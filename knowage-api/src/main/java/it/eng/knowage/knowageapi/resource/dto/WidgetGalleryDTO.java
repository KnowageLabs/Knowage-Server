package it.eng.knowage.knowageapi.resource.dto;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class WidgetGalleryDTO {

	private String id;
	private String author;
	private String name;
	private String description;
	private String type;
	private List<String> tags = new ArrayList<String>();
	// TODO correctly image handling
	private byte[] imageBase64Content = null;
	private String image = null;

	private Code code = new Code();

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
			String image) {
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

	public byte[] getImageBase64Content() {
		return imageBase64Content;
	}

	public void setImageBase64Content(byte[] imageBase64Content) {
		this.imageBase64Content = imageBase64Content;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}
