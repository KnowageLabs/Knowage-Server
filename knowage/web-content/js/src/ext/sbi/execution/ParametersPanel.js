/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 
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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */


Ext.ns("Sbi.execution");

Sbi.execution.ParametersPanel = function(config, doc) {
	
	var defaultSettings = {
		columnNo: 3
		, columnWidth: 350
		, labelAlign: 'left'
		, fieldWidth: 200	
		, maskOnRender: false
		, fieldLabelWidth: 100
		, addEmptyValueToCombo: false
		, moveInMementoUsingCtrlKey: false
		, viewportWindowWidth: 300
		, viewportWindowHeight: 300
		, fieldsPadding : 5
		, maxFieldHeight : 300
	};
	
	
	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.parametersPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.parametersPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	// create a new variable and store settings into this new variable
	var temp = {};
	temp = Ext.apply(temp, defaultSettings);
	
	// merge settings and input configuration
	var c = Ext.apply(temp, config || {});
	
	if(doc.parametersRegion != undefined){
		c.parametersRegion = doc.parametersRegion;
	}
	
	// change column number to 1 if region is east
	if(c.parametersRegion != 'north')
		c.columnNo = 1;
	
	
	this.baseConfig = c;
	
	//global variable
	this.firstInitialization = true;

	
	this.parametersPreference = undefined;
	if (c.parameters) {
		this.parametersPreference = c.parameters;
	}
	if (this.parametersPreference) {
		this.preferenceState = Ext.urlDecode(this.parametersPreference);
	}
	
	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null, CONTEST: this.contest};
	
	this.services = this.services || new Array();
	
	this.services['getParametersForExecutionService'] = this.services['getParametersForExecutionService'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_PARAMETERS_FOR_EXECUTION_ACTION'
		, baseParams: params
	});
	this.services['getParameterValueForExecutionService'] = this.services['getParameterValueForExecutionService'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_PARAMETER_VALUES_FOR_EXECUTION_ACTION'
		, baseParams: params
	});
	this.services['saveViewpointService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_VIEWPOINT_ACTION'
		, baseParams: params
	});
	
	
	this.formWidth = ( (c.columnWidth + c.fieldsPadding) * c.columnNo) ;

	if (this.isMassiveExportContext()) {
		var formState = this.getFormState();
	} else {
		this.initViewpointsPanel(config, doc)
	}
	
	this.initTootlbar();
	this.initExecutionButton();
	
	c = Ext.apply({}, c, {
		labelAlign: c.labelAlign,
		tbar: this.toolbar,
        border: false,
        //bodyStyle:'padding:10px 0px 10px 10px',
        autoHeight: true,
        items: [{
        		// panel with execution button
	        	items: this.isMassiveExportContext() ? {html: "&nbsp;"} : this.executionButton  // do not display execution button on massive export context
	        	, width: 70
	            , border: false
	            , style: c.parametersRegion == 'north' 
	            	? 'padding:' + c.fieldsPadding + 'px 0px 0px ' + c.fieldsPadding + 'px;'
	            	: 'margin-left: auto; margin-right: auto; padding: 10px 0px 10px 0px;'
	        }, {
	        	// separator panel
	        	html: '&nbsp;'
    			, layout : 'fit'
    			, border : !this.isMassiveExportContext() && c.parametersRegion == 'east'  	// puts border only in case the parameters panel is displayed on east region
    																						// and context is not massive export context
    			, height: 0
    		}, {
    			// panel with parameters
    	        layout:'table'
	            , layoutConfig: {
	                columns: c.parametersRegion == 'north' ? c.columnNo : 1
	            }
	            , width: this.formWidth 
	            , border: false
    	}]
	});

	
	// constructor
    Sbi.execution.ParametersPanel.superclass.constructor.call(this, c);
	
    this.tableContainer = this.items.get(2);
	
	this.addEvents(
			'beforesynchronize'
			, 'synchronize'
			, 'parametersForExecutionLoaded'
			, 'viewpointexecutionrequest'
			, 'applyviewpoint'
			, 'hideparameterspanel'
			, 'collapseparameterspanel'
			, 'executionbuttonclicked'
			, 'checkReady'
			, 'ready'
	);	
};

Ext.extend(Sbi.execution.ParametersPanel, Ext.FormPanel, {
    
    services: null
    , executionInstance: null
    , executionButton: null
    
	/* ATTENTION: patch for bug 1594 --> firstLoadCounter counter over firstLoadTotParams: every time a store calling the getParametersValuesForExecution has loaded
	 * the counter is updated (for all the parameters that need the load = firstLoadTotParams) . When the counter matches the total then the 'ready' event is thrown.
	 * DocumentExecutionPage is listening on ParametersPnel on 'synchronize' AND on 'ready'. */
    , firstLoadCounter: 0
    , firstLoadTotParams: 0
    

    , columnNo: 0 
    
    /**
     * parameters configuration as returned from getParametersForExecutionService. 
     * @see function loadParametersForExecution()
     */
    , parameters: null
   
    /**
     * url encoded parameters whose value must be set during initialization (ex. foodFamily=Drink)
     */
    , parametersPreference: null
    /**
     * url decoded parameters whose value must be set during initialization (ex. {foodFamily:'Drink'})
     */
    , preferenceState: null
    
    /**
     * An array of all the fields contained in the form 
     * Injected properties:
     *  - isTransient: true if ???
     *  - columnNo: the number of the column containing the field
     *  - dependecies: an array of all the fields that depends from this one
     *  - dependants: an array of all fields on which the field depends on
     */
    , fields: null
    
    /**
     * The columns (Ext.FormPanel) that compose the main column layout
     */
    , columns: null
    , baseConfig: null
    , modality : null
    , drawHelpMessage : false
    , mandatoryFieldAdditionalString: null
    
    , manageDataDependencies: true // not used so far but reserved for future use
    , manageVisualDependencies: true
    , manageVisualDependenciesOnVisibility: true
    , manageVisualDependenciesOnLabel: true
    
    , showViewpointWin:null
    , saveViewpointWin: null
    
    // ----------------------------------------------------------------------------------------
    // public methods
    // ----------------------------------------------------------------------------------------
    
    , initTootlbar: function(){
    	var toolbarItems = ['->'];
    	toolbarItems.push(new Ext.Toolbar.Button({
			iconCls: 'icon-clear'
				, tooltip: LN('sbi.execution.parametersselection.toolbar.clear')
			   	, scope: this
			   	, handler : function() {
					this.clearParametersForm();
				}
			}));
    	
    	if (Sbi.user.functionalities.contains('SeeViewpointsFunctionality') && !this.isFromCross &&
    			!this.isMassiveExportContext() ) {
			toolbarItems.push(new Ext.Toolbar.Button({
				iconCls: 'icon-saved-parameters '
				, tooltip: LN('sbi.execution.parametersselection.toolbar.open')
			   	, scope: this
			   	, handler : function() {
					this.openParametersFormStateAsViewpoint();
				}
			}));

			toolbarItems.push(new Ext.Toolbar.Button({
				iconCls: 'icon-save'
				, tooltip: LN('sbi.execution.parametersselection.toolbar.save')
			   	, scope: this
			   	, handler : function() {
					this.saveParametersFormStateAsViewpoint();
				}
			}));
		}
		
    	this.toolbar = new Ext.Toolbar({
			items:toolbarItems
		});
    }

	, clearParametersForm: function() {
		//this.reset();
		var defaultValuesFormState = this.getDefaultValuesFormState();
		Sbi.debug('[ParametersPanel.clearParametersForm] : default values form state is [' + defaultValuesFormState + ']');
		var state = Ext.apply(defaultValuesFormState, this.preferenceState);
		Sbi.debug('[ParametersPanel.clearParametersForm] : preference state applied to default values [' + Sbi.toSource(state) + ']');
		this.setFormState(state);
	}
	
	, initExecutionButton: function () {
    	this.executionButton = new Ext.Button({
	        text: LN('sbi.execution.parametersselection.executionbutton.message')
	        , tooltip: LN('sbi.execution.parametersselection.executionbutton.tooltip')
	        , handler: this.executionButtonHandler
	        , scope: this
		});
	}
	
    , executionButtonHandler : function() {
    	this.fireEvent('executionbuttonclicked', this);
	}
	
	, initViewpointsPanel: function(config, doc){
		if(!this.viewpointsPanel){
			var thisPanel= this;
			
			config.showTitle = false;
			this.viewpointsPanel =  new Sbi.execution.ViewpointsPanel(config, doc);
			delete config.showTitle;
			
			this.viewpointsPanel.on('executionrequest', function(viewpoint) {
				thisPanel.fireEvent('viewpointexecutionrequest', viewpoint);
				thisPanel.showViewpointWin.hide();
		    }, this);
			this.viewpointsPanel.on('applyviewpoint', function(viewpoint) {
				thisPanel.applyViewPoint(viewpoint);
				thisPanel.showViewpointWin.hide();
		    }, this);
		}
	}
	
	, synchronizeViewpoints: function( executionInstance ) {
		this.viewpointsPanel.synchronize( executionInstance );
	}
	
	, openParametersFormStateAsViewpoint: function() {
		if(this.showViewpointWin === null) {
			this.showViewpointWin = new Ext.Window({
				layout: 'fit',
				width: 400,
				height: 400,
				closeAction:'hide',
				title: LN('sbi.execution.viewpoints.title'),
				items: [this.viewpointsPanel]
			});
		}
		this.showViewpointWin.show();
	}
	
	, saveParametersFormStateAsViewpoint: function() {
		if(this.saveViewpointWin === null) {
			this.saveViewpointWin = new Sbi.widgets.SaveWindow();
			this.saveViewpointWin.on('save', function(w, state) {
				var params = Ext.apply({}, state, this.executionInstance);
				var formState = this.getFormState();
				for(var p in formState) {
					if(formState[p] instanceof Array ) {
						formState[p] = formState[p].join(';');
					}
				}
				params.viewpoint = Sbi.commons.JSON.encode( formState );
	
			
					Ext.Ajax.request({
			          url: this.services['saveViewpointService'],
			          
			          params: params,
			          
			          callback : function(options, success, response){
						if(success && response !== undefined) {   
				      		if(response.responseText !== undefined) {
				      			var content = Ext.util.JSON.decode( response.responseText );
				      			if(content !== undefined) {
				      				Ext.MessageBox.show({
					      				title: 'Status',
					      				msg: LN('sbi.execution.viewpoints.msg.saved'),
					      				modal: false,
					      				buttons: Ext.MessageBox.OK,
					      				width:300,
					      				icon: Ext.MessageBox.INFO 			
					      			});
				      				if (this.viewpointsPanel) {
				      					this.viewpointsPanel.addViewpoints(content);
				      				}
				      			} 
				      		} else {
				      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				      		}
			    	   }			    	  	
			          },
			          scope: this,
			  		  failure: Sbi.exception.ExceptionHandler.handleFailure      
			     });
				
			}, this);
		}
		this.saveViewpointWin.show();
	}
    
    , synchronize: function( executionInstance ) {
		var sync = this.fireEvent('beforesynchronize', this, executionInstance, this.executionInstance);
		this.executionInstance = executionInstance;
		this.loadParametersForExecution( );
		this.synchronizeViewpoints(executionInstance);
	}

	, getFieldValue: function(field) {
		var value;
		
		if(!field) return;
		
		if(field.behindParameter && field.behindParameter.multivalue === true) {
			if(field.getValues) {
				value = field.getValues();
			} else {
				Sbi.warn('Field [' + field.id + '] is multivalue but does not implement method getValues()');
				value = field.getValue();
			}
		} else {
			value = field.getValue();
		}
		
		return value;
	}
	
	, getFormState: function() {
		var state;
		
		//to avoid synchronization problem
		state = {};
		for(p in this.fields) {
			var field = this.fields[p];
			if(field.title == 'empty') continue;
			var value = this.getFieldValue(field);
			state[field.name] = value;
			var rawValue = field.getRawValue();
			if(value == "" && rawValue != ""){
				state[field.name] = rawValue;
			}
			
			
			if (rawValue !== undefined) {
				// TODO to improve: the value of the field should be an object with actual value and its description
				// Conflicts with other parameters are avoided since the parameter url name max lenght is 20
				state[field.name + '_field_visible_description'] = rawValue;
			}
			
			// add objParsId information if present (massive export case)
			if(field.objParameterIds && this.contest=='massiveExport'){
				state[field.name + '_objParameterIds']=field.objParameterIds;
			}
			
		}

		return state;
	}
	
	, setFormState: function( state ) {
		Sbi.trace('[ParametersPanel.setFormState] : IN');
		
		for(p in state) {
			var fieldName = p;
			var fieldValue = state[p];
			if(this.fields[fieldName]) {
				Sbi.debug('[ParametersPanel.setFormState] : Set value [' + p + '] to [' + fieldValue + ']');
				var aField = this.fields[fieldName];
				var hasChangeEvent = false;		
				if(aField.hasListener('change')) {
					hasChangeEvent = true;
					aField.un('change', this.onUpdateDependentFields);
				}			
				aField.setValue( fieldValue );
				if(hasChangeEvent) aField.on('change', this.onUpdateDependentFields, this);
				
				
				var fieldDescription = fieldName + '_field_visible_description';
				var rawValue = state[fieldDescription];
				if (rawValue !== undefined && rawValue != null && this.fields[fieldName].rendered === true) {
					this.fields[fieldName].setRawValue( rawValue );
					//this.updateDependentFields( this.fields[fieldName] );
				}
			}
		}
		
		Sbi.trace("[ParametersPanel.setFormState] : OUT");
	}
	
	, applyViewPoint: function(v) {
		Sbi.trace("[ParametersPanel.applyViewPoint] : " + typeof v);
		for(var p in v) {
			var str = '' + v[p];
			if(str.split(';').length > 1) {
				v[p] = str.split(';');
				Sbi.trace("[ParametersPanel.applyViewPoint] : split parameter [" + p + "] in [" + v[p].length+ "] chunks");
			}
		}
		this.setFormState(v);
	}
	  
	, resetField: function(aField, suspendEvents) {
		suspendEvents = suspendEvents || false;
		var hasChangeEvent = false;				
		if(suspendEvents && aField.hasListener('change')) {
			hasChangeEvent = true;
			aField.un('change', this.onUpdateDependentFields);
		}			
		aField.reset();
		if(hasChangeEvent) aField.on('change', this.onUpdateDependentFields, this);
	}
	
	/**
	 * reset all the fields in the form and recalculate all the dependencies
	 */
	, reset: function() {
		Sbi.trace('[ParametersPanel.reset] : IN');
		//change for menu calls		
		for(p in this.fields) {
			if (!this.isInPreferences(p)){
				var aField = this.fields[p];
				if (!aField.isTransient) {
					Sbi.debug('[ParametersPanel.reset] : Reset field [' + p + ']');
					this.resetField(aField, true);
					this.updateDependentFields( aField );
				}
			}
		}
		
		for(p in this.fields) {
			if (!this.isInPreferences(p)){
				var aField = this.fields[p];
				aField.clearInvalid();
			}
		}
		Sbi.trace('[ParametersPanel.reset] : OUT');
	}
	
	, isInPreferences: function(p){
		if (this.parametersPreference == null || this.parametersPreference == undefined || this.parametersPreference == "") return false;
		
		var values = this.parametersPreference.split("&");
		for (var i=0, l=values.length; i<l; i++){
			var parName = values[i].substring(0,  values[i].indexOf("="));
			if (parName == p) return true;
		}
		
		return false;
	}
	
	/**
	 * @deprecated use this.reset() instead
	 */
	, clear: function() {
		this.reset();
	}
	
	, getParentPageNumber: function() {
		return this.baseConfig.pageNumber;
	}
	
	, isInParametersPage: function() {
		return this.getParentPageNumber() === 2;
	}
	
	, isInExecutionPage: function() {
		return this.getParentPageNumber() === 3;
	}
	
	, isReadyForExecution: function() {
		if(this.parameters.length == 0) {
			return true;
		} else 	{
			for (p in this.fields) {
				var field = this.fields[p];
				if(!field.allowBlank){
					var behindParameter = field.behindParameter;
					var value = field.getValue();
					value = value || this.concatenateDefaultValues(behindParameter.defaultValues);
					if(field.isTransient == false && (value==undefined || value==null || value.length==0)){
						return false;
					}
				}
			}
			return true;
		}
	}
	
	// ----------------------------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------------------------
	
	
	// =====================================================================================
	// PARAMETERS functions
	// =====================================================================================
	
	, setParameters: function(parameters) {
		this.parameters = parameters;
	}
	
	, parameterHasDependencies: function(parameter) {
		return parameter.dependencies && parameter.dependencies.length > 0;
	}
	
	, parameterHasOnlyOneValue: function(parameter) {
		return parameter.valuesCount !== undefined && parameter.valuesCount == 1;
	}
	
	, parameterValueIsInPreferences: function(parameter) {
		return this.preferenceState !== null && this.preferenceState[parameter.id] !== undefined;
	}
	
	, parameterValueIsPassedFromCross: function(parameter) {
		return this.parameterValueIsInPreferences(parameter) && this.isFromCross == true;
	}
	
	, parameterValueIsPassedFromMenu: function(parameter) {
		return this.parameterValueIsInPreferences(parameter) && this.isFromCross == false;
	}
	
	, thereAreParametersToBeFilled: function() {
		var thereAreParametersToBeFilled = false;
		if(this.parameters.length > 0) {
			var o = this.getFormState();
			for(p in o) {
				// must check this.fields[p] is undefined because form state contains also parameters' descriptions
				if(this.fields[p] != undefined && this.fields[p].isTransient === false) {
					thereAreParametersToBeFilled = true;
					break;
				}
			}
		}
		return thereAreParametersToBeFilled;
	}
	
	, loadParametersForExecution: function( ) {
		
		if( !this.executionInstance ) {
			alert("Impossible to load parameters because executionInstance is not properly initialized");
		}
		
		Ext.Ajax.request({
	          url: this.services['getParametersForExecutionService'],
	          
	          params: this.executionInstance,
	          
	          callback : function(options, success, response){
	    	  	if(success && response !== undefined) {   
		      		if(response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content !== undefined) {
		      				this.initializeParametersPanel(content, true);
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
	    	  	}
	          },
	          scope: this,
	  		  failure: Sbi.exception.ExceptionHandler.handleFailure      
	     });
	}

	, initializeParametersPanel: function( parameters, reset ) {
			
		Sbi.trace('[ParametersPanel.initializeParametersPanel] : IN');
				
		this.on('checkReady', function () {
			if((this.firstLoadCounter == this.firstLoadTotParams )
					|| (this.firstLoadTotParams == 0)){	
				this.fireEvent('ready', this);	
			}
				
		}, this);
		
		this.setParameters(parameters);
		
		this.removeAllFields();		
		
		var nonTransientField = 0;
		
//		if(!this.parameters || this.parameters.length==0){
//			this.fireEvent("hideparameterspanel");
//		}
			
		var occupiedInRow = 0;
		
		for(var i = 0; i < parameters.length; i++) {
			
			if(this.parametersRegion != 'north'){
				parameters[i].colspan = 1;
			}
			
			if(this.columnNo > 1){
			// how many spaces to insert
			if( occupiedInRow != this.columnNo &&
					occupiedInRow+parameters[i].colspan > this.columnNo
					){
				var howManySpacesToInsert = this.columnNo - occupiedInRow;
				
				for(var sp=0;sp<howManySpacesToInsert;sp++){
					this.addEmptyField(null, nonTransientField++);
				}
				
				occupiedInRow = 0;
			}

			}
			
			var field = this.createField( parameters[i] );
				
			var isAdded = false;
			
			if( this.parameterHasOnlyOneValue( parameters[i] ) ) {
				if( this.parameterHasDependencies( parameters[i] ) || parameters[i].type === 'DATE') {
					this.addField(field, nonTransientField++);
					isAdded = true;
				} else {
					field.isTransient = true;
					field.setValue(parameters[i].value);
				}
			} else {				
				if ( this.parameterValueIsPassedFromMenu(parameters[i]) ) {
					field.setValue(this.preferenceState[parameters[i].id]);
//					alert("initializeParametersPanel: set value of [" + parameters[i].id + "] " +
//							"to [" + field.getValue() + "] " +
//							"but dont add it to panel");
					Sbi.debug("[ParametersPanel.initializeParametersPanel]: set value of [" + parameters[i].id + "] " +
							"to [" + field.getValue() + "] " +
							"but dont add it to panel");
				} else {
					
					// the parameter is passed in prefereces but it not came from the menu, it came from a cross nav
					if( this.parameterValueIsInPreferences(parameters[i]) ){
						//field.setValue(this.preferenceState[parameters[i].id]);
//						alert("initializeParametersPanel: set value of [" + parameters[i].id + "] " +
//								"to [" + this.preferenceState[parameters[i].id] +"] " +
//								"and add it to panel");
						Sbi.debug("[ParametersPanel.initializeParametersPanel]: set value of [" + parameters[i].id + "] " +
								"to [" + this.preferenceState[parameters[i].id] +"] " +
								"and add it to panel");
					} else {
//						alert("initializeParametersPanel: parameter [" + parameters[i].id + "] not in preferences");
						Sbi.debug("[ParametersPanel.initializeParametersPanel]: parameter [" + parameters[i].id + "] not in preferences");
					}
					
					if (parameters[i].visible === true && parameters[i].vizible !== false) {
						this.addField(field, nonTransientField++);
						isAdded = true;
						Sbi.debug('field [' + parameters[i].id + '] is added');
					} else {
						Sbi.debug('field [' + parameters[i].id + '] is not added');
					}
				}
			}

			//if(isAdded==true){
				this.fields[parameters[i].id] = field;
			//}
				// update occupiedInRow 
				occupiedInRow  += parameters[i].colspan; 
			 
		}

		if(this.thereAreParametersToBeFilled() !== true) {
			if (this.rendered) {
				Ext.DomHelper.append(this.body, '<div class="x-grid-empty">' + LN('sbi.execution.parametersselection.noParametersToBeFilled') + '</div>');
				if(this.isFromCross === true) {
					this.fireEvent("collapseparameterspanel");
				} else {
					this.fireEvent("hideparameterspanel");
				}
			}
		} else {
			// set focus on first field
			// this is a work-around for this problem on IE: very often, the manual input field is not editable;
			// in order to let it be editable, you should click on input label, or above + TAB button
			var firstItem = this.tableContainer.items.get(0).items.get(0); // remember that input fields are wrapped by a panel
			var itemParameter = firstItem.behindParameter;
			if (itemParameter.typeCode == 'MAN_IN') {
				firstItem.on('render', function(theField) {
					theField.focus();
					theField.clearInvalid();
				}, this);
			}
		}
		
		this.doLayout();
		
		this.initializeFieldDependencies();
		
		var defaultValuesFormState = this.getDefaultValuesFormState();
		Sbi.debug('[ParametersPanel.initializeParametersPanel] : default values form state is [' +  Sbi.toSource(defaultValuesFormState) + ']');
		var state = Ext.apply({}, defaultValuesFormState);
		for(p in this.preferenceState) {
			state[p] = this.preferenceState[p];
			delete state[p + '_field_visible_description'];			
		}
		//var state = Ext.apply(defaultValuesFormState, this.preferenceState);
		Sbi.debug('[ParametersPanel.initializeParametersPanel] : preference state applied to default values [' + Sbi.toSource(state) + ']');
		this.setFormState(state);
		this.firstInitialization = false;

			
		if (this.firstLoadTotParams == 0 && reset) {
			this.fireEvent('ready', this, this.isReadyForExecution(), state);	
			this.fireEvent('synchronize', this);	
		}
		
		
		Sbi.trace('[ParametersPanel.initializeParametersPanel] : OUT');
	}
	
	, getDefaultValuesFormState: function () {
		var state;
		
		state = {};
		for (p in this.fields) {
			var field = this.fields[p];
			
			if(field.title == 'empty') continue;
			
			var behindParameter = field.behindParameter;
			var value = this.concatenateDefaultValues(behindParameter.defaultValues);
			Sbi.debug('[ParametersPanel.getDefaultValuesFormState] : default values for field [' + field.name + '] is [' + value + ']');
			var description = this.concatenateDefaultValuesDescription(behindParameter.defaultValues);
			Sbi.debug('[ParametersPanel.getDefaultValuesFormState] : default description for field [' + field.name + '] is [' + description + ']');
			if (!field.isTransient) {
				// in case the parameters is transient (i.e. it is single-value and therefore hidden), it is not considered
				Sbi.debug('[ParametersPanel.getDefaultValuesFormState] : field [' + field.name + '] is not transient');
				state[field.name] = value;
				state[field.name + '_field_visible_description'] = description;
			} else {
				Sbi.debug('[ParametersPanel.getDefaultValuesFormState] : field [' + field.name + '] is transient and therefore skipped');
			}
		}
		
		Sbi.debug('[ParametersPanel.getDefaultValuesFormState] : returning [' + Sbi.toSource(state) + ']');
		return state;
	}



	
	, concatenateDefaultValues: function (defaultValues) {
		if (defaultValues.length == 0) {
			return null;
		}
		if (defaultValues.length == 1) {
			return defaultValues[0].value;
		} else {
			var value = new Array();
			for (var i = 0; i < defaultValues.length; i++) {
				value[i] = defaultValues[i].value;
			}
			return value;
		}
	}
	
	, concatenateDefaultValuesDescription: function (defaultValues) {
		if (defaultValues.length == 0) {
			return '';
		}
		if (defaultValues.length == 1) {
			return defaultValues[0].description;
		} else {
			var description = '';
			for (var i = 0; i < defaultValues.length; i++) {
				description += defaultValues[i].description + '; ';
			}
			return description;
		}
	}
	
	
	// =====================================================================================
	// DEPENDENCIES management functions
	// =====================================================================================
	
	, onUpdateDependentFields: function(field, record, index) {
		Sbi.trace('[ParametersPanel.onUpdateDependentFields] : IN');
		this.updateDependentFields( field );
		Sbi.trace('[ParametersPanel.onUpdateDependentFields] : OUT');
	}
	, initializeFieldDependencies: function() {
		for(var j = 0; j < this.parameters.length; j++) {
			
			if( this.parameters[j].dependencies.length > 0) {
				var field = this.fields[this.parameters[j].id];
				var p =  this.parameters[j];
				
				field.on('focus', function(f){
					if(f.dependencies){
						for(var i = 0; i < f.dependencies.length; i++) {
							var field = this.fields[ f.dependencies[i].urlName ];
							field.getEl().addClass('x-form-dependent-field');                         
						}	
					}
	
				}, this
				, {delay:250}
				);
				
				field.on('blur', function(f){
					if(f.dependencies){
						for(var i = 0; i < f.dependencies.length; i++) {
							var field = this.fields[ f.dependencies[i].urlName ];
							field.getEl().removeClass('x-form-dependent-field');                         
						}
					}

				}, this);
				
				if(field.dependencies){
					for(var i = 0; i < field.dependencies.length; i++) {
						var f = this.fields[ field.dependencies[i].urlName ];
						f.dependants = f.dependants || [];
						field.dependencies[i].parameterId = this.parameters[j].id;
						f.dependants.push( field.dependencies[i] );                      
					}	
				} else {
					alert('Field [' + field.name + '] have no dependencies defined');
				}

			}			
		}
		
		for(var p in this.fields) {
			var theField = this.fields[p];
			
			if(theField.title == 'empty') continue;
			
			if (theField.behindParameter && theField.behindParameter.selectionType === 'TREE') {
				
				this.fields[p].on('select', function(field, record, index) {
					this.updateDependentFields( field );
				} , this);
				
			} else 
			
			if (theField.behindParameter && theField.behindParameter.selectionType === 'LOOKUP') {
			
				this.fields[p].on('select', function(field, record, index) {
					this.updateDependentFields( field );
				} , this);
				
			} else if(theField.behindParameter.selectionType === 'COMBOBOX'
				|| theField.behindParameter.selectionType === 'LIST' 
				|| theField.behindParameter.selectionType === 'SLIDER') {
				this.fields[p].on('change', this.onUpdateDependentFields, this);
			} else if(theField.behindParameter.typeCode == 'MAN_IN') {
				
				if(theField.behindParameter.type == "DATE"){
					this.fields[p].on('change', function(field, record, index) {
						this.updateDependentFields( field );
					} , this);
				}else{
					// if input field has an element (it means that the field was displayed)
					if (theField.el !== undefined) {
						
						theField.el.on('keydown', 
							this.updateDependentFields.createDelegate(this, [theField]), this, {buffer: 350});
						
						
						var onKeyDown = function(event, element, options , field){
							if( event.keyCode == 38 || event.keyCode == 40 ) {
								if(!this.moveInMementoUsingCtrlKey || event.ctrlKey == true) {
									var moveDown = (event.keyCode == 40);
									this.setValueFromMemento(field, moveDown);
								}
							} 
						}
						
						theField.el.on( 'keydown', onKeyDown.createDelegate(this, theField, true), this );
					}
				}
				
				
			} else {
				alert("Unable to manage dependencies on input field of type [" + theField.behindParameter.selectionType + "]");
			}
		}
	}
	
	, updateDependentFields: function(f) {
		
		//if(f.behindParameter.selectionType === 'SLIDER') alert('SLIDER updateDependentFields');
		
		Sbi.trace('[ParametersPanel.updateDependentFields] : IN');
		Sbi.debug('[ParametersPanel.updateDependentFields] : updating fields that depend on [' + f.name + ']');
		
		var hasDataDependency = false;
		var hasVisualDependency = false;
		
		if (f.dependants !== undefined) {
			for(var i = 0; i < f.dependants.length; i++) {
				if(f.dependants[i].hasDataDependency === true) {
					this.updateDataDependentField(f, f.dependants[i]);
					hasDataDependency = true;
				}
				
				if(this.manageVisualDependencies === true && f.dependants[i].hasVisualDependency === true) {
					this.updateVisualDependentField(f, f.dependants[i]);
					hasVisualDependency = true;
				}
			}
		}
		
		if(this.manageVisualDependenciesOnVisibility == true 
			&& hasVisualDependency === true 
			&& Sbi.settings.invisibleParameters.remove === true) {
			
			Sbi.debug('[ParametersPanel.updateDependentFields] : Manage visibility dependencies triggered by field [' + f.name + ']');
		
			this.doRemoveNotVisibleFields();
			
			Sbi.debug('[ParametersPanel.updateDependentFields] : Visibility dependencies triggered by field [' + f.name + '] have been succesfully managed');
		
		}
		
		Sbi.debug('[ParametersPanel.updateDependentFields] : fields that depend on [' + f.name + '] have been succesfully updated');
		Sbi.trace('[ParametersPanel.updateDependentFields] : OUT');
	}
	
	, _doRemoveNotVisibleFields: function() {
		this.refreshFields();
	}
	
	
	, doRemoveNotVisibleFields: function() {
		this.manageVisualDependenciesOnVisibility = false;
		Sbi.trace('[ParametersPanel.doRemoveNotVisibleFields] : IN');
		
		var state = this.getFormState();			
		this.removeAllFields();
		
		this.initializeParametersPanel(this.parameters, false);	
		//Sbi.trace('[ParametersPanel.doRemoveNotVisibleFields] : restore state [' + state.toSource() + ']');
		this.setFormState(state);
		
		Sbi.trace('[ParametersPanel.doRemoveNotVisibleFields] : OUT');
		
		this.manageVisualDependenciesOnVisibility = true;
	}
	
	, updateDataDependentField: function(fatherField, dependantConf) {
		Sbi.debug('[ParametersPanel.updateDataDependentField] : updating field [' + dependantConf.parameterId + '] that is data correlated with field [' + fatherField.name + ']');
		
		var field = this.fields[ dependantConf.parameterId ];
		if(field.behindParameter.selectionType === 'COMBOBOX' 
			|| field.behindParameter.selectionType === 'LIST' 
			|| field.behindParameter.selectionType === 'SLIDER'){ 
			field.store.load();
		}		
		if(field.behindParameter.selectionType === 'TREE'){ 
			var p = Sbi.commons.JSON.encode(this.getFormState());
			field.reloadTree(p);
			if (!this.firstInitialization){
				field.reset();
			}
		} else {
			field.reset();
		}
		Sbi.debug('[ParametersPanel.updateDataDependentField] : field [' + dependantConf.parameterId + '] that is data correlated with field [' + fatherField.name + '] have been updeted succesfully');
	}
	
	, updateVisualDependentField: function(fatherField, dependantConf) {
		Sbi.debug('[ParametersPanel.updateVisualDependentField] : updating field [' + dependantConf.parameterId  + '] that is visually correlated with field [' + fatherField.name + ']');
		var dependantField = this.fields[ dependantConf.parameterId ];
		var conditions = dependantConf.visualDependencyConditions;
		
		var fatherFieldValues;
		fatherFieldValues = this.getFieldValue(fatherField);
		if(!fatherFieldValues) {
			fatherFieldValues = [];
		} else if(fatherField.behindParameter.multivalue === false) {
			fatherFieldValues = [fatherFieldValues];
		}
		
		var fatherFieldValueSet = {};
		for(var i = 0; i < fatherFieldValues.length; i++) {
			if(fatherFieldValues[i]){
				var v = Ext.util.Format.trim(fatherFieldValues[i]);
				fatherFieldValueSet[ v ] = v;
			}
		}
		
		var disableField = conditions.length > 0;
		
		Sbi.debug('[ParametersPanel.updateVisualDependentField] : check visual condition on [' + dependantField.name + ']');
		for(var i = 0; i < conditions.length; i++) {
			// check condition
			var condition = conditions[i];
			if( this.isVisualConditionTrue(condition, fatherFieldValueSet) ) {
				if(this.manageVisualDependenciesOnLabel === true) {
					this.setFieldLabel(dependantField, condition.label);
				}
				disableField = false;
			}
		}
		Sbi.debug('[ParametersPanel.updateVisualDependentField] : condition on [' + dependantField.name + '] are satisfied: ' + !disableField);
		
		if(this.manageVisualDependenciesOnVisibility === true) {
			if(disableField) {
				Sbi.debug('[ParametersPanel.updateVisualDependentField] : trying to disable field [' + dependantField.name + ']');
				this.setFieldLabel(dependantField, dependantField.fieldDefaultLabel);
				
				//this.resetField(dependantField, true);
				//dependantField.reset();
				dependantField.disable();
				this.hideFieldLabel(dependantField);
				dependantField.setVisible(false);
				dependantField.parameter.vizible = false;
			} else {
				dependantField.enable();
				dependantField.setVisible(true);	
				dependantField.parameter.vizible = true;
			}
		}
		
		Sbi.debug('[ParametersPanel.updateVisualDependentField] : field [' + dependantConf.parameterId + '] that is visually correlated with field [' + fatherField.name + '] have been updeted succesfully');
	}
	
	
	, isVisualConditionTrue: function(condition, fatherFieldValueSet) {
		var conditionIsTrue = false;
		var values = condition.value.split(',');
		for(var i = 0; i < values.length; i++) {
		  if(values[i]){
			var v = Ext.util.Format.trim(values[i]);
			if(fatherFieldValueSet[v]) {
				conditionIsTrue = true;
				break;
			}
		   }
		}
		
		conditionIsTrue = (condition.operation == 'contains')? conditionIsTrue: !conditionIsTrue
		Sbi.debug('[ParametersPanel.isVisualConditionTrue] : condition [' + condition.value + '] is [' + conditionIsTrue + ']' );
		//if(!conditionIsTrue) Sbi.debug('[ParametersPanel.isVisualConditionTrue] : father field values [' + fatherFieldValueSet.toSource() + ']' );
		return (conditionIsTrue);
	}
	
	
	// =====================================================================================
	// FIELDS functions
	// =====================================================================================
	
	, refreshFields: function() {
		this.manageVisualDependenciesOnVisibility = false;		
		Sbi.trace('[ParametersPanel.refreshFields] : IN');
		for(p in this.fields) {
			// if input field has an element (it means that the field was displayed)
			if (this.fields[p].el !== undefined) {
				// retrieves the element containing label plus input field and removes it
				Sbi.trace('[ParametersPanel.refreshFields] : removing field [' + p + '] ...');
				var el = this.fields[p].el.up('.x-form-item');
				this.columns[this.fields[p].columnNo].remove( this.fields[p], false );
				this.columns[this.fields[p].columnNo].doLayout();
				//el.remove();
			}
		}
		this.doLayout();
		Sbi.trace('[ParametersPanel.refreshFields] : all fields removed succesfully');
		alert('all fields removed succesfully');
		
	
		var index = 0;
		for(p in this.fields) {
			Sbi.trace('[ParametersPanel.refreshFields] : adding field [' + p + ']');
			var field = this.fields[p];
			if(field.isVisible()) {
				this.addField(field, index++);
				//var field = this.createField( this.parameters[0] );
				this.addField(field, index++);
				Sbi.trace('[ParametersPanel.refreshFields] : field [' + p + '] succesfully added');
			} else {
				Sbi.trace('[ParametersPanel.refreshFields] : field [' + p + '] not added because is not visible');
			}
		}
		this.doLayout();
		alert('all fields added succesfully');
		
		Sbi.trace('[ParametersPanel.refreshFields] : OUT');
		this.manageVisualDependenciesOnVisibility = true;
	}
	
	, addField: function(field, index) {
		field.isTransient = false;
		var newPanel = new Ext.Panel({
	        layout: 'form'
	        , autoDestroy: false
	        //, title: 'miaoooooo'
	        , colspan: this.columnNo > 1 ? field.colspan : 1
	        , border: false
	        , autoScroll: true
	        , bodyStyle : {
	        	padding : '' + this.fieldsPadding + 'px 0px 0px ' + this.fieldsPadding + 'px'  // padding is applied on top and left regions
	        	, maxHeight : '' + this.maxFieldHeight + 'px'
	        }
	        , items : [ field ]
		});
		this.tableContainer.add ( newPanel );

		
	}
	
	, addEmptyField: function(field, index) {
		var newPanel = new Ext.Panel({
			html: "aaa"
				,title: "Title not present"
		});
		this.tableContainer.add ( newPanel );
		// panel empty must be invisible
		newPanel.setVisible(false);
		
	}

	
	/**
	 * Remove and destroy all fields contained in the form. This is a private function
	 * and should not be called from an external comeponent. To just reset fields content
	 * use reset().
	 */
	, removeAllFields : function() {
		
		this.remove(this.tableContainer, true);
		this.doLayout();
		this.add({
            layout:'table'
            , layoutConfig: {
                columns: this.columnNo
            }
            , width: this.formWidth 
            , border: false
        });
		this.tableContainer = this.items.get(2);
		
		this.fields = {};
	}
	
	, setFieldLabel: function(field, label){
		field.behindParameter.label = label;
		
		// if input field has no element it means that the field wasn't displayed so we have 
		// nothing to do here
		if (field.el === undefined) return;
		
		var el = field.el.dom.parentNode.parentNode;    
		if( el.children[0].tagName.toLowerCase() === 'label' ) {  
			//el.children[0].class = 'x-exec-paramlabel-disabled';
			el.children[0].innerHTML = label + ':';    
		} else if( el.parentNode.children[0].tagName.toLowerCase() === 'label' ){    
			//el.parentNode.children[0].class = 'x-exec-paramlabel-disabled';
			el.parentNode.children[0].innerHTML = label + ':';  
			
		}    
	}
	
	, hideFieldLabel: function(field){    
		// if input field has no element it means that the field wasn't displayed so we have 
		// nothing to do here
		if (field.el === undefined) return;
		
		var el = field.el.dom.parentNode.parentNode;    
		if( el.children[0].tagName.toLowerCase() === 'label' ) {  
			//el.children[0].class = 'x-exec-paramlabel-disabled';
			el.children[0].innerHTML = '';    
		} else if( el.parentNode.children[0].tagName.toLowerCase() === 'label' ){    
			//el.parentNode.children[0].class = 'x-exec-paramlabel-disabled';
			el.parentNode.children[0].innerHTML ='';  
			
		}    
	}
	
	
	// ==============================================================================
	// Create fields
	// ==============================================================================
		
	, createDateField: function( baseConfig, executionInstance ) {
		
		var p = baseConfig.parameter;
		
		baseConfig.format = Sbi.config.localizedDateFormat;
		
		field = new Ext.form.DateField(baseConfig);
		
		if(p.value !== undefined && p.value !== null) {	
			var dt = Sbi.commons.Format.date(p.value, Sbi.config.clientServerDateFormat);
			field.setValue(p.value);				
		}
		
		return field;
		
	}

	, createLookupField: function( baseConfig, executionInstance )  {
		
		var p = baseConfig.parameter;
		
		var params = this.getBaseParams(p, executionInstance, 'complete');
		
		var store = this.createStore();
		store.on('beforeload', function(store, o) {
			var p = Sbi.commons.JSON.encode(this.getFormState());
			o.params.PARAMETERS = p;
			return true;
		}, this);
		
		field = new Sbi.widgets.LookupField(Ext.apply(baseConfig, {
			  store: store
				, params: params
				, readOnly: true
				, singleSelect: (p.multivalue === false)
		}));
		
		return field;
	}
	
	, createTreeField: function( baseConfig, executionInstance ) {
		
		var p = baseConfig.parameter;
		baseConfig.editable = false;
		
		
		var params = this.getBaseParams(p, executionInstance, 'complete');
		params.PARAMETERS = Sbi.commons.JSON.encode(this.getFormState());;
		params.LIGHT_NAVIGATOR_DISABLED = 'TRUE';
		
		
		field = new Sbi.widgets.TreeLookUpField(Ext.apply(baseConfig,{
			params: params, 
			allowInternalNodeSelection: p.allowInternalNodeSelection,
			service: this.services['getParameterValueForExecutionService']
		}));

		field.treeLoader.on('load', function(loader, node, response) {
			//fires after the sore is loaded: can apply
			this.firstLoadCounter++;
			this.fireEvent('checkReady', this);
		}, this);
		
		return field;
	}
	
	, createSliderField: function( baseConfig, executionInstance ) {
		
		Sbi.trace('[ParametersPanel.createSliderField] : IN');
		Sbi.trace('[ParametersPanel.createSliderField] : executionInstance [' + executionInstance + ']');
		
		/*ATTENTION:: keep this variable updated in all the create parameter depending by type ,
		 * that need to be loaded at first document execution (those loading a store)*/
		/*this.firstLoadTotParams ++;  this comment solves bug SPAGOBI-1685 "Document with single value slider doesn't work fine"
									   like the combobox the slider loads initially the store*/
		/*--------------------------------------------------------------------------------------*/
		
		var p = baseConfig.parameter;
		var store = this.createCompleteStore(p, executionInstance, 'simple');
		
		Sbi.trace('[ParametersPanel.createSliderField] : baseConfig.autoLoad is equal to [' + baseConfig.autoLoad + ']');
		
		// set slider width as 200 * colspan
		
		if(!p.colspan) p.colspan = 1;
		var sliderWidth = 200 * p.colspan;
		baseConfig.width  = sliderWidth;
		
		field = new Sbi.widgets.SliderField(Ext.apply(baseConfig, {
			multiSelect: p.multivalue,
			store :  store,
			displayField:'label',
			valueField:'value',
            tipText: function(slider, thumb){
            	var record = slider.store.getAt(thumb.value);
            	var value = record? record.get('label'): null;
                return String(thumb.value +  ' : ' + value);
            },
            constrainThumbs: false 
		}));
		
		Sbi.trace('[ParametersPanel.createSliderField] : OUT');
		
		return field;
	}
	
	, createComboField: function( baseConfig, executionInstance ) {
		
		var p = baseConfig.parameter;
		
		if(!p.colspan) p.colspan = 1;
		var comboWidth = 200 * p.colspan;
		baseConfig.width  = comboWidth;
		
		var store = this.createCompleteStore(p, executionInstance, 'simple');
		
		//on the load event, adds an empty value for the reset at the first 
		// position ONLY if the lov doesn't return an element with empty description
		if(this.addEmptyValueToCombo) {
			store.on('load', function(store, records, options) {
				
				var exist = false;
				for (i =0, l= records.length; i<l; i++ ){
					if (store.getAt(i).get('description') === '' ){
						exist = true;
						break;
					}
				}

				if (!exist){
					var emptyData = {
							value: '',
							label: '',
							description:'Empty value'
						};
				
					var emptyId =  store.getTotalCount()+1;
					var r = new store.recordType(emptyData, emptyId); // create new record
					store.insert(0, r);
				}
			}, this);
		}
		
		
		field = new Ext.ux.Andrie.Select(Ext.apply(baseConfig, {
			multiSelect: p.multivalue
			//, minLength:2
			, editable  : false			    
			, forceSelection : false
			, store :  store
			, displayField:'label'
			, valueField:'value'
			, emptyText: ''
			, typeAhead: false
			//, typeAheadDelay: 1000
			, triggerAction: 'all'
			, selectOnFocus:true
			, autoLoad: false
			, xtype : 'combo'
			, listeners: {
			    'select': {
			       	fn: function(){	}
			       	, scope: this
			    }			    
			}
		}));
		
		return field;
	}

	, createListField: function( baseConfig, executionInstance ) {
		
		var p = baseConfig.parameter;
		/*ATTENTION:: keep this variable updated in all the create parameter depending by type ,
		 * that need to be loaded at first document execution (those loading a store)*/
		this.firstLoadTotParams ++;
		/*--------------------------------------------------------------------------------------*/
		
		var store = this.createCompleteStore(p, executionInstance, 'simple');
		
		if(p.multivalue) {	
			field = new Sbi.widgets.CheckboxField(Ext.apply(baseConfig, {
	           store : store
	           , displayField:'label'
			   , valueField:'value'
			   , parameterId: p.id
	        }));
		
		} else {
			field = new Sbi.widgets.RadioField(Ext.apply(baseConfig, {
		       store : store
		       , displayField:'label'
			   , valueField:'value'
			   , parameterId: p.id
		    }));
		}
		
		return field;
	}
		
	, createField: function( p, executionInstance ) {
	
		var field;
		
		var baseConfig = {
			multivalue: p.multivalue
	       , fieldLabel: p.label
	       , fieldDefaultLabel: p.label
		   , name : p.id
		   , width: this.baseConfig.fieldWidth
		   , allowBlank: !p.mandatory
		   , parameter: p
		   , colspan: p.colspan
		   , thickPerc: p.thickPerc
		   // do not load store if the right value is passed in the preferences. In this case infact the field will be not added to the parameters panel
		   // so it is not necessary to calculate all its values
		   , autoLoad: !this.parameterValueIsPassedFromMenu(p) 
		};
		
		var labelStyle = '';
		labelStyle += (p.mandatory === true)?'font-weight:bold;': '';
		labelStyle += (p.dependencies.length > 0)?'font-style: italic;': '';
		labelStyle += 'width: '+this.baseConfig.fieldLabelWidth+'px;';
		baseConfig.labelStyle = labelStyle;
		
		if((this.mandatoryFieldAdditionalString!=null && this.mandatoryFieldAdditionalString!=undefined) && p.mandatory === true ){
			if(baseConfig.fieldDefaultLabel!=undefined && baseConfig.fieldDefaultLabel!=null){
				baseConfig.fieldDefaultLabel =  baseConfig.fieldDefaultLabel+' *';
			}
			if(baseConfig.fieldLabel!=undefined && baseConfig.fieldLabel!=null){
				baseConfig.fieldLabel =  baseConfig.fieldLabel+' *';
			}
		}
		
		if(p.type === 'DATE' && p.selectionType !== 'MAN_IN') {		
			field = this.createDateField( baseConfig, this.executionInstance );
		} else if(p.selectionType === 'LIST') {
			var baseParams = {};
			Ext.apply(baseParams, this.executionInstance);
			Ext.apply(baseParams, {
				PARAMETER_ID: p.id
				, MODE: 'simple'
				, OBJ_PARAMETER_IDS: p.objParameterIds  // ONly in massive export case
			});
			delete baseParams.PARAMETERS;
			var store = this.createStore();
			store.baseParams  = baseParams;
			
			store.on('beforeload', function(store, o) {
				Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : IN');
				var p = Sbi.commons.JSON.encode(this.getFormState());
				Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : form state [' + p + ']');
				o.params = o.params || {};
				o.params.PARAMETERS = p;
				Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : OUT');
				return true;
			}, this);
			
			if(p.multivalue) {	
				field = new Sbi.widgets.CheckboxField(Ext.apply(baseConfig, {
		           store : store
		           , displayField:'label'
				   , valueField:'value'
				   , parameterId: p.id
		        }));
			
			} else {
				field = new Sbi.widgets.RadioField(Ext.apply(baseConfig, {
			       store : store
			       , displayField:'label'
				   , valueField:'value'
				   , parameterId: p.id
			    }));
			}
		} else if(p.selectionType === 'COMBOBOX') {
			field = this.createComboField( baseConfig, this.executionInstance );
		} else if(p.selectionType === 'TREE'){
			field = this.createTreeField( baseConfig, this.executionInstance );
		} else if(p.selectionType === 'LOOKUP') {
			field = this.createLookupField( baseConfig, this.executionInstance );	
		} else if(p.selectionType === 'SLIDER') { 
			field = this.createSliderField( baseConfig, this.executionInstance );
		} else { 
//			if(p.type === 'DATE' || p.type ==='DATE_DEFAULT') {		
//				baseConfig.format = Sbi.config.localizedDateFormat;
//				field = new Ext.form.DateField(baseConfig);
//				if(p.type ==='DATE_DEFAULT') {
//					field.setValue(new Date());
//				}		
//			} else {
				if (p.enableMaximizer) {
					field = new Sbi.execution.LookupFieldWithMaximize(baseConfig);
				} else {
					field = new Ext.form.TextField(baseConfig);
				}	
//			}			
		}
		
		if(!field) {
			alert('Impossible to create a field of type [' + p.type + ']');
			return;
		}
		
		field.behindParameter = p;
		field.dependencies = p.dependencies;
		
		// add information: objParameterIds if present (massive export case)
		if(p.objParameterIds){
			field.objParameterIds = p.objParameterIds;	
		}
		
		return field;
	}
	
	, createCompleteStore: function(p, executionInstance, mode) {
		var store = this.createStore();
		Sbi.trace('[ParametersPanel.createCompleteStore] : executionInstance [' + executionInstance + ']');
		store.baseParams  = this.getBaseParams(p, executionInstance, mode);
		store.on('beforeload', function(store, o) {
			Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : IN');
			var p = Sbi.commons.JSON.encode(this.getFormState());
			//alert('[ParametersPanel.onBeforeStoreLoad] : form state [' + p + ']');
			Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : form state [' + p + ']');
			o.params = o.params || {};
			o.params.PARAMETERS = p;
			Sbi.trace('[ParametersPanel.onBeforeStoreLoad] : OUT');
			return true;
		}, this);
		
		return store;
		// this.createCompleteStore(p, this.executionInstance, 'simple');
	}
	
	, getBaseParams: function(p, executionInstance, mode) {
		var baseParams = {};
		Sbi.trace('[ParametersPanel.getBaseParams] : executionInstance [' + executionInstance + ']');
		
		Ext.apply(baseParams, executionInstance);
		Ext.apply(baseParams, {
			PARAMETER_ID: p.id
			, MODE: mode || 'simple'
			, OBJ_PARAMETER_IDS: p.objParameterIds  // Only in massive export case
		});
		delete baseParams.PARAMETERS;
		
		return baseParams;
		// store.baseParams  = this.getBaseParams(p, this.executionInstance);
	}
	
	, createStore: function() {
		var store;
		
		store = new Ext.data.JsonStore({
			url: this.services['getParameterValueForExecutionService']
		});
		
		store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		});
		
		store.on('load', function(store, records, options) {
			//fires after the sore is loaded: can apply 
			this.firstLoadCounter++;
			this.fireEvent('checkReady', this);
		}, this);

		return store;
		
	}
	
	// =====================================================================================
	// HELP messages functions
	// =====================================================================================
	
	// Help message on Parameters Panel.
	// work-around: since the panel toolbar may be to short, the message is injected with Ext.DomHelper.insertFirst on the body of
	// the panel, but a function for width calculation is necessary (this function does not work on page 3 when executing in
	// document browser with tree structure initially opened, since containerWidth is 0).
	// TODO: try to remove the on resize method and the width calculation
	, insertHelpMessage: function() {
		
		if (this.messageElement == undefined && this.rendered && (this.drawHelpMessage == true)) {
			var containerWidth = this.getInnerWidth();
			this.widthDiscrepancy = Ext.isIE ? 1 : 5;
			var initialWidth = containerWidth > this.formWidth ? containerWidth - this.widthDiscrepancy: this.formWidth;
			
			var message = this.getHelpMessage(this.executionInstance, this.thereAreParametersToBeFilled());
			
			this.messageElement = Ext.DomHelper.insertFirst(this.body, 
					'<div style="font-size: 12px; font-family: tahoma,verdana,helvetica; margin-bottom: 14px; color: rgb(24, 18, 241);' 
					+ (containerWidth === 0 ? '' : 'width: ' + initialWidth + 'px;') + '"'  
					+ ' class="x-panel-tbar x-panel-tbar-noheader x-toolbar x-panel-tbar-noborder x-btn-text x-item-disabled">'
					+ message
					+ '</div>');
			this.on('resize', function() {
				var containerWidth = this.getInnerWidth();
				this.messageElement.style.width = containerWidth > this.formWidth ? containerWidth - this.widthDiscrepancy: this.formWidth;
			}, this);
		}
	} 
	
	, getHelpMessage: function(executionInstance, thereAreParametersToBeFilled) {
		if (this.baseConfig.pageNumber === 2) {
			return this.getHelpMessageForPage2(executionInstance, thereAreParametersToBeFilled);
		} else {
			return this.getHelpMessageForPage3(executionInstance, thereAreParametersToBeFilled);
		}
	}
	
	, getHelpMessageForPage2: function(executionInstance, thereAreParametersToBeFilled) {
		var toReturn = null;
		var doc = executionInstance.document;
		if (doc.typeCode == 'DATAMART' && this.baseConfig.subobject == undefined) {
			if (Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality')) {
				if (!thereAreParametersToBeFilled) {
					toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.powerUserMessageWithoutParameters');
				} else {
					toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.powerUserMessageWithParameters');
				}
			} else {
				if (!thereAreParametersToBeFilled) {
					toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.readOnlyUserMessageWithoutParameters');
				} else {
					toReturn = LN('sbi.execution.parametersselection.message.page2.qbe.readOnlyUserMessageWithParameters');
				}
			}
		} else {
			if (!thereAreParametersToBeFilled) {
				toReturn = LN('sbi.execution.parametersselection.message.page2.execute');
			} else {
				toReturn = LN('sbi.execution.parametersselection.message.page2.fillFormAndExecute');
			}
		}
		return toReturn;
	}
	
	, getHelpMessageForPage3: function(executionInstance, thereAreParametersToBeFilled) {
		var toReturn = null;
		if (!thereAreParametersToBeFilled) {
			toReturn = LN('sbi.execution.parametersselection.message.page3.refresh');
		} else {
			toReturn = LN('sbi.execution.parametersselection.message.page3.fillFormAndRefresh');
		}
		return toReturn;
	}
	
	// =====================================================================================
	// MEMENTO functions
	// =====================================================================================
	
	, setValueFromMemento: function(f, moveDown) {
		if(!f.memento) return;
		
		if(moveDown) {
			f.memento.readCursor--;
		} else {
			f.memento.readCursor++;
		}
		
		var lastStateIndex = f.memento.size - 1;
		if(f.memento.readCursor < 0) {
			f.memento.readCursor = lastStateIndex; 
		}
		else if(f.memento.readCursor > lastStateIndex) {
			f.memento.readCursor = 0;
		}
		
		var state;
				
		state = f.memento.states[f.memento.readCursor];
		
		f.setValue( state.value );
		var fieldDescription = f.name + '_field_visible_description';
		var rawValue = state.description;
		if (state.description !== undefined && state.description != null && f.rendered === true) {
			f.setRawValue( state.description );
			this.updateDependentFields( f );
		}		
	}
	
	, isMassiveExportContext: function () {
		return (this.contest != undefined && this.contest == 'massiveExport');
	}
	
});