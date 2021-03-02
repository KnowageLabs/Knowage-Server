#!/usr/bin/env python3

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
from app.exceptions.KnowageRestServiceError import KnowageRestServiceError
from datetime import datetime, timedelta
import os
import xml.etree.ElementTree as ET
import json
import pkg_resources
import logging

def find_free_port():
    import socket
    import random
    s = socket.socket()
    count = 0
    range = get_ports_range()
    lower_bound = int(range[0])
    upper_bound = int(range[1])
    while count < upper_bound - lower_bound:
        count += 1
        p = random.randint(lower_bound, upper_bound)
        try:
            s.bind(('', p))  #bind to a free port provided by the host
            return s.getsockname()[1]  #return the port number assigned
        except:
            pass
    return None

def get_widget_config(data):
    script = data.get("script")
    output_variable = data.get('output_variable')
    return script, output_variable

def get_widget_info(data):
    document_id = data['document_id']
    widget_id = data['widget_id']
    return document_id, widget_id

def get_knowage_token(headers):
    user_id = headers['Knowage-Authorization']
    return user_id

def get_dataset(data):
    dataset_name = data.get('dataset')
    datastore_request = data['datastore_request']
    return dataset_name, datastore_request

def get_analytical_drivers(data):
    drivers = data.get("drivers")
    return drivers

def load_data_as_dataframe(widget):
    address = widget.knowage_address + "/knowage/restful-services/2.0/datasets/" + widget.dataset_name + "/data?offset=0&size=-1"
    auth_token = security.build_auth_token(widget.user_id)
    headers = {'Authorization': auth_token}
    #rest request for dataset
    payload = widget.datastore_request
    r = requests.post(address, headers=headers, data=payload)
    # retrieve column names from metadata
    response_body = r.json()
    if 'errors' in response_body:
        raise KnowageRestServiceError(address)
    col_names = response_body["metaData"]["fields"]
    rows = response_body["rows"]
    df = dataset_to_dataframe(col_names, rows)
    return df

# used for widget
def dataset_to_dataframe(names, rows):
    column_names = []
    column_types = {}
    for x in names:
        if type(x) is dict:
            column_names.append(x['header'])
            if x["type"] == "float":
                column_types.update({x['name']: "float64"})
            elif x["type"] == "int":
                column_types.update({x['name']: "int64"})
    #save data as dataframe
    df = pd.DataFrame(rows)
    if not df.empty:
        try:
            #cast types
            logging.debug("Trying to cast types: {}".format(column_types))
            df = df.astype(column_types)
        except Exception as e:
            logging.warning("Could not cast dataframe types")
        #drop first column (redundant)
        df.drop(columns=['id'], inplace=True)
        # assign column names
        df.columns = column_names
    return df

# used for functions catalog
def datastore_to_dataframe(metadata, rows):
    column_names = []
    column_types = {}
    for x in metadata:
        if type(x) is dict:
            column_names.append(x['header'])
            if x["type"] == "class java.lang.Double":
                column_types.update({x['header']: "float64"})
            elif x["type"] == "class java.lang.Integer":
                column_types.update({x['header']: "int64"})
    #save data as dataframe
    df = pd.DataFrame(rows)
    if not df.empty:
        # assign column names
        df.columns = column_names
        #cast types
        df = df.astype(column_types)
        #drop first column (redundant)
        if 'id' in df.columns:
            df.drop(columns=['id'], inplace=True)
    return df

def dataframe_to_datastore(df):
    knowage_json = []
    n_rows, n_cols = df.shape
    for i in range(0, n_rows):
        element = {}
        for j in range(0, n_cols):
            element.update({df.columns[j]: df.loc[i][df.columns[j]]})
        knowage_json.append(element)
    return knowage_json

def server_exists(id):
    with cm.lck:
        if id in cm.active_servers.keys():
            return True
        return False

def destroy_server(id):
    # retrieve server and thread to be stopped and stop them
    with cm.lck:
        bokeh_server = cm.active_servers[id]
        del cm.active_servers[id]
        th = cm.bokeh_resources[id].thread
        del cm.bokeh_resources[id]
    bokeh_server.io_loop.stop()
    bokeh_server.stop()
    th.join()

def bokeh_garbage_collector():
    to_destroy = []
    now = datetime.now()
    with cm.lck:
        for widget_id, res in cm.bokeh_resources.items():
            if now - res.timestamp > timedelta(hours=1):
                # add widget to destroy list
                to_destroy.append(widget_id)
                # delete temp files
                os.remove(constants.TMP_FOLDER + "bokeh_script_" + str(widget_id) + ".txt")
                if res.dataset_name is not None:
                    dataset_file = constants.TMP_FOLDER + res.dataset_name + ".pckl"
                    os.remove(dataset_file)
    for w in to_destroy:
        destroy_server(w)

def get_hmac_key():
    tree = ET.parse(constants.CONFIG_FILE)
    root = tree.getroot()
    key = root[0][0].text
    return key

def get_knowage_address():
    tree = ET.parse(constants.CONFIG_FILE)
    root = tree.getroot()
    addr = root[0][1].text
    return addr

def get_python_engine_address():
    tree = ET.parse(constants.CONFIG_FILE)
    root = tree.getroot()
    addr = root[0][2].text
    return addr

def get_ports_range():
    tree = ET.parse(constants.CONFIG_FILE)
    root = tree.getroot()
    range = root[0][3].text
    return range.split("-")

def get_environment_libraries():
    to_return = []
    for d in pkg_resources.working_set:
        lib = str(d).split(" ")
        to_return.append({"name": lib[0], "version": lib[1]})
    return json.dumps(to_return)