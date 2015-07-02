var Restapp = angular.module('angular_rest', []);
Restapp.constant('ENDPOINT_URI', 'http://' + hostName + ':' + serverPort
		+ '/athena/restful-services/1.0/');

Restapp.service('restServices', function($http, ENDPOINT_URI) {

	var service = this;
	var path = "glossary";

	function getBaseUrl(endP_path) {
		endP_path == undefined ? endP_path = path : true;
		return ENDPOINT_URI + endP_path + "/";
	}
	;

	service.get = function(endP_path, req_Path, item) {
		item == undefined ? item = "" : item = "?" + item;
		console.log("service.get");
		console.log("endP_path= " + endP_path)
		console.log("req_Path=" + req_Path)
		console.log("item=" + item)
		return $http.get(getBaseUrl(endP_path) + "" + req_Path + "" + item);
	};

	service.remove = function(endP_path, req_Path, item) {
		item == undefined ? item = "" : item = "?" + item;
		return $http.post(getBaseUrl(endP_path) + "" + req_Path + "" + item);
	};

	service.post = function(endP_path, req_Path, item) {
		return $http.post(getBaseUrl(endP_path) + "" + req_Path, item);
	};

	// prendo i nodi di un glossario

	service.getGlossNode = function(glossID, nodeID) {
		console.log(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID)
		return $http.get(getBaseUrl() + "listContents?GLOSSARY_ID=" + glossID
				+ "&PARENT_ID=" + nodeID);
	};

});