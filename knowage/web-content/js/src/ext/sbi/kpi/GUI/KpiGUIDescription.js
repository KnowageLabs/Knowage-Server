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
  

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Monica Franceschini
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.KpiGUIDescription =  function(config) {
		
		var defaultSettings = {};

		if (Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.KpiGUIDescription) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.kpi.KpiGUIDescription);
		}

		var c = Ext.apply(defaultSettings, config || {});

		Ext.apply(this, c);
		
		this.initDescription(c);
   
		Sbi.kpi.KpiGUIDescription.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.kpi.KpiGUIDescription , Ext.form.FormPanel, {
	items: null,
	descrFields: null,
	descrName: null,
	descrDescription: null,
	
	initDescription: function(){
		this.border = false;
		this.descrName = new Ext.form.TextField({fieldLabel: 'Nome', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.descrDescription = new Ext.form.TextArea({fieldLabel: 'Descrizione', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300, height: 120});
/*		this.descrCode = new Ext.form.TextField({fieldLabel: 'Codice', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});*/
		
		this.descrDsLbl = new Ext.form.TextField({fieldLabel: 'Dataset', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.descrTypeCd = new Ext.form.TextField({fieldLabel: 'Codice Tipo', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.measureTypeCd = new Ext.form.TextField({fieldLabel: 'Misura', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.scaleName = new Ext.form.TextField({fieldLabel: 'Scala', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.targetAudience = new Ext.form.TextField({fieldLabel: 'Target Audience', 
			style: 'padding-left:5px; font-style: italic;', readOnly : true, width: 300});
		this.descrFields = new Ext.form.FieldSet({
	        xtype:'fieldset',
	        border: false,
	        defaultType: 'textfield',
	        style: 'margin-top: 10px;',
	        items: [this.descrName, 
	                 //this.descrCode,
		             this.descrDescription,		             
		             //this.descrDsLbl,
		             //this.descrTypeCd,
		             //this.measureTypeCd,
		             //this.scaleName,
		             this.targetAudience]
	    });

		
		this.items =[this.descrFields];
	}
	
	, cleanPanel: function(){

	}
	, update:  function(field){	
		this.descrName.setValue(field.attributes.kpiName);
		this.descrDescription.setValue(field.attributes.kpiDescr);
		//this.descrCode.setValue(field.attributes.kpiCode);
		this.descrDsLbl.setValue(field.attributes.kpiDsLbl);
		this.descrTypeCd.setValue(field.attributes.kpiTypeCd);
		this.measureTypeCd.setValue(field.attributes.measureTypeCd);
		this.targetAudience.setValue(field.attributes.targetAudience);
		this.scaleName.setValue(field.attributes.scaleName);
		
		this.descrName.show();
		this.doLayout();
        this.render();
	}
});