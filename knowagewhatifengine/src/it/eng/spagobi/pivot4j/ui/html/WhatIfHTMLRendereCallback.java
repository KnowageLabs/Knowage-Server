package it.eng.spagobi.pivot4j.ui.html;

import static org.pivot4j.ui.table.TablePropertyCategories.HEADER;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.olap4j.Axis;
import org.pivot4j.ui.html.HtmlRenderCallback;
import org.pivot4j.ui.table.TableRenderContext;
import org.pivot4j.util.CssWriter;

public class WhatIfHTMLRendereCallback extends HtmlRenderCallback {

	private boolean showProperties = false;

	public WhatIfHTMLRendereCallback(Writer writer) {
		super(writer);

	}

	@Override
	public void startCell(TableRenderContext context) {
		boolean header;

		switch (context.getCellType()) {
		case HEADER:
		case "Title":
		case "None":
			header = true;
			break;
		default:
			header = false;
			break;
		}

		String name = header ? "th" : "td";

		startElement(name, getCellAttributes(context));

		/*
		 * if (getCommands() != null && !commands.isEmpty()) { start
		 * startCommand(context, commands); }
		 */

	}

	@Override
	protected Map<String, String> getCellAttributes(TableRenderContext context) {

		String styleClass = null;

		StringWriter writer = new StringWriter();
		CssWriter cssWriter = new CssWriter(writer);

		switch (context.getCellType()) {

		case HEADER:
			if (context.getAxis() == Axis.COLUMNS) {
				styleClass = getColumnHeaderStyleClass();
			} else {
				styleClass = getRowHeaderStyleClass();
			}

			// if its a property cell no span needed
			if (getRowHeaderLevelPadding() > 0) {

			}

		}
		return super.getCellAttributes(context);
	}
	/*
	 * private boolean isProperyCell(RenderContext context) { if (showProperties
	 * && this.getPropertyCollector() != null && context.getLevel() != null &&
	 * isEmptyNonProperyCell(context)) { List<org.olap4j.metadata.Property>
	 * propertieds =
	 * this.getPropertyCollector().getProperties(context.getLevel()); return
	 * (propertieds != null && propertieds.size() > 0);// check if // contains
	 * // properties..
	 * 
	 * } return false; }
	 */
}
