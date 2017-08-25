Knowage and NGSI
=======================================

Introduction
============

What is NGSI?
-------------
NGSI is a protocol developed by OMA to manage Context Information. It provides operations like:

* Manage the Context Information about Context Entities, for example the lifetime and quality of information.
* Access (query, subscribe/notify) to the available Context Information about Context Entities.

The FIWARE version of the OMA NGSI interface is a RESTful API via HTTP. Its purpose is to exchange context information. The three main interaction types are

* one-time queries for context information
* subscriptions for context information updates (and the corresponding notifications)

Mission
-------
Knowage extends its set of data sources by providing native and out-of-the-box NGSI integration. 
Knowage permits to create REST dataset of type NGSI that show current information data and collects notifications of context updates in order to modify the data underneath the SpagoBI documents, using it.

Integration 
============

Dataset mapping between Knowage and NGSI
----------------------------------------
The term *dataset* in Knowage indicates a unique data resource. It can be anything: a CSV file, a SQL query against a DB, a Java class, and so on. In NGSI environment a standard SpagoBI *dataset* is a collection of Context Entities with their attributes. These Context Entities are retrieved from the NGSI Provider (currently the [Orion Context Broker OCB](https://github.com/telefonicaid/fiware-orion)) through a REST call. The answer of this call represents the Context Entities in JSON format. 

The following examples are based on a demo. The `Meter` Context Element represents a power sensor: it saves the upstream and downstream active power in an instant of a prosumer (a producer/consumer of electricity).
So, for example a REST call query takes this body:

	{
	    "entities": [
	        {
	            "isPattern": "true",
	            "id": ".*",
	            "type":"Meter"
	        }
	    ]
	}
	
It retrieves all entities of type `Meter`. The response will be something like:

	{
	  "contextResponses": [
	    {
	      "contextElement": {
	        "id": "pros6_Meter",
	        "type": "Meter",
	        "isPattern": "false",
	        "attributes": [
	          {
	            "name": "atTime",
	            "type": "timestamp",
	            "value": "2015-07-21T14:49:46.968+0200"
	          },
	          {
	            "name": "downstreamActivePower",
	            "type": "double",
	            "value": "3.8"
	          },
	          {
	            "name": "prosumerId",
	            "type": "string",
	            "value": "pros3"
	          },
	          {
	            "name": "unitOfMeasurement",
	            "type": "string",
	            "value": "kW"
	          },
	          {
	            "name": "upstreamActivePower",
	            "type": "double",
	            "value": "3.97"
	          }
	        ]
	      },
	      "statusCode": {
	        "reasonPhrase": "OK",
	        "code": "200"
	      }
	    },
	    {
	      "contextElement": {
	        "id": "pros5_Meter",
	        "type": "Meter",
	        "isPattern": "false",
	        "attributes": [
	          {
	            "name": "atTime",
	            "type": "timestamp",
	            "value": "2015-08-09T20:29:45.698+0200"
	          },
	          {
	            "name": "downstreamActivePower",
	            "type": "double",
	            "value": "1.8"
	          },
	          {
	            "name": "prosumerId",
	            "type": "string",
	            "value": "pros5"
	          },
	          {
	            "name": "unitOfMeasurement",
	            "type": "string",
	            "value": "kW"
	          },
	          {
	            "name": "upstreamActivePower",
	            "type": "double",
	            "value": "0"
	          }
	        ]
	      },
	      "statusCode": {
	        "reasonPhrase": "OK",
	        "code": "200"
	      }
	    }
	  ]
	}

In this example we have two Context Elements with the following attributes:

* atTime
* downstreamActivePower
* prosumerId
* unitOfMeasurement
* upstreamActivePower

The related Knowage dataset will contain these entities with the related attributes mapped with the types defined in the response.

How to use Knowage with NGSI
============================

DataSet creation
----------------
Defining a REST NGSI DataSet in Knowage is like to define any other type of DataSet. We need to create a REST DataSet and then make it NGSI enable.
At the homepage of application, click on DataSet:

![](media/0_DataSet_Button.png)

then create a DataSet clicking on Add and fill all main fields (name, label, etc.):

![](media/0.1_Add_DataSet.png)

Now click on Type tab, select REST type and fill all fields related to a generic REST DataSet plus clicking on NGSI checkbox:

![](media/1_DataSet_Rest_Generic.png)

So you need to define (see also below to simplify these fields):

* the Orion Query Context URL
* the request headers for an JSON call
* the Orion Query as the Request body
* the HTTP Method
* the JSON Path to retrieve the items (see below): so where the items are stored in JSON (Context Elements)
* check NGSI checkbox
* the JSON Paths to retrieve the attributes (see below): where the attributes of each Context Element are stored
* offset and fetch size params

The fields for items and attributes are written in [JSON Path Notation](https://github.com/jayway/JsonPath), which is similar to XML XPath notation. As you can see from the image the Context Elements are searched under the array of Context Responses (as described before in the JSON Response example). The attributes' definitions are related to JSON of each Context Element found through items field.

**NGSI checkbox** is specific for NGSI REST calls: it permits to subcribe to Context Element notifications from Orion Context Broker and to omit some of the REST fields (since the JSON format from NGSI specifications is fixed). So considering the previous example, it's possible to **not** define:

* the request headers
* the items
* the attributes: all attributes are fetched
* the offset and fetch size params

![](media/4_DataSet_NGSI_Automatic.png)

If you click on preview button you can see the current data retrieved from OCB defined:

![](media/5_NGSI_Automatic_Preview.png)

At the end of DataSet definition lick on Save button in the upper-right cornet to save the DataSet.

Document definition
-------------------
A *Document* in Knowage environment permits to use the DataSet previous created to make reports, statistics, visualization of data etc.. In the following example we create a Document of type *Cockpit* because is strictly related to REST NGSI DataSet. With this type of Document you can see the notifications of Context Elements changes from OCB in real time.
So, start to create a Document clicking on folder icon at homepage:

TODO

Now you can see the data retrieved from OCB:

![](media/7_Console_Document.png)

Now we modify a Context Element (please use your preferred application to make REST calls). The change is immediately reflected on Cockpit Document: the bar chart goes up/down and the related values in table changes accordingly:

![](media/8_Console_Document_Changes.png)

### Chart Engine

In addition to Cockpit Engine it's also possible to add notifications capabilities to a Chart Engine Document. In order to do so, you need to directly change the XML template associated with the document. A snippet for this functionality is the following:

```xml
	<EXTCHART animate="true" height="500" shadow="true" width="600">
	<!-- Add this element for notifications -->
	<NOTIFY_FROM_SERVER />
	<LEGEND position="bottom"/>
	...
``` 

KPI Alert notification to context broker
-------------------
By defining a context broker URL in the Alert detail page, when an alert event is thrown it will be sent also to context broker. (Look at screenshot) 
The context broker type is sent as a type field in order to be able to group entities in families, while each entity is identified by alarm label. 

![](media/10_AlarmDetail.png)

