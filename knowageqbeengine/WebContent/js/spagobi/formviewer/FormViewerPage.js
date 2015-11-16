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

Ext.ns("Sbi.formviewer");

Sbi.formviewer.FormViewerPage = function(template, config, formValues) {
	
	var defaultSettings = {
		// set default values here
		//title: LN('sbi.formviewer.formviewerpage.title')
		layout: 'fit'
		, autoScroll: true
		, border : false
		//, bodyStyle: 'padding:30px'
		, showSaveFormButton : true
		, showWorksheetButton : true
		, title: LN('sbi.formviewer.formviewerpage.filters.title')
	};
	
	this.services = this.services || new Array();	
	this.services['getMeta'] = this.services['getMeta'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ANALYSIS_META_ACTION'
		, baseParams: params
	});
	
	if (Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.formViewerPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.formViewerPage);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	// add events
    this.addEvents('submit', 'crosstabrequired');
	
	this.init(template);
	
	this.initToolbar(c);
	
	Ext.apply(c, {
		tbar: this.toolbar
  		, items: this.items
	});
	
	// constructor
    Sbi.formviewer.FormViewerPage.superclass.constructor.call(this, c);

    if(formValues!=null){
    	this.on('render',function(){this.setFormState(formValues)},this);
    }
    
};

/**
 * @class Sbi.formviewer.FormViewerPage
 * @extends Ext.Panel
 * 
 * FormViewerPage
 */
Ext.extend(Sbi.formviewer.FormViewerPage, Ext.Panel, {
    
    services: null
    , staticClosedFiltersPanel: null
    , staticOpenFiltersPanel: null
    , dynamicFiltersPanel: null
    , groupingVariablesPanel: null
    , saveFormStateValuesWindow: null
    , filters: null
    // private methods
    , init: function(template) {

		this.items = [];
		if (template.staticClosedFilters !== undefined && template.staticClosedFilters !== null && template.staticClosedFilters.length > 0) {
			this.staticClosedFiltersPanel = new Sbi.formviewer.StaticClosedFiltersPanel(template.staticClosedFilters); 
			this.items.push(this.staticClosedFiltersPanel);
		}
		if (template.staticOpenFilters !== undefined && template.staticOpenFilters !== null && template.staticOpenFilters.length > 0) {
			this.staticOpenFiltersPanel = new Sbi.formviewer.StaticOpenFiltersPanel(template.staticOpenFilters); 
			this.items.push(this.staticOpenFiltersPanel);
		}
		if (template.dynamicFilters !== undefined && template.dynamicFilters !== null && template.dynamicFilters.length > 0) {
			this.dynamicFiltersPanel = new Sbi.formviewer.DynamicFiltersPanel(template.dynamicFilters); 
			this.items.push(this.dynamicFiltersPanel);
		}
		if (template.groupingVariables !== undefined && template.groupingVariables !== null && template.groupingVariables.length > 0) {
			this.groupingVariablesPanel = new Sbi.formviewer.GroupingVariablesPanel(template.groupingVariables); 
			this.items.push(this.groupingVariablesPanel);
		}
		
		// work-around for layout management on resize event, since components are not automatically resized
	    if (this.staticClosedFiltersPanel != null) {
			this.staticClosedFiltersPanel.on('resize', function (component, adjWidth, adjHeight, rawWidth, rawHeight) {
		    	if (this.staticOpenFiltersPanel != null) {
		    		this.staticOpenFiltersPanel.doLayout();
		    	}
		    	if (this.dynamicFiltersPanel != null) {
		    		this.dynamicFiltersPanel.doLayout();
		    	}
		    	if (this.groupingVariablesPanel != null) {
		    		this.groupingVariablesPanel.doLayout();
		    	}
		    }, this);
	    }
		
	}

	, initToolbar : function (c) {
		var toolbarItems = ['->'];
		if (c.showSaveFormButton) {
			toolbarItems.push({
		    	text: LN('sbi.formviewer.formviewerpage.save'),
		    	tooltip: LN('sbi.formviewer.formviewerpage.save.tooltip'),
				handler: function() {
		    		this.validateForm(function() {
		    			this.showSaveWindow();
		    		}, this);
		    	},
				scope: this
		    });
			toolbarItems.push('-');
		}
		
		toolbarItems.push({
			text: LN('sbi.formviewer.formviewerpage.execute'),
			tooltip: LN('sbi.formviewer.formviewerpage.execute.tooltip'),
			handler: function() {
	    		this.validateForm(function() {
	    			var state = this.getFormState();
	    			this.fireEvent('submit', state);
	    		}, this);
	    	},
			scope: this
	    });
		
		if (c.showWorksheetButton) {
			toolbarItems.push('-');
			toolbarItems.push({
				text: LN('sbi.formviewer.formviewerpage.designworksheet'),
				tooltip: LN('sbi.formviewer.formviewerpage.designworksheet.tooltip'),
				handler: function() {
		    		this.validateForm(function() {
		    			var state = this.getFormState();
		    			this.fireEvent('crosstabrequired', state);
		    		}, this);
		    	},
				scope: this
		    });
		}
		
		this.toolbar = new Ext.Toolbar({
			items: toolbarItems
		});
	}
    
	, validateForm: function(successHandler, obj) {
		var errors = new Array();
		if (this.staticOpenFiltersPanel !== null) {
			var openFiltersErrors = this.staticOpenFiltersPanel.getErrors();
			errors = errors.concat(openFiltersErrors);
		}
		if (this.dynamicFiltersPanel !== null) {
			var dynamicFiltersErrors = this.dynamicFiltersPanel.getErrors();
			errors = errors.concat(dynamicFiltersErrors);
		}
		if (errors.length == 0 && successHandler !== undefined) {
			successHandler.call(obj || this);
		} else {
			Sbi.exception.ExceptionHandler.showErrorMessage(errors.join('<br/>'), LN('sbi.formviewer.formviewerpage.validation.error'));
		}
	}

    // public methods

	, getFormState: function() {
		var state = {};
		if (this.staticClosedFiltersPanel !== null) {
			state.staticClosedFilters = this.staticClosedFiltersPanel.getFormState();
		}
		if (this.staticOpenFiltersPanel !== null) {
			state.staticOpenFilters = this.staticOpenFiltersPanel.getFormState();
		}
		if (this.dynamicFiltersPanel !== null) {
			state.dynamicFilters = this.dynamicFiltersPanel.getFormState();
		}
		if (this.groupingVariablesPanel !== null) {
			state.groupingVariables = this.groupingVariablesPanel.getFormState();
		}
		return state;
	}

	//set the saved values in the form
	, setFormState: function(state) {
		if (this.staticClosedFiltersPanel !== null && state.staticClosedFilters!=null) {
			this.staticClosedFiltersPanel.setFormState(state.staticClosedFilters);
		}
		if (this.staticOpenFiltersPanel !== null && state.staticOpenFilters!=null) {
			this.staticOpenFiltersPanel.setFormState(state.staticOpenFilters);
		}
		if (this.dynamicFiltersPanel !== null && state.dynamicFilters!=null) {
			this.dynamicFiltersPanel.setFormState(state.dynamicFilters);
		}
		if (this.groupingVariablesPanel !== null && state.groupingVariables!=null) {
			this.groupingVariablesPanel.setFormState(state.groupingVariables);
		}
	}
	
	//shows the save window
    , showSaveWindow: function(){
        var nameMeta = "";
        var descriptionMeta = "";
        var scopeMeta = "";
        
	    if(this.saveFormStateValuesWindow == null) {
	    	this.saveFormStateValuesWindow = new Sbi.widgets.SaveWindow({
	    		title: LN('sbi.qbe.queryeditor.savequery')
	    		, descriptionFieldVisible: true
	    		, scopeFieldVisible: true
	    	});
	    	
		      //getting meta informations 
	       	Ext.Ajax.request({
				url:  this.services['getMeta'],
				callback: function(options, success, response) {
       				if(success) {
       					if(response !== undefined && response.responseText !== undefined ) {
		      			    var content = Ext.util.JSON.decode( response.responseText );
		      			  
    		      			if (content !== undefined) {      		      					                   			 
    		      				nameMeta = content.name;                      
    		      				descriptionMeta = content.description; 
    		      				scopeMeta = (content.scope);    		                   
    		      				this.saveFormStateValuesWindow.setFormState({ name: nameMeta
                                	    		, description: descriptionMeta
                                	    		, scope: scopeMeta
                                	    	  });   				      			
    		      			} 
    		      		} else {
    		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
    		      		}
       				}
       			},
       			scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure		
	       	});  
	    	

	    	this.saveFormStateValuesWindow.on('save', this.saveFormValues, this);
		}
	    this.saveFormStateValuesWindow.show();
	}
  	
  	//saves the form fields values
    , saveFormValues: function(win, subObjectParams){
    	var formState = this.getFormState();
    	Sbi.formviewer.SaveFormValueSubObject.saveSubObject(formState, subObjectParams, this);
    }
    
});