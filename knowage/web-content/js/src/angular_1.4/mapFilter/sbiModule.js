var sbiM=angular.module('sbiModule',[]);
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

sbiM.service('sbiModule_restServices', function($http, sbiModule_config, sbiModule_logger) {
	var alteredContextPath=null;

	this.alterContextPath=function(cpat){
		alteredContextPath=cpat;
	};

	function getBaseUrl(endP_path) {
		var burl= alteredContextPath==null? sbiModule_config.contextName +'/api/'+ endP_path+"/"  : alteredContextPath+ "" + endP_path+"/" 
				alteredContextPath=null;
		return burl ;
	};

	this.get = function(endP_path, req_Path, item, conf) {
		(item == undefined || item==null) ? item = "" : item = "?" + encodeURIComponent(item).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
		sbiModule_logger.trace("GET: " +endP_path+"/"+ req_Path + "" + item, conf);
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