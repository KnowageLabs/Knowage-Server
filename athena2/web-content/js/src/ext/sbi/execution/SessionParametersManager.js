/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * Object name 
  * 
  * Singleton object that handle all errors generated on the client side
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


Ext.ns("Sbi.execution");

Sbi.execution.SessionParametersManager = function() {
	
    // private variables
	var STORE_NAME = 'Sbi_execution_SessionParametersManager'; // dots are not allowed in the store name by the Persist library
	
	var PARAMETER_STATE_OBJECT_KEY = 'parameterState'; 
	var PARAMETER_MEMENTO_OBJECT_KEY = 'parameterMemento';
	
	
	// configurations
	var isStatePersistenceEnabled = Sbi.config.isParametersStatePersistenceEnabled;
	var statePersistenceScope = Sbi.config.parameterStatePersistenceScope; // = 'SESSION' ||  'BROWSER'
	var isMementoPersistenceEnabled = Sbi.config.isParametersMementoPersistenceEnabled;
	var mementoPersistenceScope = Sbi.config.parameterMementoPersistenceScope; // = 'SESSION' ||  'BROWSER'
	var mementoPersistenceDepth = Sbi.config.parameterMementoPersistenceDepth; // how many states for each parameter the memento object must store

	// public space
	return {

		init: function() {
			
			isStatePersistenceEnabled = Sbi.config.isParametersStatePersistenceEnabled;
			statePersistenceScope = Sbi.config.parameterStatePersistenceScope; // = 'SESSION' ||  'BROWSER'
			isMementoPersistenceEnabled = Sbi.config.isParametersMementoPersistenceEnabled;
			mementoPersistenceScope = Sbi.config.parameterMementoPersistenceScope; // = 'SESSION' ||  'BROWSER'
			mementoPersistenceDepth = Sbi.config.parameterMementoPersistenceDepth; // how many states for each parameter the memento object must store
			
			if(isStatePersistenceEnabled === undefined) isStatePersistenceEnabled = false;
			if(statePersistenceScope != 'BROWSER' && statePersistenceScope != 'SESSION') statePersistenceScope = 'SESSION';
			if(isMementoPersistenceEnabled === undefined) isMementoPersistenceEnabled = true;
			if(mementoPersistenceScope != 'BROWSER' && mementoPersistenceScope != 'SESSION') mementoPersistenceScope = 'BROWSER';
			if(mementoPersistenceDepth === undefined) mementoPersistenceDepth = 5;
			
			try {
				if (isStatePersistenceEnabled || isMementoPersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store = new Persist.Store(STORE_NAME, {
					      swf_path: Sbi.config.contextName + '/js/lib/persist-0.1.0/persist.swf'
				    });
				}
			} catch (err) {}
		}
		
		/**
		 * restores the state of all parameters used in the input parameters panel
		 * The input parametersPanel is an instance of class Sbi.execution.ParametersPanel
		 */
		, restoreStateObject: function(parametersPanel) {
			try {
				if (isStatePersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_STATE_OBJECT_KEY, function(ok, value) {
						if (ok && value !== undefined && value !== null) {
							var storedParameters = Sbi.commons.JSON.decode(value);
							var state = {};
							for(var p in parametersPanel.fields) {
								var field = parametersPanel.fields[p];
																
								if (!field.isTransient) {
									var parameterStateObject = storedParameters[Sbi.execution.SessionParametersManager.getParameterStorageKey(field)];
									if (parameterStateObject && parameterStateObject.value) {
										state[field.name] = parameterStateObject.value;
										if (parameterStateObject.description) {
											state[field.name + '_field_visible_description'] = parameterStateObject.description;
										}
									}
								}
							}
							parametersPanel.setFormState(state);
						}
					});
				}
			} catch (err) {}
		}
		
		/**
		 * restores the memento of all parameters used in the input parameters panel
		 * The input parametersPanel is an instance of class Sbi.execution.ParametersPanel
		 */
		, restoreMementoObject: function(parametersPanel) {
			try {
				if (isMementoPersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_MEMENTO_OBJECT_KEY, function(ok, value) {
						if (ok && value !== undefined && value !== null) {
							//alert(value);
							
							var mementoObject = Sbi.commons.JSON.decode(value);
							
							var state = {};
							for(var p in parametersPanel.fields) {
								var field = parametersPanel.fields[p];
																
								if (!field.isTransient) {
									var parameterMementoObject = mementoObject[Sbi.execution.SessionParametersManager.getParameterStorageKey(field)];
									if(parameterMementoObject) {
										parameterMementoObject.readCursor = parameterMementoObject.insertCursor;
									}
									field.memento = parameterMementoObject;
								}
							}							
						}
					});
				}
			} catch (err) {}
		}
		
		
		/**
		 * saves the state of all parameters used in the input parameters panel
		 * The input parametersPanel is an instance of class Sbi.execution.ParametersPanel
		 */
		, saveStateObject: function(parametersPanel) {
			try {
				if (isStatePersistenceEnabled) {
					for (var p in parametersPanel.fields) {
						var field = parametersPanel.fields[p];
						if (!field.isTransient) {
							Sbi.execution.SessionParametersManager.saveParameterState(field);
						}
					}
				}
			} catch (err) {}
		}
		
		/**
		 * saves a parameter state
		 * The input field is a field belonging to class Sbi.execution.ParametersPanel
		 */
		, saveParameterState: function(field) {
			try {
				if (isStatePersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_STATE_OBJECT_KEY, function(ok, value) {
						if (ok) {
							var storedParameters = null;
							if (value === undefined || value === null) {
								storedParameters = {};
							} else {
								storedParameters = Sbi.commons.JSON.decode(value);
							}
							var fieldValue = field.getValue();
							if (fieldValue === undefined || fieldValue === null || fieldValue === '' || fieldValue.length === 0) {
								Sbi.execution.SessionParametersManager.clear(field);
							} else {
								var parameterStateObject = {};
								parameterStateObject.value = fieldValue;
								var rawValue = field.getRawValue();
								if (rawValue !== undefined) {
									parameterStateObject.description = rawValue;
								}
								storedParameters[Sbi.execution.SessionParametersManager.getParameterStorageKey(field)] = parameterStateObject;
								Sbi.execution.SessionParametersManager.store.set(PARAMETER_STATE_OBJECT_KEY, Sbi.commons.JSON.encode(storedParameters));
							}
						}
					});
				}
			} catch (err) {}
		}
		
		/**
		 * update the memento object with the values used in the input parameters panel
		 * The input parametersPanel is an instance of class Sbi.execution.ParametersPanel
		 */
		, updateMementoObject: function(parametersPanel) {
			try {
				if (isMementoPersistenceEnabled) {
					for (var p in parametersPanel.fields) {
						var field = parametersPanel.fields[p];
						if (!field.isTransient) {
							if(field.behindParameter.typeCode == 'MAN_IN' && field.behindParameter.type === 'STRING'){
								Sbi.execution.SessionParametersManager.updateParameterMemento(field);
							}
						}
					}
				}
			} catch (err) {}
			
			
			//Sbi.execution.SessionParametersManager.debug();
		}
		
		/**
		 * update a parameter memento
		 * The input field is a field belonging to class Sbi.execution.ParametersPanel
		 */
		, updateParameterMemento: function(field) {
			try {
				if (isMementoPersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_MEMENTO_OBJECT_KEY, function(ok, value) {
						if (ok) {
							var mementoObject = null;
							if (value === undefined || value === null) {
								mementoObject = {};
							} else {
								mementoObject = Sbi.commons.JSON.decode(value);
							}
							
							var fieldValue = field.getValue();
							if (fieldValue === undefined || fieldValue === null || fieldValue === '' || fieldValue.length === 0) {
								// do nothing
							} else {
								
								var parameterStorageKey = Sbi.execution.SessionParametersManager.getParameterStorageKey(field);
								
								var parameterMementoObject = mementoObject[parameterStorageKey];
								if(!parameterMementoObject) {
									var parameterMementoObject = {};
									parameterMementoObject.size = 0;
									parameterMementoObject.insertCursor = -1;
									parameterMementoObject.states = new Array(mementoPersistenceDepth);
									parameterMementoObject.distinctValues = {};
								}
																
								var state = {};
								state.value = fieldValue;
								var rawValue = field.getRawValue();
								if (rawValue !== undefined) {
									state.description = rawValue;
								}
								
								if(!parameterMementoObject.distinctValues[state.value]) {
									parameterMementoObject.insertCursor = (parameterMementoObject.insertCursor + 1)%5;
									
									var removedState = parameterMementoObject.states[parameterMementoObject.insertCursor];
									parameterMementoObject.states[parameterMementoObject.insertCursor] = state;
									
									
									if(parameterMementoObject.size < mementoPersistenceDepth) {
										parameterMementoObject.distinctValues[state.value] = true;
										parameterMementoObject.size++;
									} else {
										delete parameterMementoObject.distinctValues[removedState.value];
										//alert('removed value: ' + removedState.value + '; removed status:' + (!parameterMementoObject.distinctValues[removedState.value]));
									}

									mementoObject[parameterStorageKey] = parameterMementoObject;
									Sbi.execution.SessionParametersManager.store.set(PARAMETER_MEMENTO_OBJECT_KEY, Sbi.commons.JSON.encode(mementoObject));
									
									parameterMementoObject.readCursor = parameterMementoObject.insertCursor;
									field.memento = parameterMementoObject;
									//alert(field.memento.toSource());
								} else {
									//alert('alredy in');
								}
							}
						}
					});
				}
			} catch (err) {}
		}
		
		
		, debug: function(field) {
			try {
				if (isMementoPersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_MEMENTO_OBJECT_KEY, function(ok, value) {
						if (ok) {
							alert(value);
						}
					});
				}
			} catch (err) {}
		}
		
		
		
		/**
		 * clears a stored parameter
		 * The input field is a field belonging to class Sbi.execution.ParametersPanel
		 */
		, clear: function(field) {
			try {
				if (isStatePersistenceEnabled) {
					Sbi.execution.SessionParametersManager.store.get(PARAMETER_STATE_OBJECT_KEY, function(ok, value) {
						if (ok) {
							var storedParameters = Sbi.commons.JSON.decode(value);
							if (storedParameters !== undefined && storedParameters !== null) {
								delete storedParameters[Sbi.execution.SessionParametersManager.getParameterStorageKey(field)];
							}
							Sbi.execution.SessionParametersManager.store.set(PARAMETER_STATE_OBJECT_KEY, Sbi.commons.JSON.encode(storedParameters));
						}
					});
				}
			} catch (err) {}
		}
		
		/**
		 * resets all stored parameters
		 */
		, resetSessionObjects: function() {
			try {
				if (isStatePersistenceEnabled && statePersistenceScope === 'SESSION') {
					Sbi.execution.SessionParametersManager.store.set(PARAMETER_STATE_OBJECT_KEY, Sbi.commons.JSON.encode({}));
				}
				
				if (isMementoPersistenceEnabled && mementoPersistenceScope === 'SESSION') {
					Sbi.execution.SessionParametersManager.store.set(PARAMETER_MEMENTO_OBJECT_KEY, Sbi.commons.JSON.encode({}));
				}
			} catch (err) {}
		}
		
		/**
		 * internal utility method that returns the key that will be used in order to store the parameter state.
		 * The key is composed by the following information retrieved by the parameter that stands behind the input field:
		 * - label of the parameter
		 * - id of the parameter use mode (in order to avoid that parameters with the same labels but different modalities conflict)
		 */
		, getParameterStorageKey: function(field) {
			try {
			var parameterStorageKey = field.behindParameter.label + '_' + field.behindParameter.parameterUseId;
			return parameterStorageKey;
			} catch (err) {}
		}
		
	};
	
}();