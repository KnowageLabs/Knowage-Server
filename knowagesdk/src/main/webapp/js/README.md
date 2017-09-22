# Javascript SpagoBI API

SpagoBI SDK contains a javascript API that helps users to embed parts of SpagoBI Suite inside a web page or to retrieve informations about datasets and documents.

## Same-origin policy
Because of this policy web browsers don't let a script in a web page to retrieve data from a url if this url has a different origin respect of the web page. The origin is different if the URI schema, host name or port are different. For more informations about the same-origin policy please refer to https://en.wikipedia.org/wiki/Same-origin_policy.

That means that if you use SpagoBI SDK in a different origin from the SpagoBI server the browser will not let your script to communicate with SpagoBI.

There are two types of functions in SpagoBI SDK:
1. iFrame injection: they inject a SpagoBI page inside an iFrame
2. REST services: they use REST services of SpagoBI to retrieve some kind of informations

For the first type of function same-origin policy is not a problem. For the second one it is.
In order to solve the problem SpagoBI SDK use two different approaches:
1. jsonp (see https://en.wikipedia.org/wiki/JSONP for more details)
2. CORS (see https://en.wikipedia.org/wiki/Cross-origin_resource_sharing for more details)

The jsonp version of these functions are inside the Sbi.sdk.api namespace together with the functions of the first type (iFrame injection). The CORS version of the same functions (the ones based on REST services) are in the Sbi.sdk.cors.api namespace and have same names as jsonp counterpart. 

For example, injectDocument(config) is a function of the first type and can be found in Sbi.sdk.api namespace while getDataSetList(config) is of the second type and can be found in Sbi.sdk.api (jsonp version) namespace as well as Sbi.sdk.cors.api (CORS version).

## Jsonp vs CORS
New api that gives the same functionalities of these functions were developed using CORS instead of jsonp.
There are three main advantages on using CORS over jsonp:
<ul>
<li>all the methods are available while in jsonp only GET request can be done;</li>
<li>if an error occurs it is possible to manage it with CORS, while in jsonp it is only possible to set a timeout;</li>
<li>jsonp has security problems (see later for an example).</li>
</ul>

However, jsonp is supported by all browser while CORS doesn't work properly in Internet Explorer 8 and 9
(in IE 7 and earlier versions is not supported at all).

If you use the version with jsonp please take note about this security problem:
the authentication is made with a GET request, so users credentials are sent as query parameters.
It would be better to not use "authenticate" function (in that way, the user should already logged in
in order to use the api).

## Authentication
In order to use SpagoBI SDK functions the user has to be authenticated. If the user is already authenticated in SpagoBI the SDK use cockies to retrieve the session and all functions can be used.

If the user is not already authenticated he has to log in. There is the authenticate function for that. As for functions based on REST services, the authenticate function is also available in both Sbi.sdk.api and Sbi.sdk.cors.api namespaces.

If you use the version with jsonp please take note about this security problem: the authentication is made with a GET request, so users credentials are sent as query parameters. It would be better to not use "authenticate" function (in that way, the user should already logged in in order to use the API)

For the functions that use REST services it is also available Basic Authentication. If the user is not authenticated the browser will show a pop up asking for user credentials the first time. Than the credentials will be saved in the cache of the browser and they will be used for all other REST services. The functions based on REST services and CORS give also the possibility to specify the credentials that will be set automatically in the (Basic Authentication) header of the request.

## SDK Javascript reference
This documentation is generated from Javascript sources using [jsdoc to markdown](https://github.com/jsdoc2md/jsdoc-to-markdown).

<dl>
<dt><a href="#Sbi">Sbi</a> : <code>object</code></dt>
<dd></dd>
</dl>
## Typedefs
<dl>
<dt><a href="#ResponseCallback">ResponseCallback</a> : <code>function</code></dt>
<dd><p>This callback is called a response is returned by the server.</p>
</dd>
</dl>
<a name="Sbi"></a>
## Sbi : <code>object</code>
**Kind**: global namespace  

* [Sbi](#Sbi) : <code>object</code>
  * [.sdk](#Sbi.sdk) : <code>object</code>
    * [.api](#Sbi.sdk.api) : <code>object</code>
      * [.getDocumentHtml(config)](#Sbi.sdk.api.getDocumentHtml)
      * [.injectDocument(config)](#Sbi.sdk.api.injectDocument)
      * [.getWorksheetHtml(config)](#Sbi.sdk.api.getWorksheetHtml)
      * [.injectWorksheet(config)](#Sbi.sdk.api.injectWorksheet)
      * [.getQbeHtml(config)](#Sbi.sdk.api.getQbeHtml)
      * [.injectQbe(config)](#Sbi.sdk.api.injectQbe)
      * [.getDataSetList(config)](#Sbi.sdk.api.getDataSetList)
      * [.executeDataSet(config)](#Sbi.sdk.api.executeDataSet)
    * [.cors](#Sbi.sdk.cors) : <code>object</code>
      * [.api](#Sbi.sdk.cors.api) : <code>object</code>
        * [.getDataSetList(config)](#Sbi.sdk.cors.api.getDataSetList)
        * [.executeDataSet(config)](#Sbi.sdk.cors.api.executeDataSet)

<a name="Sbi.sdk"></a>
### Sbi.sdk : <code>object</code>
**Kind**: static namespace of <code>[Sbi](#Sbi)</code>  

* [.sdk](#Sbi.sdk) : <code>object</code>
  * [.api](#Sbi.sdk.api) : <code>object</code>
    * [.getDocumentHtml(config)](#Sbi.sdk.api.getDocumentHtml)
    * [.injectDocument(config)](#Sbi.sdk.api.injectDocument)
    * [.getWorksheetHtml(config)](#Sbi.sdk.api.getWorksheetHtml)
    * [.injectWorksheet(config)](#Sbi.sdk.api.injectWorksheet)
    * [.getQbeHtml(config)](#Sbi.sdk.api.getQbeHtml)
    * [.injectQbe(config)](#Sbi.sdk.api.injectQbe)
    * [.getDataSetList(config)](#Sbi.sdk.api.getDataSetList)
    * [.executeDataSet(config)](#Sbi.sdk.api.executeDataSet)
  * [.cors](#Sbi.sdk.cors) : <code>object</code>
    * [.api](#Sbi.sdk.cors.api) : <code>object</code>
      * [.getDataSetList(config)](#Sbi.sdk.cors.api.getDataSetList)
      * [.executeDataSet(config)](#Sbi.sdk.cors.api.executeDataSet)

<a name="Sbi.sdk.api"></a>
#### sdk.api : <code>object</code>
Note that Sbi.sdk.api definition is defined in both api.js and api_jsonp.js.In api_jsonp.js there are functions that uses jsonp to avoid the same-origin policy.The same functions were also developed with CORS and they are defined in api_cors.js.jsonp is deprecated, it is highly recommended to use CORS instead of it.NB: CORS functions are inside Sbi.sdk.cors.api namespace and have same names as jsonp counterpart.

**Kind**: static namespace of <code>[sdk](#Sbi.sdk)</code>  

* [.api](#Sbi.sdk.api) : <code>object</code>
  * [.getDocumentHtml(config)](#Sbi.sdk.api.getDocumentHtml)
  * [.injectDocument(config)](#Sbi.sdk.api.injectDocument)
  * [.getWorksheetHtml(config)](#Sbi.sdk.api.getWorksheetHtml)
  * [.injectWorksheet(config)](#Sbi.sdk.api.injectWorksheet)
  * [.getQbeHtml(config)](#Sbi.sdk.api.getQbeHtml)
  * [.injectQbe(config)](#Sbi.sdk.api.injectQbe)
  * [.getDataSetList(config)](#Sbi.sdk.api.getDataSetList)
  * [.executeDataSet(config)](#Sbi.sdk.api.executeDataSet)

<a name="Sbi.sdk.api.getDocumentHtml"></a>
##### api.getDocumentHtml(config)
It returns the HTML code of an iFrame containing document visualization. In particular config is an object that must contain at least one between documentId and documentLabel.It can also have (optional) parameters (an object containing values of document parameters), executionRole, displayToolbar and iframe, an object containing the style, height and width of the iframe where the document will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.documentId | <code>String</code> | the document id, must contain at least one between documentId and documentLabel |
| config.documentLabel | <code>String</code> | the document label,  must contain at least one between documentId and documentLabel |
| [config.parameters] | <code>Object</code> | the values of document parameters |
| [config.executionRole] | <code>String</code> | the role of execution |
| [config.displayToolbar] | <code>Boolean</code> | display or not the toolbar |
| [config.displaySliders] | <code>Boolean</code> | display or not the sliders |
| [config.iframe] | <code>Object</code> | the style object of iframe |
| [config.iframe.height] | <code>String</code> | the height of iframe |
| [config.iframe.width] | <code>String</code> | the width of iframe |
| [config.iframe.style] | <code>String</code> | the style of iframe |

**Example**  
```js
var html = Sbi.sdk.api.getDocumentHtml({		documentLabel: 'RPT_WAREHOUSE_PROF'		, executionRole: '/spagobi/user'		, parameters: {warehouse_id: 19}		, displayToolbar: false		, displaySliders: false		, iframe: {    		height: '500px'    		, width: '100%'			, style: 'border: 0px;'		}	});
```
<a name="Sbi.sdk.api.injectDocument"></a>
##### api.injectDocument(config)
It calls [getDocumentHtml](#Sbi.sdk.api.getDocumentHtml) and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as default.It can also have (optional) parameters (an object containing values of document parameters), executionRole, displayToolbar and iframe, an object containing the style, height and width of the iframe where the document will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  
**See**: [getDocumentHtml](#Sbi.sdk.api.getDocumentHtml)  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.documentId | <code>String</code> | the document id, must contain at least one between documentId and documentLabel |
| config.documentLabel | <code>String</code> | the document label,  must contain at least one between documentId and documentLabel |
| [config.parameters] | <code>Object</code> | the values of document parameters |
| [config.executionRole] | <code>String</code> | the role of execution |
| [config.displayToolbar] | <code>Boolean</code> | display or not the toolbar |
| [config.displaySliders] | <code>Boolean</code> | display or not the sliders |
| [config.iframe] | <code>Object</code> | the style object of iframe |
| [config.iframe.height] | <code>String</code> | the height of iframe |
| [config.iframe.width] | <code>String</code> | the width of iframe |
| [config.iframe.style] | <code>String</code> | the style of iframe |

**Example**  
```js
execTest8 = function() {	Sbi.sdk.api.injectWorksheet({		datasetLabel: 'DS_DEMO_51_COCKPIT'		, target: 'worksheet'		, height: '600px'		, width: '1100px'		, iframe: {			style: 'border: 0px;'		}	});};
```
<a name="Sbi.sdk.api.getWorksheetHtml"></a>
##### api.getWorksheetHtml(config)
It returns the HTML code of an iFrame containing worksheet visualization.config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.target | <code>String</code> | the target |
| config.documentLabel | <code>String</code> | the document label,  must contain at least one between documentId and documentLabel |
| [config.height] | <code>String</code> | the height of iframe, can be put inside iframe object |
| [config.width] | <code>String</code> | the width of iframe, can be put inside iframe object |
| config.iframe | <code>Object</code> | the style object of iframe |
| [config.iframe.height] | <code>String</code> | the height of iframe, can be put outside |
| [config.iframe.width] | <code>String</code> | the width of iframe |
| [config.iframe.style] | <code>String</code> | the style of iframe |

<a name="Sbi.sdk.api.injectWorksheet"></a>
##### api.injectWorksheet(config)
It calls [getWorksheetHtml](#Sbi.sdk.api.getWorksheetHtml) and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as defaultconfig is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| config | <code>Object</code> |  | the configuration |
| [config.target] | <code>String</code> | <code>&quot;&lt;body&gt;&quot;</code> | the target |
| config.documentLabel | <code>String</code> |  | the document label,  must contain at least one between documentId and documentLabel |
| [config.height] | <code>String</code> |  | the height of iframe, can be put inside iframe object |
| [config.width] | <code>String</code> |  | the width of iframe, can be put inside iframe object |
| config.iframe | <code>Object</code> |  | the style object of iframe |
| [config.iframe.height] | <code>String</code> |  | the height of iframe, can be put outside |
| [config.iframe.width] | <code>String</code> |  | the width of iframe |
| [config.iframe.style] | <code>String</code> |  | the style of iframe |

<a name="Sbi.sdk.api.getQbeHtml"></a>
##### api.getQbeHtml(config)
It returns the HTML code of an iFrame containing qbe visualization.config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.target | <code>String</code> | the target |
| config.documentLabel | <code>String</code> | the document label,  must contain at least one between documentId and documentLabel |
| [config.height] | <code>String</code> | the height of iframe, can be put inside iframe object |
| [config.width] | <code>String</code> | the width of iframe, can be put inside iframe object |
| config.iframe | <code>Object</code> | the style object of iframe |
| [config.iframe.height] | <code>String</code> | the height of iframe, can be put outside |
| [config.iframe.width] | <code>String</code> | the width of iframe |
| [config.iframe.style] | <code>String</code> | the style of iframe |

<a name="Sbi.sdk.api.injectQbe"></a>
##### api.injectQbe(config)
It calls [getQbeHtml](#Sbi.sdk.api.getQbeHtml) and inject the generated iFrame inside a specified tag. If the target tag is not specified in config variable, it chooses the <body> tag as default.config is an object that must contain datasetLabel and iframe, an object containing the style, height and width of the iframe where the worksheet and qbe will be rendered (height and width can also be put outside the iframe object).

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| config | <code>Object</code> |  | the configuration |
| [config.target] | <code>String</code> | <code>&quot;&lt;body&gt;&quot;</code> | the target |
| config.documentLabel | <code>String</code> |  | the document label,  must contain at least one between documentId and documentLabel |
| [config.height] | <code>String</code> |  | the height of iframe, can be put inside iframe object |
| [config.width] | <code>String</code> |  | the width of iframe, can be put inside iframe object |
| config.iframe | <code>Object</code> |  | the style object of iframe |
| [config.iframe.height] | <code>String</code> |  | the height of iframe, can be put outside |
| [config.iframe.width] | <code>String</code> |  | the width of iframe |
| [config.iframe.style] | <code>String</code> |  | the style of iframe |

**Example**  
```js
execTest9 = function() {		Sbi.sdk.api.injectQbe({			datasetLabel: 'DS_DEMO_51_COCKPIT'			, target: 'qbe'			, height: '600px'			, width: '1100px'			, iframe: {			style: 'border: 0px;'		  }		});	};
```
<a name="Sbi.sdk.api.getDataSetList"></a>
##### api.getDataSetList(config)
It returns the list of datasets

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.callback | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the response is returned by the server |

**Example**  

```js
execTest6 = function() {	    Sbi.sdk.api.getDataSetList({	    	callback: function( json, args, success ) {	    		if (success){	    			var str = "";	    				    			for (var key in json){		    			str += "<tr><td>" + json[key].label + "</td><td>" + json[key].name + "</td><td>" + json[key].description + "</td></tr>";	    			}	    				    			document.getElementById('datasets').innerHTML = str;	    		}			}});	};
```
<a name="Sbi.sdk.api.executeDataSet"></a>
##### api.executeDataSet(config)
It executes a dataset

**Kind**: static method of <code>[api](#Sbi.sdk.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.documentLabel | <code>String</code> | the document label |
| [config.parameters] | <code>Object</code> | the values of dataset parameters |
| config.callback | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the response is returned by the server |

**Example**  

```js
execTest7 = function() {   Sbi.sdk.api.executeDataSet({   	datasetLabel: 'DS_DEMO_EXTCHART'   	, parameters: {   		par_year: 2011,   		par_family: 'Food'   	}   	, callback: function( json, args, success ) {   		if (success){   			var str = "<th>Id</th>";   			   			var fields = json.metaData.fields;   			for(var fieldIndex in fields) {   				if (fields[fieldIndex].hasOwnProperty('header'))   					str += '<th>' + fields[fieldIndex]['header'] + '</th>';   			}   			   			str += '<tbody>';   			   			var rows = json.rows;   			for (var rowIndex in rows){   				str += '<tr>';   				for (var colIndex in rows[rowIndex]) {   					str += '<td>' + rows[rowIndex][colIndex] + '</td>';   				}   				str += '</tr>';   			}   			   			str += '</tbody>';   			   			document.getElementById('results').innerHTML = str;   		}		}});};
```
<a name="Sbi.sdk.cors"></a>
#### sdk.cors : <code>object</code>
**Kind**: static namespace of <code>[sdk](#Sbi.sdk)</code>  

* [.cors](#Sbi.sdk.cors) : <code>object</code>
  * [.api](#Sbi.sdk.cors.api) : <code>object</code>
    * [.getDataSetList(config)](#Sbi.sdk.cors.api.getDataSetList)
    * [.executeDataSet(config)](#Sbi.sdk.cors.api.executeDataSet)

<a name="Sbi.sdk.cors.api"></a>
##### cors.api : <code>object</code>
There are three main advantages on using CORS over jsonp:<ul> <li>all the methods are available while in jsonp only GET request can be done;</li> <li>if an error occurs it is possible to manage it with CORS, while in jsonp it is only possible to set a timeout;</li> <li>jsonp has security problems (see later for an example).</li></ul>

**Kind**: static namespace of <code>[cors](#Sbi.sdk.cors)</code>  
**See**: [api](#Sbi.sdk.api)  

* [.api](#Sbi.sdk.cors.api) : <code>object</code>
  * [.getDataSetList(config)](#Sbi.sdk.cors.api.getDataSetList)
  * [.executeDataSet(config)](#Sbi.sdk.cors.api.executeDataSet)

<a name="Sbi.sdk.cors.api.getDataSetList"></a>
###### api.getDataSetList(config)
It returns the list of datasets. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.

**Kind**: static method of <code>[api](#Sbi.sdk.cors.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.callbackOk | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the ok response is returned by the server |
| [config.callbackError] | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the error response is returned by the server |

**Example** 
 
```js
execTest6 = function() {   Sbi.sdk.cors.api.getDataSetList({   	callbackOk: function(obj) {   		str = '';   		   		for (var key in obj){   			str += "<tr><td>" + obj[key].label + "</td><td>" + obj[key].name + "</td><td>" + obj[key].description + "</td></tr>"; 			} 			 			document.getElementById('datasets').innerHTML = str;		}   });	};
```
<a name="Sbi.sdk.cors.api.executeDataSet"></a>
###### api.executeDataSet(config)
It executes a dataset. This time config contains two callback functions: callbackOk and callbackError. This is because with CORS it is possible to know when the server returns an error, so we can define the behavior to have if there is an error. However, it is not mandatory to specify the callbackError function: there is a default one that create a pop up with status code and text describing the error.

**Kind**: static method of <code>[api](#Sbi.sdk.cors.api)</code>  

| Param | Type | Description |
| --- | --- | --- |
| config | <code>Object</code> | the configuration |
| config.documentLabel | <code>String</code> | the document label |
| [config.parameters] | <code>Object</code> | the values of dataset parameters |
| config.callbackOk | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the ok response is returned by the server |
| [config.callbackError] | <code>[ResponseCallback](#ResponseCallback)</code> | function to be called after the error response is returned by the server |

**Example**  

```js
execTest7 = function() {   Sbi.sdk.cors.api.executeDataSet({   	datasetLabel: 'DS_DEMO_EXTCHART'   	, parameters: {   		par_year: 1998,   		par_family: 'Food'   	}   	, callbackOk: function(obj) {   		var str = "<th>Id</th>";   		 			var fields = obj.metaData.fields; 			for(var fieldIndex in fields) { 				if (fields[fieldIndex].hasOwnProperty('header')) 					str += '<th>' + fields[fieldIndex]['header'] + '</th>'; 			} 			 			str += '<tbody>'; 			 			var rows = obj.rows; 			for (var rowIndex in rows){ 				str += '<tr>'; 				for (var colIndex in rows[rowIndex]) { 					str += '<td>' + rows[rowIndex][colIndex] + '</td>'; 				} 				str += '</tr>'; 			} 			 			str += '</tbody>'; 			 			document.getElementById('results').innerHTML = str;		}});};
```
<a name="ResponseCallback"></a>
## ResponseCallback : <code>function</code>
This callback is called a response is returned by the server.

**Kind**: global typedef  

| Param | Type | Description |
| --- | --- | --- |
| json | <code>String</code> | json of response |
| args | <code>Array</code> |  |
| success | <code>Boolean</code> | true if it's a success response otherwise false |

