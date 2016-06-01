package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.component.AbstractSvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;
import it.eng.knowage.engines.svgviewer.map.renderer.configurator.AbstractMapRendererConfigurator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractMapRenderer.
 *
 * @author Andrea Gioia
 */
public class AbstractMapRenderer extends AbstractSvgViewerEngineComponent implements IMapRenderer {

	/** The selected hierarchy name. */
	private String selectedMeasureName;

	/** The measures. */
	private Map measures;

	/** The layers. */
	private Map layers;

	private GuiSettings guiSettings;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractMapRenderer.class);

	/**
	 * Instantiates a new abstract map renderer.
	 */
	public AbstractMapRenderer() {
		super();
		measures = new HashMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.AbstractGeoEngineComponent#init(java.lang.Object)
	 */
	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		AbstractMapRendererConfigurator.configure(this, getConf());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#renderMap(it.eng.spagobi.engines.geo.map.provider.IMapProvider,
	 * it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider, java.lang.String)
	 */
	@Override
	public File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider, String outputType) throws SvgViewerEngineException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#renderMap(it.eng.spagobi.engines.geo.map.provider.IMapProvider,
	 * it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider)
	 */
	@Override
	public File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider) throws SvgViewerEngineException {
		return null;
	}

	/**
	 * Gets the measure.
	 *
	 * @param measureName
	 *            the measure name
	 *
	 * @return the measure
	 */
	public Measure getMeasure(String measureName) {
		Measure measure = (Measure) measures.get(measureName.toUpperCase());
		return measure;
	}

	/**
	 * Gets the tresholds array.
	 *
	 * @param measureName
	 *            the measure name
	 *
	 * @return the tresholds array
	 */
	public String[] getTresholdsArray(String measureName) {
		Measure measure = getMeasure(measureName);
		if (measure != null) {
			Properties params = measure.getTresholdCalculatorParameters();
			if (params == null)
				return null;
			String pValue = params.getProperty("range");
			String[] trasholds = pValue.split(",");
			return trasholds;
		}

		return null;
	}

	/**
	 * Gets the colours array.
	 *
	 * @param measureName
	 *            the measure name
	 *
	 * @return the colours array
	 */
	public String[] getColoursArray(String measureName) {
		Measure measure = getMeasure(measureName);
		if (measure != null) {
			Properties params = measure.getColurCalculatorParameters();
			if (params == null)
				return null;
			String pValue = params.getProperty("range");
			if (pValue == null)
				return new String[0];
			String[] colours = pValue.split(",");
			return colours;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#getLayer(java.lang.String)
	 */
	@Override
	public Layer getLayer(String layerName) {
		return (Layer) layers.get(layerName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#addLayer(it.eng.spagobi.engines.geo.map.renderer.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		layers.put(layer.getName(), layer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#getLayerNames()
	 */
	@Override
	public String[] getLayerNames() {
		if (layers == null)
			return null;
		return (String[]) layers.keySet().toArray(new String[0]);
	}

	/**
	 * Sets the measures.
	 *
	 * @param measures
	 *            the new measures
	 */
	public void setMeasures(Map measures) {
		this.measures = measures;
	}

	/**
	 * Sets the layers.
	 *
	 * @param layers
	 *            the new layers
	 */
	public void setLayers(Map layers) {
		this.layers = layers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.IMapRenderer#clearLayers()
	 */
	@Override
	public void clearLayers() {
		layers.clear();
	}

	public GuiSettings getGuiSettings() {
		return guiSettings;
	}

	public void setGuiSettings(GuiSettings guiSettings) {
		this.guiSettings = guiSettings;
	}

	public String getSelectedMeasureName() {
		return selectedMeasureName;
	}

	@Override
	public void setSelectedMeasureName(String selectedMeasureName) {
		this.selectedMeasureName = selectedMeasureName;
	}

}
