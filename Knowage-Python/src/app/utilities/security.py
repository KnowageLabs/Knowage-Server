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

import jwt
from datetime import datetime
from app.utilities import configs

def jwt_token_to_python_script(token):
    try:
        decoded_token = jwt.decode(token, get_hmac_key(), algorithms='HS256')
    except Exception as e:
        return False, None
    # check expiration date
    expiration_time = decoded_token.get("exp")
    script = decoded_token.get("script")
    now = datetime.now().timestamp()
    if now > expiration_time:
        return False, None
    return True, script

def get_hmac_key():
    with open(configs.HMACKEY_FILE, "rb") as f:
        key = f.read()
    return key