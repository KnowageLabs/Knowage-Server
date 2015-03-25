/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.utils;


import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.data.general.ValueDataset;


/** 
 *  @author Antonella Giachino (antonella.giachino@eng.it)
 */

public class MyDialPlot extends DialPlot {
	/**
    * An optional collection of legend items that can be returned by the
    * getLegendItems() method.
    */
	 private LegendItemCollection legendItems;
	 

	 
	/**
     * Returns the legend items for the plot.  By default, this method returns
     * <code>null</code>.  Subclasses should override to return a
     * {@link LegendItemCollection}.
     *
     * @return The legend items for the dial plot (possibly <code>null</code>).
     */
     public LegendItemCollection getLegendItems() {
    	return this.legendItems;
     }

	/**
	 * @param legendItems the legendItems to set
	 */
	public void setLegendItems(LegendItemCollection legendItems) {
		this.legendItems = legendItems;
	}

}
