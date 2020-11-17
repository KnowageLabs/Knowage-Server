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

from flask import Blueprint, request
from app.utilities import security, utils
import pandas as pd

catalog = Blueprint('catalog', __name__)
#url: knowage_addr:port/catalog

@catalog.route('execute', methods = ['POST'])
def python_function_execute():
    # retrieve input parameters
    try:
        data = request.get_json()
        datastore = data['datastore']
        col_metadata = datastore["metadata"]
        records = datastore["records"]
        datastore_df = utils.convertKnowageDatastoreToDataframe(col_metadata, records)
        token = data['token']
        isAuthenticated, script = security.jwtToken2pythonScript(token)
        input_values = data['inputs']
        input_columns = input_values['inputColumns']
        input_variables = input_values['inputVariables']
        output_columns = input_values['outputColumns']
    except Exception as e:
        return str(e), 400

    if not isAuthenticated:
        return "Unauthorized", 401

    # resolve references to datastore
    #script = script.replace("${df}", "df_")

    # resolve references to input columns
    for input_col in input_columns:
        script = script.replace("${" + input_col + "}", "df_." + input_col)

    # resolve references to input variables
    for input_var in input_variables:
        script = script.replace("${" + input_var + "}", "variables_.get(\'" + input_var + "\')")

    # resolve references to output columns
    for output_col in output_columns:
        script = script.replace("${" + output_col + "}", "outdf_." + output_col)

    # init empty dataframe that will contain new columns
    out_df = pd.DataFrame(columns=output_columns)

    # execute script
    try:
        namespace = {"df_": datastore_df, "outdf_": out_df, "variables_": input_variables}
        exec (script, namespace)
    except Exception as e:
        return str(e), 400

    # convert dataframe to knowage json format
    knowage_json = utils.convertDataframeToKnowageDataset(namespace["outdf_"])

    return str(knowage_json).replace('\'', "\""), 200