/**
 * This service links datasets CometD notifications from server to frontend.
 * It permits to subscribe to server notifications and then update data models.
 *
 */
angular.module("cockpitModule").service("cockpitModule_realtimeServices",function($rootScope, sbiModule_user, sbiModule_util, sbiModule_restServices, sbiModule_config, cockpitModule_template, cockpitModule_datasetServices, cometd, sbiModule_messaging){
	var rt=this;

	broadcast = function(message, dsLabel){
		if(this.oldDataMap && this.oldDataMap[message.channel]){
			if(this.oldDataMap[message.channel] != message.data){
				this.oldDataMap[message.channel] = message.data;
			}else{
				return;
			}
		}else{
			if(!this.oldDataMap){
				this.oldDataMap = {};
			}
			this.oldDataMap[message.channel] = message.data;
		}

		var event = "UPDATE_FROM_REALTIME";
		var data=JSON.parse(message.data);
		if(data.isFoundInCache) {
			console.log("Broadcasting a WIDGET_EVENT named " + event + " for dataset " + dsLabel)
			$rootScope.$broadcast("WIDGET_EVENT", event, {dsLabel:dsLabel, data:data.dataStore});
		} else {
			console.log("Error while processing data from Context Broker. Data cannot be updated due to missing of previous data.");
			console.log("You can re-execute this dashboard to get updates again");
			sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.cockpit.storeManager.noDatasetInCache"));
		}
	};

	this.init = function(){
		console.log("Initializing realtime datasets subscriptions");
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			var label = cockpitModule_template.configuration.datasets[i].dsLabel;
			console.log("Getting metadata for dataset " + label);

			var ds = cockpitModule_datasetServices.getDatasetByLabel(label);
			console.log(ds);
			if(ds.isRealtime){
				console.log("Dataset " + label + " is realtime");
				var cometdConfig = {
					contextPath: sbiModule_config.externalBasePath,
					userId:sbiModule_user.userId,
					//listenerId:sbiModule_util.uuid(),
					listenerId:'1',
					dsLabel:ds.label
					};
				console.log("Subscribe dataset " + label + " with the following config:");
				console.log(cometdConfig);
				rt.subscribe(cometdConfig);
			}
		}
	};

	/**
	 * It permits to subscribe to server notifications
	 *  @example
	 *  var cometdConfig = {
	 *    contextPath: pageContextPath,
	 *    listenerId:"1",
	 *    dsLabel:s.dsLabel,
	 *  };
	 *  cockpitModule_realtimeServices.subscribe(cometdConfig);
	 *
	 * @method cockpitModule_realtimeServices.subscribe
	 * @param {Object} config - the configuration
	 * @param {String} config.contextPath - the context path of engine
	 * @param {String} config.userId - the unique id of the user
	 * @param {String} config.listenerId - the unique id of listener
	 * @param {String} config.dsLabel - the label of dataset
	 */
	this.subscribe = function (config) {
	    var channel='/'+config.userId+'/dataset/'+config.dsLabel+'/'+config.listenerId;
	    console.log("User channel is " + channel);

	    // Function that manages the connection status with the Bayeux server
	    var _connected = false;
	    function _metaConnect(message) {
	        if (cometd.isDisconnected()) {
	            _connected = false;
	            if (config.connectionClosed!=null) {
	                config.connectionClosed();
	            }
	            return;
	        }

	        var wasConnected = _connected;
	        _connected = message.successful === true;
	        if (!wasConnected && _connected) {
	            if (config.connectionEstablished!=null) {
	                config.connectionEstablished();
	            }
	        } else if (wasConnected && !_connected) {
	            if (config.connectionBroken!=null) {
	                config.connectionBroken();
	            }
	        }
	    }

	    // Function invoked when first contacting the server and
	    // when the server has lost the state of this client
	    function _metaHandshake(handshake) {
	        if (handshake.successful === true) {
	            cometd.batch(function() {

	                cometd.subscribe(channel, function(message) {
	                    var callback=config.messageReceived || broadcast;
	                    callback(message,config.dsLabel);
	                });
	            });
	        }
	    }

	     // Disconnect when the page unloads
	     //$(window).unload(function() {
	     //   cometd.disconnect(true);
	     //});

	    var cometURL = config.contextPath + "/cometd";
	    cometd.configure({
	        url: cometURL,
	        logLevel: 'debug'
	    });

	    console.log("Comet config is set with the URL:");
	    console.log(cometURL);

	    cometd.addListener('/meta/handshake', _metaHandshake);
	    cometd.addListener('/meta/connect', _metaConnect);

	    cometd.handshake({
	        ext: {
	            'userChannel':channel
	        }
	    });
	};
})