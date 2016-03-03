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

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.ChooseAggregationFunctionWindow = function(config) {

	var defaultSettings = {
		title: LN('sbi.crosstab.crosstabdetailswizard.title')
		, width: 400
  	};
	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.crosstab && Sbi.settings.cockpit.widgets.crosstab.chooseAggregationFunctionWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.crosstab.chooseAggregationFunctionWindow);
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