define({ "api": [
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "./doc/main.js",
    "group": "D__Knowage_Workspace_Trunk_knowagedataminingengine_doc_main_js",
    "groupTitle": "D__Knowage_Workspace_Trunk_knowagedataminingengine_doc_main_js",
    "name": ""
  },
  {
    "type": "get",
    "url": "/1.0/function/execute-sample/:id",
    "title": "Execute function by ID with sample data",
    "name": "GET_executeSampleCatalogFunctionById",
    "group": "Functions",
    "version": "0.1.0",
    "description": "<p>-- AUTHENTICATION</p> <p>All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this requirement.</p> <p>-- DESCRIPTION</p> <p>This service can be used to execute a function (local or remote) specifying its identifier. The identifier (which is less friendly compared to label) can be obtained by querying the function catalog through its APIs.</p> <p>When calling this service, it is not request to provide any data. The function will use the embedded mandatory sample data. Those sample data have been provided when the functions has been added to the catalog.</p> <p>The response contains an array of JSONObject with three fields: 'resultType', 'result' and 'resultName'.</p> <p>The first field contains one of the following values: 'Image', 'Text', 'Dataset' or 'File'.</p> <p>In case of 'Image' and 'File', the result is provided with Base64 encoding.</p> <p>Also, 'File' has a special result structure (see example below). Please notice that 'filesize' and 'filetype' are optional fields. Valid values for 'filetype' are listed here: http://www.iana.org/assignments/media-types/</p> <p>-- IMPORTANT --</p> <p>Due to its nature, this service can benefit of data compression for both request and response. Because of this, the payload can be compressed when required (mostly when files and images are involved). This means that a request or a response with one or more 'File' SHOULD be sent already compressed with a header set to 'Content-Encoding': 'gzip'.</p> <p>In both direction this aspect must be handled by reading the request header accordingly.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "id",
            "description": "<p>Function ID.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "json",
            "optional": false,
            "field": "response",
            "description": "<p>The list of functions and keywords with the specified type.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Response-example:",
          "content": "[\n   {\n      \"resultType\":\"Image\",\n      \"result\":\"iVBORw0KGgoAAAANSUhEUgAAAyAA....BiJ1pQ89NBDakQohBASIEIIIYqG1CaKuIkY+OlY6623QmCC\",\n      \"resultName\":\"valuesPlot\"\n   },\n   {\n      \"resultType\":\"Text\",\n      \"result\":\"120\",\n      \"resultName\":\"maximimValue\"\n   },\n   {\n   \t  \"resultType\":\"File\",\n   \t  \"result\":\n   \t  {\n   \t  \t\"filesize\": \"54836\",\n      \t\"filetype\": \"image/jpeg\",\n      \t\"filename\": \"chart.jpg\",\n      \t\"base64\":   \"/9j/4AAQSkZJRgABAgAAAQAB....AAD//gAEKgD/4gIctcwIQA...\"\n   \t  },\n      \"resultName\":\"fileToBeSave\"\n   }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "examples": [
        {
          "title": "Error-Response example:",
          "content": "{\n\t\"service\":\"\",\n\t\"errors\":[\n\t\t{\"message\":\"Here the error message.\"}\n\t]\n}",
          "type": "json"
        }
      ]
    },
    "filename": "./src/it/eng/spagobi/engines/datamining/api/FunctionResource.java",
    "groupTitle": "Functions"
  },
  {
    "type": "get",
    "url": "/1.0/function/execute-sample?label=:label",
    "title": "Execute function by label with sample data",
    "name": "GET_executeSampleCatalogFunctionByLabel",
    "group": "Functions",
    "version": "0.1.0",
    "description": "<p>-- AUTHENTICATION All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this requirement.</p> <p>-- DESCRIPTION This service can be used to execute a function (local or remote) specifying its label. The label is usually known, but can be obtained by querying the function catalog through its GUI or APIs.</p> <p>Please refer to the service GET /1.0/function/execute-sample/:id to get more information about the usage.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "label",
            "description": "<p>Function label.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "json",
            "optional": false,
            "field": "response",
            "description": "<p>The list of functions and keywords with the specified type.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Response-example:",
          "content": "[\n   {\n      \"resultType\":\"Image\",\n      \"result\":\"iVBORw0KGgoAAAANSUhEUgAAAyAA....BiJ1pQ89NBDakQohBASIEIIIYqG1CaKuIkY+OlY6623QmCC\",\n      \"resultName\":\"valuesPlot\"\n   },\n   {\n      \"resultType\":\"Text\",\n      \"result\":\"120\",\n      \"resultName\":\"maximimValue\"\n   }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "examples": [
        {
          "title": "Error-Response example:",
          "content": "{\n\t\"service\":\"\",\n\t\"errors\":[\n\t\t{\"message\":\"Here the error message.\"}\n\t]\n}",
          "type": "json"
        }
      ]
    },
    "filename": "./src/it/eng/spagobi/engines/datamining/api/FunctionResource.java",
    "groupTitle": "Functions"
  },
  {
    "type": "POST",
    "url": "/1.0/function/execute/:id",
    "title": "Execute function by ID with provided data",
    "name": "POST_executeCatalogFunctionById",
    "group": "Functions",
    "version": "0.1.0",
    "description": "<p>-- AUTHENTICATION</p> <p>All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this requirement.</p> <p>-- DESCRIPTION</p> <p>This service can be used to execute a function (local or remote) specifying its identifier and providing specific data. The identifier (which is less friendly compared to label) can be obtained by querying the function catalog through its APIs.</p> <p>The request contains an array of three JSONObject, which defines input type and provided data. When calling this service, the caller MUST provide all the data. Optional fields are not allowed. Please pay attention to the special structure for 'filesIn' inputs. Like inside the response file structure, 'filesize' and 'filetype' are useful but optional.</p> <p>The response contains an array of JSONObject with three fields: 'resultType', 'result' and 'resultName'.</p> <p>The first field contains one of the following values: 'Image', 'Text', 'Dataset' or 'File'.</p> <p>In case of 'Image' and 'File', the result is provided with Base64 encoding.</p> <p>Also, 'File' has a special result structure (see example below). Please notice that 'filesize' and 'filetype' are optional fields. Valid values for 'filetype' are listed here: http://www.iana.org/assignments/media-types/</p> <p>-- IMPORTANT --</p> <p>Due to its nature, this service can benefit of data compression for both request and response. Because of this, the payload can be compressed when required (mostly when files and images are involved). This means that a request or a response with one or more 'File' SHOULD be sent already compressed with a header set to 'Content-Encoding': 'gzip'.</p> <p>In both direction this aspect must be handled by reading the request header accordingly.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "id",
            "description": "<p>Function id.</p>"
          },
          {
            "group": "Parameter",
            "type": "json",
            "optional": false,
            "field": "function",
            "description": "<p>Function detail.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Request-Example:",
          "content": "\n[\n   {\n      \"type\":\"variablesIn\",\n      \"items\":{\n         \"a\":\"3\",\n         \"b\":\"3\"\n      }\n   },\n   {\n      \"type\":\"datasetsIn\",\n      \"items\":{\n         \"df\":\"df2\"\n      }\n   },\n   {\n      \"type\":\"filesIn\",\n      \"items\":{\n         \"df\": {\n         \t\"filename\":\"traffic.avi\",\n         \t\"base64\":\"/9j/4AAQSkZJRgABAgAAAQAB....AAD//gAEKgD/4gIctcwIQA...\"\n         }\n      }\n   }\n]",
          "type": "json"
        }
      ]
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "json",
            "optional": false,
            "field": "response",
            "description": "<p>The results from the execute function.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Response-example:",
          "content": "[\n   {\n      \"resultType\":\"Image\",\n      \"result\":\"iVBORw0KGgoAAAANSUhEUgAAA...==NSUhEUgtfgf\",\n      \"resultName\":\"res\"\n   },\n   {\n      \"resultType\":\"Dataset\",\n      \"result\":\"biadmin_function_catalog_datasetOutNEW\",\n      \"resultName\":\"datasetOut\"\n   }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "examples": [
        {
          "title": "Error-Response example:",
          "content": "{\n\t\"service\":\"\",\n\t\"errors\":[\n\t\t{\"message\":\"Here the error message.\"}\n\t]\n}",
          "type": "json"
        }
      ]
    },
    "filename": "./src/it/eng/spagobi/engines/datamining/api/FunctionResource.java",
    "groupTitle": "Functions"
  },
  {
    "type": "POST",
    "url": "/1.0/function/execute?label=:label",
    "title": "Execute function by label with provided data",
    "name": "POST_executeCatalogFunctionByLabel",
    "group": "Functions",
    "version": "0.1.0",
    "description": "<p>-- AUTHENTICATION All the Knowage RESTful services are based on Basic Authentication. Please generate your request in accordance with this requirement.</p> <p>-- DESCRIPTION This service can be used to execute a function (local or remote) specifying its label and providing specific data. The label is usually known, but can be obtained by querying the function catalog through its GUI or APIs.</p> <p>Please refer to the service GET /1.0/function/execute/:id to get more information about the usage.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "label",
            "description": "<p>Function label.</p>"
          },
          {
            "group": "Parameter",
            "type": "json",
            "optional": false,
            "field": "function",
            "description": "<p>Function detail.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Request-Example:",
          "content": "\n[\n   {\n      \"type\":\"variablesIn\",\n      \"items\":{\n         \"a\":\"3\",\n         \"b\":\"3\"\n      }\n   },\n   {\n      \"type\":\"datasetsIn\",\n      \"items\":{\n         \"df\":\"df2\"\n      }\n   }\n]",
          "type": "json"
        }
      ]
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "json",
            "optional": false,
            "field": "response",
            "description": "<p>The results from the execute function.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Response-example:",
          "content": "[\n   {\n      \"resultType\":\"Image\",\n      \"result\":\"iVBORw0KGgoAAAANSUhEUgAAA...==NSUhEUgtfgf\",\n      \"resultName\":\"res\"\n   },\n   {\n      \"resultType\":\"Dataset\",\n      \"result\":\"biadmin_function_catalog_datasetOutNEW\",\n      \"resultName\":\"datasetOut\"\n   }\n]",
          "type": "json"
        }
      ]
    },
    "error": {
      "examples": [
        {
          "title": "Error-Response example:",
          "content": "{\n\t\"service\":\"\",\n\t\"errors\":[\n\t\t{\"message\":\"Here the error message.\"}\n\t]\n}",
          "type": "json"
        }
      ]
    },
    "filename": "./src/it/eng/spagobi/engines/datamining/api/FunctionResource.java",
    "groupTitle": "Functions"
  }
] });
