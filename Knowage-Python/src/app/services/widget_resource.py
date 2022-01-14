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
import base64
import os
from app.utilities import security, utils
import logging

widget = Blueprint('widget', __name__)
#url: knowage_addr:port/2.0/widget

@widget.route('/<output_type>', methods = ['POST'])
def python_widget_execute(output_type):
    # retrieve input parameters
    try:
        request_body = request.get_json(force=True);
        token, output_file = utils.get_widget_config(request_body)
        isAuthenticated, script = security.jwt_token_to_python_script(token)
        dataset_name, datastore = utils.get_dataset(request_body)
        drivers = utils.get_analytical_drivers(request_body)
    except Exception as e:
        logging.exception("Error during request decoding")
        return str(e), 500

    if not isAuthenticated:
        logging.error("Unauthorized access")
        return "Unauthorized", 401

    # resolve analytical drivers
    for d in drivers:
        script = script.replace("$P{" + d + "}", "drivers_.get(\'" + d + "\')")
    # resolve dataset references
    if dataset_name != None:
        script = script.replace(dataset_name, "df_")

    # convert to dataframe
    df = {}
    try:
        if datastore != None:
            df = utils.datastore_to_dataframe(datastore['metaData']['fields'], datastore['rows'])
    except Exception as e:
        logging.exception("Error during dataframe conversion")
        return str(e), 500

    # execute script
    try:
        namespace = {"df_": df, "drivers_": drivers}
        exec(script, namespace)
    except Exception as e:
        logging.exception("Error during script execution")
        return str(e), 500

    # collect script result
    with open(output_file, "rb") as f:
        output_file_content = f.read()
    #delete temp files
    try:
        os.remove(output_file)
    except Exception:
        pass

    if output_type == "img":
        encoded_img = base64.b64encode(output_file_content)
        to_return = "<img src=\"data:image/;base64, " + encoded_img.decode('utf-8') + "\" style=\"width:100%;height:100%;\">"
    else:
        to_return = output_file_content

    return to_return, 200

@widget.route('/libraries', methods = ['GET'])
def python_libraries():
    return utils.get_environment_libraries(), 200