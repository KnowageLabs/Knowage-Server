package it.eng.knowage.engines.svgviewer.dataset;

import it.eng.knowage.engines.svgviewer.map.renderer.Layer;

import java.util.Map;

/**
 * The Class HierarchyMember.
 *
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
/**
 * @author giachino
 *
 */
public class HierarchyMember {

	private String name;
	private String dsMeasure;
	private String dsConfig;
	private Integer level;

	private DataSetMetaData dsMetaData;
	private Map<String, Layer> layers;

	private boolean isActive;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dsMeasure
	 */
	public String getDsMeasure() {
		return dsMeasure;
	}

	/**
	 * @param dsMeasure
	 *            the dsMeasure to set
	 */
	public void setDsMeasure(String dsMeasure) {
		this.dsMeasure = dsMeasure;
	}

	/**
	 * @return the dsConfig
	 */
	public String getDsConfig() {
		return dsConfig;
	}

	/**
	 * @param dsConfig
	 *            the dsConfig to set
	 */
	public void setDsConfig(String dsConfig) {
		this.dsConfig = dsConfig;
	}

	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @return the dsMetaData
	 */
	public DataSetMetaData getDsMetaData() {
		return dsMetaData;
	}

	/**
	 * @param dsMetaData
	 *            the dsMetaData to set
	 */
	public void setDsMetaData(DataSetMetaData dsMetaData) {
		this.dsMetaData = dsMetaData;
	}

	/**
	 * @return the layers
	 */
	public Map<String, Layer> getLayers() {
		return layers;
	}

	/**
	 * @param layers
	 *            the layers to set
	 */
	public void setLayers(Map<String, Layer> layers) {
		this.layers = layers;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
