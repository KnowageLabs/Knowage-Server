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
from app.utilities import utils, security, constants
import logging

widget = Blueprint('widget', __name__)
#url: knowage_addr:port/2.0/widget

@widget.route('/img', methods = ['POST'])
def python_img():
    # retrieve input parameters
    try:
        request_body = request.get_json(force=True);
        script, img_file = utils.get_widget_config(request_body)
        dataset_name, datastore = utils.get_dataset(request_body)
        drivers = utils.get_analytical_drivers(request_body)
    except Exception as e:
        raise_error("Error during request decoding: {}".format(e), e)

    # resolve analytical drivers
    for d in drivers:
        script = script.replace("$P{" + d + "}", "drivers_.get(\'" + d + "\')")
    # resolve dataset references
    script = script.replace(dataset_name, "df_")

    # convert to dataframe
    try:
        if dataset_name != None:
            df = utils.datastore_to_dataframe(datastore['metaData']['fields'], datastore['rows'])
    except Exception as e:
        raise_error("Error during dataframe conversion: {}".format(e), e)

    # execute script
    try:
        namespace = {"df_": df, "drivers_": drivers}
        exec(script, namespace)
    except Exception as e:
        raise_error("Error during script execution: {}".format(e), e)

    # collect script result
    with open(img_file, "rb") as f:
        encoded_img = base64.b64encode(f.read())
    #delete temp files
    try:
        os.remove(img_file)
    except Exception:
        pass

    return "<img src=\"data:image/;base64, " + encoded_img.decode('utf-8') + "\" style=\"width:100%;height:100%;\">", 200

def raise_error(message, exception):
    logging.error(message)
    return str(exception), 400

@widget.route('/libraries', methods = ['GET'])
def python_libraries():
    return utils.get_environment_libraries(), 200