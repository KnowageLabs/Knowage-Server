/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
* @namespace Sbi
*/

/**
* @namespace Sbi.tools
*/

/**
* @namespace Sbi.tools.dataset
*/

/**
 * This library links datasets CometD notifications from server to frontend.
 * It permits to subscribe to server notifications and then update Ext JS Store (3 and 4 versions).
 *
 * @namespace Sbi.tools.dataset.cometd
 */
var nsName="Sbi.tools.dataset.cometd";
//create namespace
var ns=(function() {
    var o=nsName.split(".");
    var last=window;
    for (var i=0;i<o.length;i++) {
        var pref=o[i];
        if (last[pref]===undefined) {
            var prefO={}
            last[pref]=prefO;
        }
        last=last[pref];
    }
    return last;
})(nsName);


//override this method to the specified store for a bug in grouping in ext 4.
var overrideUpdateGroupsOnUpdate = function (s) {
    Ext.override(s, {
         updateGroupsOnUpdate: function(record, modifiedFieldNames){
            var me = this,
                groupField = me.getGroupField(),
                groupName = me.getGroupString(record),
                groups = me.groups,
                len, i, items, group;

            if (modifiedFieldNames && Ext.Array.indexOf(modifiedFieldNames, groupField) !== -1) {

                items = groups.items;
                for (i = 0, len = items.length; i < len; ++i) {
                    group = items[i];
                    if (group.contains(record)) {
                        group.remove(record);
                        break;
                    }
                }
                groups.getByKey(groupName);
                if (!group) {
                    group = groups.add(new Ext.data.Group({
                        key: groupName,
                        store: me
                    }));
                }
                group.add(record);


                me.suspendEvents();
                me.remove(record);
                me.addSorted(record);
                me.resumeEvents();
            } else {
                var group=groups.getByKey(groupName);
                if (typeof group != 'undefined') {
                    group.setDirty();
                }
            }
        }
    });
};

/**
 * Update the Ext JS Store from data from server. Used only internally.
 * s: Store, message: data from cometd server message
 */
var updateStore=function(message,s) {
    var extV="fields" in s ? 3:4;
    var data=JSON.parse(message.data);

    var getIdColumn=function() {
        //the first option is for ext 3, the last is for ext 4
        var fields= extV==3 ? s.fields : s.getProxy().getModel().fields;
        for (var i=0;i<fields.getCount();i++) {
            var field=fields.get(i);
            if (field.name==='id' || field.header==='id') {
                return field.name;
            }
        }
        return null;
    };

    var createRecord=function(dataRec) {
        if (extV === 3) {
            return new s.recordType(dataRec);
        }

        //Ext 4 version
        var model=s.getProxy().getModel();
        var res=new model(dataRec);
        return res;
    }


    var idColumn=getIdColumn();
    if (idColumn===null) {
        //no update
        return null;
    }

    //added
    var toAdd=[];
    for (var i=0;i<data.added.length;i++) {
        var addRec=data.added[i];
        toAdd.push(createRecord(addRec));
    }
    s.add(toAdd);

    if (extV === 4 && s.updateGroupsOnUpdateOverridden !== true) {
        overrideUpdateGroupsOnUpdate(s);
        s.updateGroupsOnUpdateOverridden=true;
    }

    //updated O(n), can be done in O(1) with id property set
    for (var i=0;i<data.updated.length;i++) {
        var updRec=data.updated[i];
        //if server paginated then it can not find the record
        for (var j=0;j<s.getCount();j++) {
            var toUpd=s.getAt(j);
            if (toUpd.get(idColumn)!==updRec[idColumn]) {
                continue;
            }

            var newRec=createRecord(updRec);

            //found, update all fields
            for (var k=0;k<toUpd.fields.getCount();k++) {
                var field=toUpd.fields.get(k);
                if (updRec[field.name]===undefined) {
                    //there could be a value of record (recNo for example) not defined in update
                    continue;
                }

                //take the object value from the created record
                var valueUpdate=newRec.get(field.name);
                if (field.type==="date" && typeof(updRec[field.name])==='string') {
                    //date case: parse the date string to date object
                    valueUpdate=Date.parseDate(updRec[field.name],field.dateFormat );
                }
                toUpd.set(field.name,valueUpdate);
            }
            toUpd.commit();
        }
    }

    //deleted O(n), can be done in O(1) with id property set
    for (var i=0;i<data.deleted.length;i++) {
        var delRec=data.deleted[i];
        //if server paginated then it can not find the record
        for (var j=0;j<s.getCount();j++) {
            var toDel=s.getAt(j);
            if (toDel.get(idColumn)!==delRec[idColumn]) {
                continue;
            }
            //found, remove toDel
            s.removeAt(j);
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
 *    store:s
 *  };
 *  Sbi.tools.dataset.cometd.subscribe(cometdConfig);
 *
 * @method Sbi.tools.dataset.cometd.subscribe
 * @param {Object} config - the configuration
 * @param {String} config.contextPath - the context path of engine
 * @param {String} config.listenerId - the unique id of listener
 * @param {String} config.dsLabel - the label of dataset
 * @param {Object} config.store - the Ext JS store
 */
ns.subscribe = function (config) {
    var $=jQuery;
    var cometd=$.cometd;

    var channel='/'+Sbi.user.userId+'/dataset/'+config.dsLabel+'/'+config.listenerId;

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
                //example of channel
                cometd.subscribe(channel, function(message) {
                    var callback=config.messageReceived || updateStore;
                    callback(message,config.store);
                });
            });
        }
    }

     // Disconnect when the page unloads
    $(window).unload(function() {
        cometd.disconnect(true);
    });

    var cometURL = location.protocol + "//" + location.host + config.contextPath + "/cometd";
    cometd.configure({
        url: cometURL
    });

    cometd.addListener('/meta/handshake', _metaHandshake);
    cometd.addListener('/meta/connect', _metaConnect);

     //only as an example
    cometd.handshake({
        ext: {
            'userChannel':channel
        }
    });
};

