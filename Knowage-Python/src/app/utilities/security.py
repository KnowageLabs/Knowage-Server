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

import base64
import requests
import json
import jwt
from datetime import datetime
from app.utilities import constants, utils
import logging

def build_auth_token(user_id):
    # auth_token = "Direct " + base64(user_id)
    encoded_uid = base64.b64encode(bytes(user_id, 'utf-8')).decode('utf-8')
    return "Direct " + encoded_uid

def get_user_functionalities(widget):
    address = widget.knowage_address + "/knowage/restful-services/2.0/backendservices/userprofile/"
    logging.info("Address: {}".format(address))
    auth_token = build_auth_token(widget.user_id)
    headers = {'Authorization': auth_token}
    r = requests.get(address, headers=headers)
    logging.info("Response: {}".format(r))
    return r.json()["functionalities"]

def is_user_authorized_for_functionality(widget, func):
    if func in get_user_functionalities(widget):
        return True
    else:
        return False

def load_script_from_template(python_widget):
    template = json.loads(get_document_template(python_widget))
    for sheet in template["sheets"]:
        for widget in sheet["widgets"]:
            if widget["id"] == python_widget.widget_id:
                return widget["pythonCode"]
    return ""

def get_document_template(python_widget):
    address = python_widget.knowage_address + "/knowage/restful-services/2.0/backendservices/documenttemplate/" + str(python_widget.document_id)
    logging.info("Address: {}".format(address))
    auth_token = build_auth_token(python_widget.user_id)
    headers = {'Authorization': auth_token, "Content-Type": "application/json"}
    r = requests.post(address, headers=headers, data=json.dumps(python_widget.analytical_drivers))
    logging.info("Response: {}".format(r))
    return base64.b64decode(r.text).decode("utf-8")

def jwt_token_to_python_script(token):
    try:
        decoded_token = jwt.decode(token, utils.get_hmac_key(), algorithms='HS256')
    except Exception as e:
        return False, None
    # check expiration date
    expiration_time = decoded_token.get("exp")
    script = decoded_token.get("script")
    now = datetime.now().timestamp()
    if now > expiration_time:
        return False, None
    return True, script
