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
import logging

dataset = Blueprint('dataset', __name__)
#url: knowage_addr:port/dataset

@dataset.route('', methods = ['POST'])
def python_dataset_execute():
    # retrieve input parameters
    try:
        data = request.get_json()
        token = data['script']
        isAuthenticated, script = security.jwt_token_to_python_script(token)
        df_name = data['df_name']
        knowage_parameters = data['parameters']
    except Exception as e:
        logging.exception("Error parsing dataset request")
        return str(e), 400

    if not isAuthenticated:
        return "Unauthorized", 401

    #build parameters dictionary
    parameters = buildParameters(knowage_parameters)
    # resolve parameters
    for p in parameters:
        script = script.replace("$P{" + p + "}", "parameters_.get(\'" + p + "\')")
    # execute script
    try:
        namespace = {df_name: "", "parameters_": parameters}
        exec (script, namespace)
    except Exception as e:
        logging.exception("Error resolving input references inside script")
        return str(e), 400
    # collect script result
    df = namespace[df_name]

    # convert dataframe to knowage json format
    knowage_json = utils.dataframe_to_datastore(df)

    return str(knowage_json).replace('\'', "\""), 200

def buildParameters(knowage_parameters):
    parameters = {}
    for x in knowage_parameters:
        key = x['name']
        if x['value'] != '':
            value = x['value']
        else:
            value = x['defaultValue']
        if x['type'] == 'Number':
            value = float(value)
        parameters.update({key: value})
    return parameters

@dataset.route('/libraries', methods = ['GET'])
def python_libraries():
    return utils.get_environment_libraries(), 200