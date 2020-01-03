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

class PythonWidgetExecution:
    _knowage_address = None
    _user_id = None
    _document_id = None
    _widget_id = None
    _script = None
    _output_variable = None
    _dataset_name = None
    _datastore_request = None

    def __init__(self, knowage_address=None, user_id=None, document_id=None, widget_id=None,
                 script=None, output_variable=None, dataset_name=None, datastore_request=None):
        self._knowage_address = knowage_address
        self._user_id = user_id
        self._document_id = document_id
        self._widget_id = widget_id
        self._script = script
        self._output_variable = output_variable
        self._dataset_name = dataset_name
        self._datastore_request = datastore_request

    @property
    def knowage_address(self):
        return self._knowage_address

    @property
    def user_id(self):
        return self._user_id

    @property
    def document_id(self):
        return self._document_id

    @property
    def widget_id(self):
        return self._widget_id

    @property
    def script(self):
        return self._script

    @script.setter
    def script(self, value):
        self._script = value

    @property
    def dataset_name(self):
        return self._dataset_name

    @property
    def datastore_request(self):
        return self._datastore_request