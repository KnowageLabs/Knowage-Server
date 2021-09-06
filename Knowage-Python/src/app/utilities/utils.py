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
import json
import pkg_resources
from app.utilities import configs

def get_widget_config(data):
    script = data.get("script")
    output_variable = data.get('output_variable')
    return script, output_variable

def get_widget_info(data):
    document_id = data['document_id']
    widget_id = data['widget_id']
    return document_id, widget_id

def get_dataset(data):
    dataset_name = data.get('dataset_label')
    datastore = None
    if data.get('datastore') != None:
        datastore = json.loads(data.get('datastore'))
    return dataset_name, datastore

def get_analytical_drivers(data):
    drivers = data.get("drivers")
    return drivers

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
        # drop first column (redundant)
        if 'id' in df.columns:
            df.drop(columns=['id'], inplace=True)
        # assign column names
        df.columns = column_names
        #cast types
        df = df.astype(column_types)
    return df

def dataframe_to_datastore(df):
    knowage_json = []
    n_rows, n_cols = df.shape
    for i in range(0, n_rows):
        element = {}
        for j in range(0, n_cols):
            key = df.columns[j]
            value = df.loc[i][df.columns[j]]
            if type(value) is pd.Timestamp:
                value = value.strftime(configs.TIMESTAMP_FORMAT)
            element.update({key: value})
        knowage_json.append(element)
    return knowage_json

def get_environment_libraries():
    to_return = []
    for d in pkg_resources.working_set:
        lib = str(d).split(" ")
        to_return.append({"name": lib[0], "version": lib[1]})
    return json.dumps(to_return)