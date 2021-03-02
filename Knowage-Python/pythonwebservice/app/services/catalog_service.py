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
import logging

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
        datastore_df = utils.datastore_to_dataframe(col_metadata, records)
        token = data['token']
        isAuthenticated, script = security.jwt_token_to_python_script(token)
        input_values = data['inputs']
        input_columns = input_values['inputColumns']
        input_variables = input_values['inputVariables']
        output_columns = input_values['outputColumns']
    except Exception as e:
        logging.error("Error parsing catalog function request: {}".format(e))
        return str(e), 400

    if not isAuthenticated:
        logging.error("Unauthorized access")
        return "Unauthorized", 401

    try:
        # resolve references to input columns
        for input_col in input_columns:
            script = script.replace("${" + input_col + "}", "df_." + input_col)
        # resolve references to input variables
        for input_var in input_variables:
            script = script.replace("${" + input_var + "}", "variables_.get(\'" + input_var + "\')")
        # resolve references to output columns
        for output_col in output_columns:
            script = script.replace("${" + output_col + "}", "outdf_." + output_col)
    except Exception as e:
        logging.error("Error resolving input references inside script: {}".format(e))
        return str(e), 500

    # init empty dataframe that will contain new columns
    out_df = pd.DataFrame(columns=output_columns)

    #build variables runtime object
    input_variables_runtime = build_runtime_variables(input_variables)

    # execute script
    try:
        namespace = {"df_": datastore_df, "outdf_": out_df, "variables_": input_variables_runtime}
        exec (script, namespace)
    except Exception as e:
        logging.error("Error during script execution: {}".format(e))
        return str(e), 500

    # convert dataframe to knowage json format
    try:
        knowage_json = utils.dataframe_to_datastore(namespace["outdf_"])
    except Exception as e:
        logging.error("Error converting dataframe to knowage format: {}".format(e))
        return str(e), 500

    return str(knowage_json).replace('\'', "\""), 200

def build_runtime_variables(input_variables):
    runtime_vars = {}
    for key in input_variables:
        var = input_variables[key]
        if var["type"] == "NUMBER":
            runtime_vars[key] = float(var["value"])
        else:
            runtime_vars[key] = var["value"]
    return runtime_vars
