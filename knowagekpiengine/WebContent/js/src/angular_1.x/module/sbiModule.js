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

var sbiM=angular.module('sbiModule',['ngMaterial']);
sbiM.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('indigo').accentPalette('blue-grey');
});

sbiM.factory('sbiModule_user',function(){

	var user={};

	return user;
});

sbiM.service('sbiModule_logger',function(){
	this.exec=true;
	this.log = function(val1,val2,val3){
		if(this.exec){
			console.log("[LOG] ",val1,(val2 || ""),(val3|| ""));
		}
	};

	this.trace = function(val1,val2,val3){
		if(this.exec){
			console.log("[TRACE] ",val1,(val2 || ""),(val3|| ""));
		}
	};

});

sbiM.service('sbiModule_translate', function() {
	this.addMessageFile = function(file){
		messageResource.load([file,"messages"], function(){});
	};

	this.load = function(key,sourceFile) {
		var sf= sourceFile == undefined? 'messages' : sourceFile;
		return messageResource.get(key, sf);
	};
});

sbiM.service('sbiModule_restServices', function($http, sbiModule_config,sbiModule_logger) {
	var alteredContextPath=null;

	this.alterContextPath=function(cpat){
		alteredContextPath=cpat;
	}

	function getBaseUrl(endP_path) {
		var burl= alteredContextPath==null? sbiModule_config.contextName +'/api/'+ endP_path+"/"  : alteredContextPath+ "" + endP_path+"/" 
				alteredContextPath=null;
		return burl ;
	};

	this.get = function(endP_path, req_Path, item, conf) {
		(item == undefined || item==null) ? item = "" : item = "?" + encodeURIComponent(item).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
		sbiModule_logger.trace("GET: " +endP_path+"/"+ req_Path + "" + item,conf);
		return $http.get(getBaseUrl(endP_path) + "" + req_Path + "" + item, conf);
	};

	this.remove = function(endP_path, req_Path, item, conf) {
		item == undefined ? item = "" : item = "?" + item;
		sbiModule_logger.trace("REMOVE: "+endP_path+"/"+req_Path + "" + item,conf);
		return $http.post(getBaseUrl(endP_path) + "" + req_Path + "" + item, conf);
	};

	this.post = function(endP_path, req_Path, item, conf) {
		sbiModule_logger.trace("POST: "+endP_path+"/"+ req_Path,item,conf);
		return $http.post(getBaseUrl(endP_path) + "" + req_Path, item, conf);
	};

	this.delete = function(endP_path, req_Path, item, conf) {
		(item == undefined || item==null) ? item = "" : item = "?" + encodeURIComponent(item).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
		sbiModule_logger.trace("PUT:" +endP_path+"/"+req_Path+ "" + item,conf);
		return $http.delete(getBaseUrl(endP_path) + "" + req_Path, conf);
	};

	this.put = function(endP_path, req_Path, item, conf) {
		sbiModule_logger.trace("PUT: "+endP_path+"/"+req_Path,item,conf);
		return $http.put(getBaseUrl(endP_path) + "" + req_Path, item, conf);
	};

});