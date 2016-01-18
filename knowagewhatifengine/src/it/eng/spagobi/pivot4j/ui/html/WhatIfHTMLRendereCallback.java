package it.eng.spagobi.pivot4j.ui.html;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.olap4j.Axis;
import org.pivot4j.ui.command.DrillDownCommand;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.html.HtmlRenderCallback;
import org.pivot4j.ui.table.TableRenderContext;

public class WhatIfHTMLRendereCallback extends HtmlRenderCallback {

	public WhatIfHTMLRendereCallback(Writer writer) {
		super(writer);

	}

	@Override
	public void renderCommands(TableRenderContext context, List<UICommand<?>> commands) {
		Map<String, String> attributes = new TreeMap<String, String>();

		if (commands != null && !commands.isEmpty()) {
			for (UICommand<?> command : commands) {
				String cmd = command.getName();
				String drillMode = command.getMode(context);

				int colIdx = context.getColumnIndex();
				int rowIdx = context.getRowIndex();

				int axis = 0;
				if (context.getAxis() != null) {
					axis = context.getAxis().axisOrdinal();
				}
				int memb = 0;
				if (context.getPosition() != null) {
					memb = context.getAxis().axisOrdinal();
				}
				int pos = 0;
				if (context.getAxis() == Axis.COLUMNS) {
					pos = rowIdx;
				} else {
					pos = colIdx;
				}

				String uniqueName = context.getMember().getUniqueName();
				String positionUniqueName = context.getPosition().getMembers().toString();

				if ((cmd.equalsIgnoreCase("collapsePosition") || cmd.equalsIgnoreCase("drillUp") || cmd.equalsIgnoreCase("collapseMember"))
						&& !drillMode.equals(DrillDownCommand.MODE_REPLACE)) {
					attributes.put("src", "../img/minus.gif");
					attributes.put("onClick", "javascript:Sbi.olap.eventManager.drillUp(" + axis + " , " + pos + " , " + memb + ",'" + uniqueName + "','"
							+ positionUniqueName + " ')");
					startElement("img", attributes);
					endElement("img");

				} else if ((cmd.equalsIgnoreCase("expandPosition") || cmd.equalsIgnoreCase("drillDown") || cmd.equalsIgnoreCase("expandMember"))) {
					attributes.put("src", "../img/plus.gif");
					attributes.put("onClick", "javascript:Sbi.olap.eventManager.drillDown(" + axis + " , " + pos + " , " + memb + ",'" + uniqueName + "','"
							+ positionUniqueName + "' )");
					startElement("img", attributes);
					endElement("img");
				}
			}
		}
	}
}
