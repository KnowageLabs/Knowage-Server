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