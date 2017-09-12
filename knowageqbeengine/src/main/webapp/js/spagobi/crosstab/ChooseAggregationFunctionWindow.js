/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.crosstab");

Sbi.crosstab.ChooseAggregationFunctionWindow = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.crosstab.crosstabdetailswizard.title')
		, width: 400
  	};
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.chooseAggregationFunctionWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.chooseAggregationFunctionWindow);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.init(c);
	
	c = Ext.apply(c, {
		title: LN('sbi.crosstab.chooseaggregationfunctionwindow.title') + ' ' + this.behindMeasure.alias
		, closeAction: 'close'
      	, items: [this.form]
	});
	
	// constructor
    Sbi.crosstab.ChooseAggregationFunctionWindow.superclass.constructor.call(this, c);
	
    this.addEvents('apply');
    
};

Ext.extend(Sbi.crosstab.ChooseAggregationFunctionWindow, Ext.Window, {
    
	behindMeasure: undefined // the row that stands behind the form, must be present on the constructor's config input
	, aggregationFunctionCombo: null
	, form: null
	, aggregationFunctionsStore: new Ext.data.SimpleStore({
		 fields: ['funzione', 'nome', 'descrizione'],
	     data : [
	        ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
	        ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
	        ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
	        ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
	        ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
	        ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
	     ] 
	 })
	
	, init: function(c) {
	
		var initialAggregationFunction = 'SUM';
		if (this.behindMeasure.funct && this.behindMeasure.funct !== 'NONE') {
			initialAggregationFunction = this.behindMeasure.funct;
		}
		
		this.aggregationFunctionCombo = new Ext.form.ComboBox({
            fieldLabel: LN('sbi.qbe.selectgridpanel.headers.function')
            , allowBlank: false
            , editable: false
            , store: this.aggregationFunctionsStore
            , mode: 'local'
	        , displayField: 'nome'
	        , valueField: 'funzione'
		    , triggerAction: 'all'
		    , autocomplete: 'off'
		    , forceSelection: true
		    , value: initialAggregationFunction
        });
	
		this.form = new Ext.form.FormPanel({
			frame: true
			, items: [this.aggregationFunctionCombo]
			, buttons: [{
    			text: LN('sbi.crosstab.chooseaggregationfunctionwindow.buttons.apply')
    		    , handler: function() {
    	    		this.fireEvent('apply', this.getModifiedRow(), this);
                	this.close();
            	}
            	, scope: this
    	    },{
    		    text: LN('sbi.crosstab.chooseaggregationfunctionwindow.buttons.cancel')
    		    , handler: function(){ this.close(); }
            	, scope: this
    		}]
		});
		
	}

	, getModifiedRow: function() {
		this.behindMeasure.funct = this.aggregationFunctionCombo.getValue();
		return this.behindMeasure;
	}

});