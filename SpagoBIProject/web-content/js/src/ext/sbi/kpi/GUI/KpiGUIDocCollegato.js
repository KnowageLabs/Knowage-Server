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

Sbi.kpi.KpiGUIDocCollegato =  function(config) {
		
		var defaultSettings = {};
		var c = Ext.apply(defaultSettings, config || {});

		Ext.apply(this, c);

		
		this.initFrame();
		
		this.initWindow();
		
		var c = {
				layout: 'fit',
				border: false,
				items: [this.perWin],
				scope: this.scope
		};
   
		Sbi.kpi.KpiGUIDocCollegato.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.kpi.KpiGUIDocCollegato , Ext.Panel, {
	doclabel: null
	, kpiValue: null
	, executionInstance: null
	, miframe : null
    , perWin: null
    , noDocText: null

	, initWindow: function(conf){
	
		this.perWin = new Ext.Panel({
	        scope		: this,
	        autoScroll: true,
	        border: false,
	    	autoHeight: true,
	        items       : [this.miframe]
		});

	}
	, initFrame: function(conf){
		
		this.miframe = new Ext.ux.ManagedIFramePanel ({
	        border: false,
	        bodyBorder: false
	        , autoScroll: true
	        , loadMask: {msg: 'Loading...'}
			, minWidth: 475
			, height: 400
	        , defaultSrc: 'about:blank'
	        , scope:this

	    });

	}

	, getDocViewUrl: function(field) {
		var url = null;

		if(field.attributes.documentLabel && field.attributes.documentExecUrl){
			url = field.attributes.documentExecUrl;
		}

		return url;
	}

	, update:  function(field){	
		if(this.noDocText != null){
			this.noDocText.destroy();
		}else{
			this.miframe.hide();
			this.perWin.doLayout();
		}

		var url = this.getDocViewUrl(field);
		if(url != null){
			
			this.miframe.setSrc(url);
			this.miframe.update();
			this.miframe.show();
			this.miframe.doLayout();
			this.perWin.doLayout();
			
		}
		else{
			this.miframe.hide();
			this.noDocText =new Ext.form.DisplayField({
				value: 'Nessun documento collegato', 
				style: 'padding-left:5px; font-style: italic;'}); 
			this.perWin.add(this.noDocText);
			this.perWin.doLayout();
		}
	}

});