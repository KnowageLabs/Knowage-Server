# Knowage, Open Source Business Intelligence suite
# Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
#
# Knowage is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
# Knowage is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import pandas as pd
import requests
from app.utilities import constants, security, cuncurrency_manager as cm
from datetime import datetime, timedelta
import os

def findFreePort():
    import socket
    s = socket.socket()
    s.bind(('', 0))  #bind to a free port provided by the host
    return s.getsockname()[1]  #return the port number assigned

def retrieveScriptInfo(data):
    script = data.get("script")
    output_variable = data.get('output_variable')
    return script, output_variable

def retrieveKnowageInfo(headers, data):
    user_id = headers['Authorization']
    knowage_address = data['knowage_address']
    return user_id, knowage_address

def retrieveDatasetInfo(data):
    dataset_name = data.get('dataset')
    datastore_request = data['datastore_request']
    return dataset_name, datastore_request

def retrieveWidgetInfo(data):
    document_id = data['document_id']
    widget_id = data['widget_id']
    return document_id, widget_id

def retrieveAnalyticalDriversInfo(data):
    drivers = data.get("drivers")
    return drivers

def getDatasetAsDataframe(widget):
    address = "http://" + widget.knowage_address + "/knowage/restful-services/2.0/datasets/" + widget.dataset_name + "/data?offset=0&size=-1"
    auth_token = security.buildAuthToken(widget.user_id)
    headers = {'Authorization': auth_token}
    #rest request for dataset
    payload = widget.datastore_request
    r = requests.post(address, headers=headers, data=payload)
    #retrieve column names from metadata
    names = r.json()["metaData"]["fields"]
    column_names = []
    column_types = {}
    for x in names:
        if type(x) is dict:
            column_names.append(x['header'])
            if x["type"] == "float":
                column_types.update({x['name']: "float64"})
            elif x["type"] == "float":
                column_types.update({x['name']: "int64"})
    #save data as dataframe
    df = pd.DataFrame(r.json()["rows"])
    #cast types
    df = df.astype(column_types)
    #drop first column (redundant)
    df.drop(columns=['id'], inplace=True)
    # assign column names
    df.columns = column_names
    return df

def serverExists(id):
    with cm.lck:
        if id in cm.active_servers.keys():
            return True
        return False

def destroyServer(id):
    # retrieve server and thread to be stopped and stop them
    with cm.lck:
        bokeh_server = cm.active_servers[id]
        del cm.active_servers[id]
        th = cm.bokeh_resources[id].thread
        del cm.bokeh_resources[id]
    bokeh_server.io_loop.stop()
    bokeh_server.stop()
    th.join()

def bokehGarbageCollector():
    to_destroy = []
    now = datetime.now()
    with cm.lck:
        for widget_id, res in cm.bokeh_resources.items():
            if now - res.timestamp < timedelta(hours=1):
                # add widget to destroy list
                to_destroy.append(widget_id)
                # delete temp files
                os.remove(constants.TMP_FOLDER + "bokeh_script_" + str(widget_id) + ".txt")
                if res.dataset_name is not None:
                    dataset_file = constants.TMP_FOLDER + res.dataset_name + ".pckl"
                    os.remove(dataset_file)
    for w in to_destroy:
        destroyServer(w)