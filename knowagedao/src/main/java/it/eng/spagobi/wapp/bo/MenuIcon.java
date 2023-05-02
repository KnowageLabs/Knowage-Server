package it.eng.spagobi.wapp.bo;

/**
 *
 * @author albnale
 * @since 2020/02
 */

/*
 * {"label":"500px", "className":"fab fa-500px", "unicode":"\\uf26e", "visible":true, "id":0, "category":"brands"}
 */
public class MenuIcon {

	private String label;
	private String className;
	private String unicode;
	private Boolean visible;
	private String id;
	private String category;
	private String src;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUnicode() {
		return unicode;
	}

	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

}
