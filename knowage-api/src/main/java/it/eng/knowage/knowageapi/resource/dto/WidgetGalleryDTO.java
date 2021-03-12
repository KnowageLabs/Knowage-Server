package it.eng.knowage.knowageapi.resource.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WidgetGalleryDTO {

	private UUID id;
	private String author;
	private String label;
	private String type;
	private List<String> tags = new ArrayList<String>();

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

	public WidgetGalleryDTO(UUID id, String author, String label, String type, List<String> tags) {
		super();
		this.id = id;
		this.author = author;
		this.label = label;
		this.type = type;
		this.tags = tags;
	}

	public WidgetGalleryDTO() {
		super();
	}

}
