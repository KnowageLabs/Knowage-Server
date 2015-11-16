/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi");

Sbi.isValorized = function(o, subProperties) {
	var isValorized = Ext.isDefined(o) && Sbi.isNotNull(o);
	if(isValorized && subProperties) {
		if(Ext.isString( subProperties ) ) {
			subProperties = subProperties.split(".");
		}
		if(subProperties.length > 0) {
			var property = subProperties.shift();
			isValorized = Sbi.isValorized(o[property], subProperties);
		}
	}
	return isValorized;
};

Sbi.isNotValorized = function(o, subProperties) {
	return !Sbi.isValorized(o, subProperties);
};

Sbi.isNull = function(o) {
	return o === null;
};

Sbi.isNotNull = function(o) {
	return !Sbi.isNull(o);
};

Sbi.isExtObject = function(o) {
	var objectClass = Ext.ClassManager.getClass(o);
	if(objectClass != null) {
		Sbi.trace("[Sbi.isExtObject]: type of: " + Ext.ClassManager.getName(o));
	}
	return objectClass != null;
//	Sbi.trace("[Sbi.isExtObject]: Is an insance of [Ext.data.Store]: " + (o instanceof Ext.data.Store));
//	Sbi.trace("[Sbi.isExtObject]: Is an insance of [Ext.util.Observable]: " + (o instanceof Ext.util.Observable));
//	return typeof o === 'object' && (o instanceof Ext.util.Observable);
};

Sbi.isNotExtObject = function(o) {
	return !Sbi.isExtObject(o);
};


Sbi.getObjectSettings = function(objectQilifiedName, defaultSettings) {
	var settings = null;

	if( Sbi.isValorized(objectQilifiedName) && Ext.isString(objectQilifiedName)) {
		var nameParts = objectQilifiedName.split(".");
		if(nameParts.length > 0 && nameParts[0] === "Sbi") {
			nameParts.shift(); // remove the first element
			nameParts.unshift("Sbi", "settings");
		}
		if(nameParts.length > 2) { // it's a real object not just general settings
			var objectName = nameParts.pop();
			objectName = objectName.charAt(0).toLowerCase() + objectName.slice(1);
			nameParts.push(objectName);
		}

		settings = Sbi.getObjectByName(nameParts.join("."));
		if(settings) {
			settings = Ext.apply(defaultSettings || {}, settings);
		}
	}

	if(settings) {
		//Sbi.trace("[Sbi.getObjectSettings]: for object [" + objectQilifiedName + "] the following settings [" + Sbi.toSource(settings) + "] has been found");
	} else {
		//Sbi.trace("[Sbi.getObjectSettings]: No settings has been found for object [" + objectQilifiedName + "]");
	}

	if(!settings && defaultSettings) {
		settings = defaultSettings;
	}


	return settings;
};


Sbi.getObjectByName  = function(objectName) {

	//Sbi.trace("[Sbi.getObjectByName]: IN");

	if( Sbi.isNotValorized(objectName)) {
		Sbi.showErrorMessage("Input parameter [objectName] must be valorized");
		return null;
	}

	if( Ext.isString(objectName) === false) {
		Sbi.showErrorMessage("Input parameter [objectName] must be of type string");
		return null;
	}

	//Sbi.trace("[Sbi.getObjectByName]: Input parameter [objectName] is equal to [" + objectName + "]");

	var scope = window;
	var scopeStr = 'window'; // used by debug logs


	var namespace = objectName.split('.');
	objectName = namespace.pop();
	if(namespace.length > 0) {
		for(var i = 0; i< namespace.length; i++) {
			var o = scope[namespace[i]];
			if (typeof o === "object") {
				//Sbi.trace("[Sbi.getObjectByName]: Object [" + namespace[i] + "] found in scope [" + scopeStr + "]");
				scope = o;
				if(scopeStr === 'window') {
					scopeStr = namespace[i];
				} else {
					scopeStr += '.' + namespace[i];
				}
			} else {
				//Sbi.warn("Impossible to find an object named [" + namespace[i] + "] in scope [" + scopeStr + "]");
				return null;
			}
		}
	}

	//Sbi.trace("[Sbi.getObjectByName]: OUT");

	return scope[objectName];
};

Sbi.createObjectByClassName = function(fnName, fnArgs) {

	var output;

	Sbi.trace("[Sbi.execFunctionByName]: IN");

	Sbi.trace("[Sbi.createObjectByClassName]: function name is equal to [" + fnName + "]");
	if( Sbi.isNotValorized(fnName)) {
		Sbi.showErrorMessage("Input parameter [fnName] must be valorized");
		return null;
	}

	// find object
	var fn = Sbi.getObjectByName(fnName);

	// is object a function?
	if (typeof fn === "function") {
		Sbi.trace("Function [" + fnName + "] found in scope");
		output =  new fn(fnArgs);
		Sbi.trace("Function [" + fnName + "] sucesfully called in scope");
	} else {
		Sbi.showErrorMessage("Impossible to find a function named [" + fnName + "] in scope");
	}

	Sbi.trace("[Sbi.execFunctionByName]: OUT");

	return output;
};

Sbi.isEmptyObject = function(o) {
	for(var p in o) {
		if(o.hasOwnProperty(p)) {
			return false;
		}
	}
	return true;
};

Sbi.isNotEmptyObject = function(o) {
	Sbi.isEmptyObject(o) === false;
};

// store utils
Sbi.pivotStore = function(s) {
	var store = Ext.create('Ext.data.ArrayStore', {
	    // store configs
	    storeId: 'myStore',
	    // reader configs
	    fields: [
	       'company',
	       {name: 'price', type: 'float'},
	       {name: 'change', type: 'float'},
	       {name: 'pctChange', type: 'float'},
	       {name: 'lastChange', type: 'date', dateFormat: 'n/j h:ia'}
	    ]
	});

	var myData = [
	              ['3m Co',71.72,0.02,0.03,'9/1 12:00am'],
	              ['Alcoa Inc',29.01,0.42,1.47,'9/1 12:00am'],
	              ['Boeing Co.',75.43,0.53,0.71,'9/1 12:00am'],
	              ['Hewlett-Packard Co.',36.53,-0.03,-0.08,'9/1 12:00am'],
	              ['Wal-Mart Stores, Inc.',45.45,0.73,1.63,'9/1 12:00am']
	          ];
};
