/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

Ext.ns("Sbi.cockpit.widgets.chart");

Sbi.cockpit.widgets.chart.SeriesGroupingPanel = function(config) {

	var defaultSettings = {
		title: LN('sbi.cockpit.widgets.seriesgroupingpanel.title')
		, frame: true
		, emptyMsg: LN('sbi.cockpit.widgets.seriesgroupingpanel.emptymsg')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.chart && Sbi.settings.cockpit.widgets.chart.seriesGroupingPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.chart.seriesGroupingPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	// constructor
	Sbi.cockpit.widgets.chart.SeriesGroupingPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.cockpit.widgets.chart.SeriesGroupingPanel, Sbi.cockpit.widgets.chart.ChartCategoryPanel, {

	getSeriesGroupingAttribute : function () {
		return this.getCategory();
	}

	,
	setSeriesGroupingAttribute : function (attribute) {
		return this.setCategory(attribute);
	}

});