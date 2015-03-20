/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
Ext.ns("Sbi.logging");

// in ie if the console is not open console object is undefined
if (typeof console === "undefined") { 
	console = { log: function () { } }
};

Sbi.logging.enabled = true;

Sbi.logging.level = "TRACE";

Sbi.logging.levels = {
	"TRACE": 1
	, "DEBUG": 2
	, "INFO": 3
	, "WARN": 4
	, "ERROR": 5
};

Sbi.log = function(level, msg) {
	if(Sbi.logging.enabled 
		&& Sbi.logging.levels[level] >= Sbi.logging.levels[Sbi.logging.level]) {
	
		if(!Ext.isIE && console) {
			console.log('[' + level + '] : ' + msg);
		}
	}
};

Sbi.trace = function(msg) {
	Sbi.log("TRACE", msg);
};

Sbi.debug = function(msg) {
	Sbi.log("DEBUG", msg);
};

Sbi.info = function(msg) {
	Sbi.log("INFO", msg);
};

Sbi.warn = function(msg) {
	Sbi.log("WARN", msg);
};

Sbi.error = function(msg) {
	Sbi.log("ERROR", msg);
};

Sbi.toSource = function(o) {
	return Ext.util.JSON.encode(o);
};