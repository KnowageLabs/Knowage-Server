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
from app.utilities import security, cuncurrency_manager

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
    dataset_name = data['dataset']
    datastore_request = data['datastore_request']
    return dataset_name, datastore_request

def retrieveWidgetInfo(data):
    document_id = data['document_id']
    widget_id = data['widget_id']
    return document_id, widget_id

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
    with cuncurrency_manager.lck:
        if id in cuncurrency_manager.active_servers.keys():
            return True
        return False

def destroyServer(id):
    # retrieve server and thread to be stopped and stop them
    with cuncurrency_manager.lck:
        bokeh_server = cuncurrency_manager.active_servers[id]
        del cuncurrency_manager.active_servers[id]
        th = cuncurrency_manager.active_threads[id]
        del cuncurrency_manager.active_threads[id]
        del cuncurrency_manager.ports_dict[id]
    bokeh_server.io_loop.stop()
    bokeh_server.stop()
    th.join()