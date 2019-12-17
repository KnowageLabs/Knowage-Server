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

def buildAuthToken(user_id):
    # auth_token = "Direct " + base64(user_id)
    encoded_uid = base64.b64encode(bytes(user_id, 'utf-8')).decode('utf-8')
    return "Direct " + encoded_uid

def getUserFunctionalities(user_id, knowage_address):
    address = "http://" + knowage_address + "/knowage/restful-services/userprofile/"
    auth_token = buildAuthToken(user_id)
    headers = {'Authorization': auth_token}
    r = requests.get(address, headers=headers)
    return r.json()["functionalities"]

def userIsAuthorizedForFunctionality(user_id, knowage_address, func):
    if func in getUserFunctionalities(user_id, knowage_address):
        return True
    else:
        return False

def userIsAuthenticated(user_id, knowage_address):
    address = "http://" + knowage_address + "/knowage/restful-services/2.0/datasets/basicinfo/all"
    auth_token = buildAuthToken(user_id)
    headers = {'Authorization': auth_token}
    r = requests.get(address, headers=headers)
    if r.status_code == 200:
        return True
    return False