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


Ext.ns("Sbi.cockpit.widgets.text");

Sbi.cockpit.widgets.text.TextWidgetDesigner = function(config) {

	var defaultSettings = {
			name: 'textWidgetDesigner',
			title: LN('sbi.cockpit.widgets.text.textWidgetDesigner.title'),
	};


	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.text && Sbi.settings.cockpit.widgets.text.textWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.text.textWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.initTextPanel();

	c = {
			layout: 'fit',
			height: 350,
			items: [this.textPanel]
	};

	Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.constructor.call(this, c);

	this.on(
			'beforerender' ,
			function (thePanel, attribute) {
				var state = {};
				state.textValue = thePanel.textValue;
				state.wtype = 'text';
				state.tableBgColor = thePanel.tableBgColor;
				this.setDesignerState(state);
			},
			this
	);

};

Ext.extend(Sbi.cockpit.widgets.text.TextWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	textField: null
	, textPanel: null

	, getDesignerState: function(running) {
		Sbi.trace("[TextWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Text Designer';
		state.wtype = 'text';

		state.textValue = this.textField.getValue();
		if(this.tableBgColorPicker !== null) {	
			state.tableBgColor = this.tableBgColorPicker.getValue();
		}
		Sbi.trace("[TextWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

, setDesignerState: function(state) {
	Sbi.trace("[TextWidgetDesigner.setDesignerState]: IN");
	Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.setDesignerState(this, state);
	if(state.textValue) this.textField.setValue(state.textValue);
	if (state.tableBgColor) this.tableBgColorPicker.setValue(state.tableBgColor);
	Sbi.trace("[TextWidgetDesigner.setDesignerState]: OUT");
}

, validate: function(validFields){
	return Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.validate(this, validFields);
}

, initTextPanel: function (){

	var LABEL_WIDTHS = 90;
	var WIDTHS = 180;
	this.textField = Ext.create('Ext.form.HtmlEditor', {
		width: 525,
		height: 100,
		fieldLabel: LN('sbi.cockpit.widgets.text.textWidgetDesigner.text'),
		name: 'textField',
		enableLinks: false,
		enableSourceEdit: false,
		enableLists: false
	});

	// state.tableBgColor
	this.tableBgColorPicker = Ext.create('Ext.ux.FontColorField', { 
		msgTarget: 		'qtip', 
		fallback: 		true,
		fieldLabel: 	LN('sbi.cockpit.designer.fontConf.bgColor'),
//		afterLabelTextTpl : '<span class="help" data-qtip="'
//		+ LN('sbi.cockpit.designer.fontConf.fontColor.info')
//		+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		name: 			'tableBgColorPicker',
		allowBlank: 	true,
		labelWidth:		LABEL_WIDTHS,
		width:			WIDTHS,
	});
	this.textPanel = Ext.create('Ext.form.Panel', {
		border: false,
		fieldDefaults: {
			labelWidth: 55
		},
		/*url: 'save-form.php',*/
		bodyPadding: 5,

		items: [this.textField, this.tableBgColorPicker]
	});
}

});
