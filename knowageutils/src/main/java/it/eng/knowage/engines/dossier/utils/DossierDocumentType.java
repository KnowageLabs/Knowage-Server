package it.eng.knowage.engines.dossier.utils;

public enum DossierDocumentType {
	PPTV2("ppt", "PowerPoint"), PPTXV2("pptx", "PowerPoint 2007"), PPT("ppt", "PowerPoint"), PPTX("pptx", "PowerPoint 2007"), DOC("doc", "Word"),
	DOCX("docx", "Word 2007");

	private String type;
	private String name;

	private DossierDocumentType(String type, String name) {
		this.setType(type);
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
