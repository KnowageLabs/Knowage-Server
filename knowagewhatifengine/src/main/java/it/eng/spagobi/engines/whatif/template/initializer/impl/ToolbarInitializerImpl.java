/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;

/**
 * @author Dragan Pirkovic
 *
 */
public class ToolbarInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(ToolbarInitializerImpl.class);

	public static final String TAG_TOOLBAR = "TOOLBAR";
	public static final String TAG_VISIBLE = "visible";
	public static final String TAG_CLICKED = "CLICKED";
	public static final String TAG_MENU = "menu";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		List<SourceBeanAttribute> toolbarButtons;
		SourceBeanAttribute aToolbarButton;
		String name;
		String visible;
		String menu;
		String clicked;
		SourceBean value;

		logger.debug("IN. loading the toolbar config");
		SourceBean toolbarSB = (SourceBean) template.getAttribute(TAG_TOOLBAR);
		if (toolbarSB != null) {

			List<String> toolbarVisibleButtons = new ArrayList<String>();
			List<String> toolbarMenuButtons = new ArrayList<String>();
			List<String> toolbarClickedButtons = new ArrayList<String>();

			logger.debug(TAG_TOOLBAR + ": " + toolbarSB);
			toolbarButtons = toolbarSB.getContainedAttributes();
			if (toolbarButtons != null) {
				for (int i = 0; i < toolbarButtons.size(); i++) {
					aToolbarButton = toolbarButtons.get(i);
					name = aToolbarButton.getKey();
					if (aToolbarButton.getValue() != null) {
						value = (SourceBean) aToolbarButton.getValue();
						visible = (String) value.getAttribute(TAG_VISIBLE);
						menu = (String) value.getAttribute(TAG_MENU);
						clicked = (String) value.getAttribute(TAG_CLICKED);
						if (visible != null && visible.equalsIgnoreCase(TRUE)) {
							/*
							 * if (menu != null && menu.equalsIgnoreCase(TRUE)) { toolbarMenuButtons.add(name); } else {
							 */
							toolbarVisibleButtons.add(name);
							// }
							if (clicked != null && clicked.equalsIgnoreCase(TRUE)) {
								toolbarClickedButtons.add(name);
							}
						}
					}
				}

				logger.debug("Updating the toolbar in the template");
				toReturn.setToolbarMenuButtons(toolbarMenuButtons);
				toReturn.setToolbarVisibleButtons(toolbarVisibleButtons);
				toReturn.setToolbarClickedButtons(toolbarClickedButtons);
			}
		} else {
			logger.debug(TAG_TOOLBAR + ": no toolbar buttons defined in the template");
		}
	}

}
