package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.spago.base.SourceBean;

import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class DateLabelProducer.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DateLabelProducer extends AbstractLabelProducer {

	/** The day format. */
	private String dayFormat = "dd/MM/yyyy";

	/** The hour format. */
	private String hourFormat = "HH:mm";

	/** The text. */
	private String text = "Ultimo aggiornamento del ${day} alle ore ${hour}";

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.renderer.LabelProducer#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean conf) {
		super.init(conf);
		SourceBean formatSB = (SourceBean) conf.getAttribute("FORMAT");
		dayFormat = (String) formatSB.getAttribute("day");
		hourFormat = (String) formatSB.getAttribute("hour");
		SourceBean textSB = (SourceBean) conf.getAttribute("TEXT");
		text = textSB.getCharacters();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.renderer.LabelProducer#getLabel()
	 */
	@Override
	public String getLabel() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat df = null;

		df = new SimpleDateFormat(dayFormat);
		String day = df.format(date);

		df = new SimpleDateFormat(hourFormat);
		String hour = df.format(date);

		String label = text;
		label = label.replaceAll("\\$\\{day\\}", day);
		label = label.replaceAll("\\$\\{hour\\}", hour);

		return label;
	}
}
