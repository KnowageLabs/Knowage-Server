/**
 * @license
 * messageResource.js - v1.0
 * Copyright (c) 2014, Suhaib Khan
 * http://khansuhaib.wordpress.com
 *
 * messageResource.js is licensed under the MIT License.
 * http://www.opensource.org/licenses/mit-license.php
 */
 
 //For old version of Internet Explorer
 if(!String.prototype.trim) {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, '');
	}
}
 
/**
 * @author Suhaib Khan http://khansuhaib.wordpress.com
 * @version 1.0
 */
(function(global){
	'use strict';
	
	/**
	 * Module with methods for loading and using message resource
	 * property files in javascript.
	 * @module messageResource
	 */
	var messageResource = (function(){
		
		// private variables
		

		var properties, 
			// default module name if no module name specified
			DEFAULT_MODULE_NAME = '_default',
			// default message resource file extension.
			DEFAULT_EXTENSION = '.properties',
			filePath,
			fileExtension,
			defaultLocale = 'en_US',
			currentLocale = defaultLocale,
			fileNameResolver,
			ajaxFunction,
			// status flag for checking valid configuration.
			validConfiguration = false,
			debugMode = false;
		
		// private functions
		
		/**
		 * Default file name resolver.
		 * 
		 * @param {String} module - Module name.
		 * @param {String} locale - Locale identifier like en_US.
		 * @return {String} File name.
		 * @private
		 */
		function defaultFileNameResolver(module, locale){
			// by default locale is seperated by underscore
			return (locale && typeof locale === 'string') ? 
				module + '_' + locale : module;
		}

		/**
		 * Parse and save a message resource file to properties.
		 * 
		 * @param {String} text - Contents of message resource file.
		 * @param {String} module - A valid module name.
		 * @param {String} locale - A valid locale identifier.
		 * @private
		 */
		function saveFile(text, module, locale){
			var linesArray,
				curModuleMap;
			
			// number to string
			text = '' + text;
			
			if (!text){
				log('Invalid contents.');
				return;
			}
			
			// append locales and modules if not exists
			properties = properties || {};
			properties[locale] = properties[locale] || {};
			properties[locale][module] = properties[locale][module] || {};
			
			// set current reading module
			curModuleMap = properties[locale][module];
			
			// split text by newline
			linesArray = text.split('\n');
			if (linesArray){
				// split each line to key-value pair and
				// save in properties
				linesArray.forEach(function (line, index, array){
					line = line.trim();
					
					// discard empty lines and lines 
					// starting with #, which is considered as a comment
					if (line === '' || line.charAt(0) === '#'){
						return;
					}
					
					var keyValPair = line.split('=');
					if (keyValPair && keyValPair.length === 2){
						curModuleMap[keyValPair[0].trim()] = keyValPair[1].trim();
					}else{
						log('Invalid line : ' + line);
					}
				});
			}
		}
		
		/**
		 * Get a valid locale name. If null is passed,
		 * returns default or current configured locale.
		 * 
		 * @param {String} locale - Locale identifier.
		 * @return {String} A valid locale identifier.
		 * @private
		 */
		function getValidLocale(locale){
			
			if (!locale || typeof locale !== 'string'){
				locale = currentLocale; // default or configured locale
			}
			
			// locale fix
			// if - present in locale identifier replace that with _
			if (locale.indexOf('-') !== -1){
				locale = locale.replace('-', '_');
			}
			
			return locale;
		}
		/**
		 * Check whether a module already loaded.
		 * 
		 * @param {String} module - A valid module name.
		 * @param {String} locale - A valid locale identifier.
		 * @return {Boolean} Module loaded or not.
		 * @private
		 */
		function isModuleLoaded(module, locale){
			var moduleLoaded = false;
			if (module && locale){
				moduleLoaded = (properties && properties[locale] && 
					properties[locale][module]) ? true : false;
			}
			return moduleLoaded;
		}
		
		/**
		 * Convert unicode string to international characters.
		 * @param {String} str - String to convert.
		 * @return {String} Converted string.
		 * @private
		 */
		function convertUnicodeString(str) {
			var convertedText = str.replace(/\\u[\dA-Fa-f]{4}/g, function (unicodeChar) {
				return String.fromCharCode(parseInt(unicodeChar.replace(/\\u/g, ''), 16));
			});
			return convertedText;
		}
		
		/**
		 * For logging and alerting error/debug messages.
		 * 
		 * @param {String} msg - Message to display.
		 * @param {Boolean} doAlert - Alert message or not.
		 * @private
		 */
		function log(msg, doAlert){
			// log message to console if debug mode enabled.
			if (debugMode && console && console.log){
				console.log('messageResource.js : ' + msg);
			}
			// alert messages if needed
			if (doAlert === true){
				alert('messageResource.js : ' + msg);
			}
		}
		
		/**
		 * Get contents of a file using AJAX.
		 * 
		 * @param {String} url - Url to load
		 * @param {Function} callback - Callback to be executed after loading.
		 * @private
		 */
		function ajaxGet(url, callback){
			
			var xmlhttp;
			
			// get XMLHttpRequest
			if (window.XMLHttpRequest) {
				xmlhttp = new XMLHttpRequest();
			} else {
				//	for IE6, IE5
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
			}
			
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState === 4) {
					if(xmlhttp.status === 200){
						callback(xmlhttp.responseText);
					}else {
						callback(xmlhttp.status);
					}
				}
			};
			// async request
			xmlhttp.open('GET', url, true);
			xmlhttp.send();	
		}
		
		return {
			/**
			 * Initialize messageResource.js
			 * @param {Object} config - Configuration object.
			 * @param {String} [config.filePath] - Path(directory) of message resource files.
			 * @param {String} [config.fileExtension = .properties] - File extension of message resource files.
			 * @param {String} [config.defaultLocale = en_US] - Default locale.
			 * @param {Function} [config.fileNameResolver = defaultFileNameResolver] - Specify custom file name resolver.
			 * @param {Function} [config.ajaxFunction = ajaxGet] - Specify custom ajax function for loading files. 
					The function should accept only 2 arguments : 
					1. Url/path of the file.
					2. Callback with response text as argument.
			 * @param {Boolean} [config.debugMode = false] - Enable/Disble debug mode.
			 * @public
			 */
			init : function(config){
				
				config = config || {};
				
				// append '/' to file path if not exists
				filePath = config.filePath || '';
				if (filePath && filePath.charAt(filePath.length - 1) !== '/'){
					filePath = filePath + '/';
				}
				
				// prepend '.' to file extension if not exists
				fileExtension = config.fileExtension || DEFAULT_EXTENSION;
				if (fileExtension.charAt(0) !== '.'){
					fileExtension = '.' + fileExtension;
				}

				// configure default locale
				config.defaultLocale = getValidLocale(config.defaultLocale);
				defaultLocale = config.defaultLocale;
				currentLocale = config.defaultLocale;
				
				// configure file name resolver
				fileNameResolver = config.fileNameResolver || defaultFileNameResolver;
				
				// custom ajax function
				ajaxFunction = config.ajaxFunction || ajaxGet;
				
				// enable or disable debug mode
				debugMode = config.debugMode || false;
				
				// all configurations are valid
				validConfiguration = true;
			},
			
			/**
			 * Set current locale to be used. This configured locale
			 * will be used by load and get functions if locale not specified.
			 * If current locale is not set config.defaultLocale will be used as
			 * current locale, which is en_US by default.
			 * 
			 * @param {Sring} locale - Locale identifier like en_US.
			 * @public
			 */
			setCurrentLocale : function(locale){
				// set current locale
				if (locale && typeof locale === 'string'){
					currentLocale = locale;
				}
			},
			
			/**
			 * Load a message resource file. The file name is constructed based on 
			 * the given module name and locale.
			 * 
			 * File name is constructed with default configuration in different cases as follows : 
			 * 
			 * case 1 : Module name and locale can be empty or null, then the file name will be 
			 * 			resolved to _default.properties. 
			 * case 2 : Module name - HomePage and locale - empty, then file name - HomePage.properties.
			 * case 3 : Module name - empty and locale - en_US, then file name - _default_en_US.properties.
			 * case 4 : Module name - HomePage and locale - en_US, then file name - HomePage_en_US.properties.
			 * 
			 * The above file name construction logic can be overriden by configuring 
			 * custom fileNameResolver functions.
			 * 
			 * @param {String | Array} [module = DEFAULT_MODULE_NAME] - Module name or list of module names.
			 * @param {String} [locale] - Locale identifier like en_US. Configured 
			 * 		current locale will be used if not given.
			 * @param {Function} callback - Callback to be executed after loading message resource.
			 * @public
			 */
			load : function(module, callback, locale){
			
				var fileLocale,
					validLocale, 
					validModule,
					modulesToLoad = [],
					i;
					
				if (!validConfiguration){
					log('Invalid configuration - Invoke init method with proper configuration', true);
					return;
				}
				
				validModule = module || DEFAULT_MODULE_NAME;
				validLocale = getValidLocale(locale);
				
				// no need to pass a valid locale as locale, locale can be avoided 
				// in file name for default locale
				fileLocale = (validLocale === defaultLocale) ? locale : validLocale;
				
				if (Array.isArray(validModule)){
					// load only not loaded module
					for (i = 0; i < validModule.length; i++){
						if (validModule[i] && 
							!isModuleLoaded(validModule[i], validLocale)){
							modulesToLoad.push(validModule[i]);
						}
					}
				}else{
					// only one module to load
					if (!isModuleLoaded(validModule, validLocale)){
						modulesToLoad.push(validModule);
					}
				}
				
				// return if no modules to load
				if (modulesToLoad.length === 0){
					if (callback){
						callback();
					}
					return;
				}
			
				// append specified locale if not exists
				properties = properties || {};
				properties[validLocale] = properties[validLocale] || {};
				
				// count of files loaded
				// for checking load status.
				var filesLoadedCount  = 0;
				
				// request all the files at a time
				// and callback is executed once all the files are 
				// loaded and saved.
				modulesToLoad.forEach(function (modName, index, array){
					var fileName,
						fileUrl;
					
					// read file 
					fileName = fileNameResolver(modName, fileLocale);
					fileUrl = filePath + fileName + fileExtension;
					
					// invoke configured ajax get function
					ajaxFunction(fileUrl, function(text){
						saveFile(text, modName, validLocale);
						// increment files loaded count
						filesLoadedCount += 1;
						
						// if all files loaded execute callback
						if (filesLoadedCount === modulesToLoad.length){
							if (callback){
								callback();
							}
						}
					});
				});
			},
			
			/**
			 * Get property value from loaded message resource files.
			 * 
			 * @param {String} key - Message resource property key.
			 * @param {String} [module = DEFAULT_MODULE_NAME] - Module name
			 * @param {String} [locale] - Locale identifier like en_US. Configured 
			 * 		current locale will be used if not given.
			 * @param {String} defaultValue - Default value to return if value not found. 
			 * 		If defaultValue is empty key will be used as defaultValue.
			 * @return {String} Message resource property value if exists else defaultValue passed.
			 * @public
			 */
			get : function(key, module, locale, defaultValue){
				var validModule, 
					validLocale,
					moduleObj,
					value = defaultValue || key;
				
				validModule = module || DEFAULT_MODULE_NAME;
				validLocale = getValidLocale(locale);
				
				// if specified locale - module combination is not
				// loaded return defaultValue or key which is valid.
				if (isModuleLoaded(validModule, validLocale)){
				
					moduleObj = properties[validLocale][validModule];
					value = moduleObj[key] || value;
				}
				
				return convertUnicodeString(value);
			}
		};
		
	}());
	
	// set global 
	if (!global.messageResource){
		global.messageResource = messageResource;
	}
	
}(this));
(function(){
	'use strict';
	
	// polyfills
	
	// Production steps of ECMA-262, Edition 5, 15.4.4.18
	// Reference: http://es5.github.com/#x15.4.4.18
	if (!Array.prototype.forEach) {

		Array.prototype.forEach = function (callback, thisArg) {

			var T, k;

			if (typeof this === 'undefined' || this === null) {
				throw new TypeError(" this is null or not defined");
			}

			// 1. Let O be the result of calling ToObject passing the |this| value as the argument.
			var O = Object(this);

			// 2. Let lenValue be the result of calling the Get internal method of O with the argument "length".
			// 3. Let len be ToUint32(lenValue).
			var len = O.length >>> 0;

			// 4. If IsCallable(callback) is false, throw a TypeError exception.
			// See: http://es5.github.com/#x9.11
			if (typeof callback !== "function") {
				throw new TypeError(callback + " is not a function");
			}

			// 5. If thisArg was supplied, let T be thisArg; else let T be undefined.
			if (arguments.length > 1) {
				T = thisArg;
			}

			// 6. Let k be 0
			k = 0;

			// 7. Repeat, while k < len
			while (k < len) {

				var kValue;

				// a. Let Pk be ToString(k).
				//   This is implicit for LHS operands of the in operator
				// b. Let kPresent be the result of calling the HasProperty internal method of O with argument Pk.
				//   This step can be combined with c
				// c. If kPresent is true, then
				if (k in O) {

					// i. Let kValue be the result of calling the Get internal method of O with argument Pk.
					kValue = O[k];

					// ii. Call the Call internal method of callback with T as the this value and
					// argument list containing kValue, k, and O.
					callback.call(T, kValue, k, O);
				}
				// d. Increase k by 1.
				k++;
			}
			// 8. return undefined
		};
	}
	
	// polyfill fpr isArray method
	if(!Array.isArray) {
		Array.isArray = function(arg) {
			return Object.prototype.toString.call(arg) === '[object Array]';
		};
	}
	
}());