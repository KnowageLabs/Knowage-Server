var Restapp = angular.module('angular_rest', []);
Restapp.constant('ENDPOINT_URI', 'http://' + window.parent.url.host + ':' + window.parent.url.port+"/"+window.parent.url.contextPath
		+ '/restful-services/');

Restapp.service('restServices', function($http, ENDPOINT_URI) {

	var service = this;
	var path = "1.0/glossary";

	var alteredContextPath=null;

	service.alterContextPath=function(cpat){
		alteredContextPath= 'http://' + window.parent.url.host + ':' + window.parent.url.port+"/"+cpat+ '/restful-services/';
	}

	function getBaseUrl(endP_path) {
		endP_path == undefined ? endP_path = path : true;
		return alteredContextPath==null? ENDPOINT_URI + endP_path + "/" : alteredContextPath + endP_path + "/"
		
	}
	;

	service.get = function(endP_path, req_Path, item) {
		
		item == undefined ? item = "" : item = "?" + encodeURIComponent(item).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");
		console.log("GET: "+getBaseUrl(endP_path) + "" + req_Path + "" + item);
		return $http.get(getBaseUrl(endP_path) + "" + req_Path + "" +item);
	};
	service.get_item = function(endP_path, req_Path, item){
		console.log("GET2");
		console.log(item);
		return $http.get(getBaseUrl(endP_path) + "" + req_Path + "", item);
	}
	service.remove = function(endP_path, req_Path, item) {
		item == undefined ? item = "" : item = "?" + item;
		console.log("REMOVE: "+getBaseUrl(endP_path) + "" + req_Path + "" + item);
		return $http.post(getBaseUrl(endP_path) + "" + req_Path + "" + item);
	};

	service.post = function(endP_path, req_Path, item, conf) {
		console.log("POST: "+getBaseUrl(endP_path) + "" + req_Path);
		console.log(item);
		return $http.post(getBaseUrl(endP_path) + "" + req_Path, item, conf);
	};
	
	service.put = function(endP_path, req_Path, item, conf) {
		console.log("PUT: "+getBaseUrl(endP_path) + "" + req_Path);
		console.log(item);
		return $http.put(getBaseUrl(endP_path) + "" + req_Path, item, conf);
	};
	
	service.delete = function(endP_path, req_Path) {
		console.log("PUT: "+getBaseUrl(endP_path) + "" + req_Path);
		console.log(item);
		return $http.delete(getBaseUrl(endP_path) + "" + req_Path);
	};

	// prendo i nodi di un glossario

	service.getGlossNode = function(glossID, nodeID) {
		console.log(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID)
		return $http.get(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID);
	};

});