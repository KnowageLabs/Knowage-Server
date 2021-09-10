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

var sbiM=angular.module('sbiModule',['toastr','ngSanitize']);
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

sbiM.service('sbiModule_messaging',function(toastr,sbiModule_restServices,$mdToast){
	this.showErrorMessage = function(msg,title){

//		toastr.error(msg,title, {
//			  closeButton: true
//		});
		sbiModule_restServices.errorHandler(msg,title);
	};
	this.showWarningMessage = function(msg,title,hideTimeout){

//		toastr.warning(msg,title, {
//			  closeButton: true
//		});
		var timeout = 3000;

		if (hideTimeout && typeof hideTimeout == "number") {
			timeout = hideTimeout;
		}

		return	$mdToast.show(
					$mdToast
					.simple()
					.content(msg)
					.position('top')
					.action('OK')
					.highlightAction(false)
					.hideDelay(timeout)
				);
	};
	this.showInfoMessage = function(msg,title,hideTimeout){

//		toastr.info(msg,title, {
//			  closeButton: true
//		});

		var timeout = 3000;

		if (hideTimeout && typeof hideTimeout == "number") {
			timeout = hideTimeout;
		}

		return	$mdToast.show(
					$mdToast
					.simple()
					.content(msg)
					.position('top')
					.action('OK')
					.highlightAction(false)
					.hideDelay(timeout)
				);

	};

	this.showSuccessMessage = function(msg,title,hideTimeout){

//		toastr.success(msg,title, {
//			  closeButton: true
//			});
		var timeout = 3000;

		if (hideTimeout && typeof hideTimeout == "number") {
			timeout = hideTimeout;
		}

		return	$mdToast.show(
					$mdToast
					.simple()
					.content(msg)
					.position('top')
					.action('OK')
					.highlightAction(false)
					.hideDelay(timeout) // changed by: danristo (previous value: 60000)
				);
	};

});


sbiM.service('sbiModule_restServices', function($http, sbiModule_config,sbiModule_logger,$mdDialog,$q,sbiModule_translate) {
	var alteredContextPath=null;

	this.alterContextPath=function(cpat){
		alteredContextPath=cpat;
	}

	this.restToRootProject=function(){
		alteredContextPath=sbiModule_config.externalBasePath+"restful-services";
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

	/*
	NEW METHODS
	*/

	var genericErrorHandling = function(data, status, headers, config, deferred) {
  		deferred.reject(data, status, headers, config);
	};

	var handleResponse = function(data, status, headers, config, deferred) {
		if(data.data != null){
			if ( data.data.hasOwnProperty("errors")) {

				genericErrorHandling(data, status, headers, config, deferred);
			} else {
				deferred.resolve(data, status, headers, config);
			}
		}else{
			if ( data.status == 201) {
				deferred.resolve(data, status, headers, config);

			} else {
				genericErrorHandling(data, status, headers, config, deferred);
			}

		}

	};

	// SAMPLE METHOD, this will be the implementation
	this.promiseGet = function(endP_path, req_Path, item, conf) {
		var deferred = $q.defer();

		// Required for passing JSON on a GET request
		if (item == undefined || item==null) {
			item = "";
		}else {
			item = "?" +
				encodeURIComponent(item)
				.replace(/'/g,"%27")
				.replace(/"/g,"%22")
				.replace(/%3D/g,"=")
				.replace(/%26/g,"&");
		}

		sbiModule_logger.trace("GET: " +endP_path+"/"+ req_Path + "" + item, conf);

		deferred.notify('About to call async function');

		$http.get(getBaseUrl(endP_path) + "" + req_Path + "" + item, conf)
			.then(
					function successCallback(data, status, headers, config) {
						handleResponse(data, status, headers, config, deferred);
				  	},
				  	function errorCallback(data, status, headers, config) {
				  		genericErrorHandling(data, status, headers, config, deferred);
				  	}
			);

		return deferred.promise;
	};

	this.promisePost = function(endP_path, req_Path, item, conf) {
		var deferred = $q.defer();

		sbiModule_logger.trace("POST: " +endP_path+"/"+ req_Path + "" + item, conf);

		deferred.notify('About to call async function');

		$http.post(getBaseUrl(endP_path) + "" + req_Path , item, conf)
			.then(
					function successCallback(data, status, headers, config) {
						handleResponse(data, status, headers, config, deferred);
				  	},
				  	function errorCallback(data, status, headers, config) {
				  		genericErrorHandling(data, status, headers, config, deferred);
				  	}
			);

		return deferred.promise;
	};

	this.promisePut = function(endP_path, req_Path, item, conf) {
		var deferred = $q.defer();

		sbiModule_logger.trace("PUT: " +endP_path+"/"+ req_Path + "" + item, conf);

		deferred.notify('About to call async function');

		$http.put(getBaseUrl(endP_path) + "" + req_Path , item, conf)
			.then(
					function successCallback(data, status, headers, config) {
						handleResponse(data, status, headers, config, deferred);
				  	},
				  	function errorCallback(data, status, headers, config) {
				  		genericErrorHandling(data, status, headers, config, deferred);
				  	}
			);

		return deferred.promise;
	};

	this.promiseDelete = function(endP_path, req_Path, item, conf) {
		var deferred = $q.defer();

		sbiModule_logger.trace("DELETE: " +endP_path+"/"+ req_Path + "" + item, conf);

		deferred.notify('About to call async function');
		(item == undefined || item==null) ? item = "" : item = "?" + encodeURIComponent(item).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&");

		$http.delete(getBaseUrl(endP_path) + "" + req_Path+""+item, conf)
			.then(
					function successCallback(data, status, headers, config) {
						handleResponse(data, status, headers, config, deferred);
				  	},
				  	function errorCallback(data, status, headers, config) {
				  		genericErrorHandling(data, status, headers, config, deferred);
				  	}
			);

		return deferred.promise;
	};


	this.errorHandler=function(text,title){
		var titleFin=sbiModule_translate.load(title) || "";
		var textFin=text;
		if(angular.isObject(text)){
			if(text.hasOwnProperty("errors")){
				textFin="";
				for(var i=0;i<text.errors.length;i++){
					textFin+=sbiModule_translate.load(text.errors[i].message)+" <br> ";
				}
			}else if(text.hasOwnProperty("data")){
				textFin=text.data;
			}else{
				textFin=sbiModule_translate.load(JSON.stringify(text));
			}
		}else{
			textFin=sbiModule_translate.load(text);
		}

		var alert = $mdDialog.alert()
		.title(titleFin)
		.htmlContent(textFin)
		.ariaLabel('error')
		.ok('OK')
		return $mdDialog.show(alert); //can use the finally function
	}


});


sbiM.directive('restLoading',   ['$http' ,function ($http)
                              	{
                              	    return {
                              	        template:"<div loading layout-fill style='position:fixed;z-index: 500;background:rgba(0,0,0, 0.3);'>"+
                              	    	"<md-progress-circular  md-mode='indeterminate' style='top:50%;left:50%' ></md-progress-circular></div>",
                              	        link: function (scope, elm, attrs)
                              	        {
                              	            scope.$watch(function () {
                              	                return $http.pendingRequests.length > 0;
                              	            }, function (v)
                              	            {
                              	                if(v){
                              	               	 elm.css("display","block");
                              	                }else{
                              	               	 elm.css("display","none");
                              	                }
                              	            });
                              	        }
                              	    };
                              	}]);