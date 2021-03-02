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

from flask import Blueprint, request, render_template
import base64
import os
from bokeh.embed import server_document
from bokeh.server.server import Server
from threading import Thread
from tornado.ioloop import IOLoop
from app.utilities import utils, security, constants, cuncurrency_manager
from app.utilities.objects import PythonWidgetExecution, BokehResourceList
from datetime import datetime
import logging

editMode = Blueprint('editMode', __name__)
#url: knowage_addr:port/edit

@editMode.route('/html', methods = ['POST'])
def python_html():
    #retrieve input parameters
    script, output_variable = utils.get_widget_config(request.get_json())
    user_id = utils.get_knowage_token(request.headers)
    dataset_name, datastore_request = utils.get_dataset(request.get_json())
    drivers = utils.get_analytical_drivers(request.get_json())
    python_widget = PythonWidgetExecution(analytical_drivers=drivers, script=script, output_variable=output_variable, user_id=user_id,
                                          dataset_name=dataset_name, datastore_request=datastore_request)
    # check authentication
    if not security.is_user_authorized_for_functionality(python_widget, constants.EDIT_PYTHON_SCRIPTS):
        return "Error: authentication failed", 401
    # resolve analytical drivers
    for d in drivers:
        python_widget.script = python_widget.script.replace("$P{" + d + "}", "drivers_.get(\'" + d + "\')")
    try:
        #retrieve dataset
        if python_widget.dataset_name != None:
            dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
            df = utils.load_data_as_dataframe(python_widget)
            df.to_pickle(dataset_file)
            python_widget.script = "import pandas as pd\n" + python_widget.dataset_name + " = pd.read_pickle(\"" + dataset_file + "\")\n" + python_widget.script
    except Exception as e:
        logging.error("Error during dataframe conversion: {}".format(e))
        return str(e), 400
    try:
    #execute script
        namespace = {python_widget.output_variable: "", "drivers_": drivers}
        exec(python_widget.script, namespace)
    except Exception as e:
        logging.error("Error during script execution: {}".format(e))
        return str(e), 400
    #collect script result
    html = namespace[python_widget.output_variable]
    #remove dataset tmp file
    try:
        os.remove(dataset_file)
    except Exception:
        pass
    return html, 200

@editMode.route('/img', methods = ['POST'])
def python_img():
    # retrieve input parameters
    script, img_file = utils.get_widget_config(request.get_json())
    user_id = utils.get_knowage_token(request.headers)
    dataset_name, datastore_request = utils.get_dataset(request.get_json())
    document_id, widget_id = utils.get_widget_info(request.get_json())
    drivers = utils.get_analytical_drivers(request.get_json())
    python_widget = PythonWidgetExecution(analytical_drivers=drivers, script=script, output_variable=img_file, user_id=user_id,
                                          dataset_name=dataset_name, datastore_request=datastore_request)
    # check authentication
    if not security.is_user_authorized_for_functionality(python_widget, constants.EDIT_PYTHON_SCRIPTS):
        return "Error: authentication failed", 401
    # resolve analytical drivers
    for d in drivers:
        python_widget.script = python_widget.script.replace("$P{" + d + "}", "drivers_.get(\'" + d + "\')")
    try:
        # retrieve dataset
        if python_widget.dataset_name != None:
            dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
            df = utils.load_data_as_dataframe(python_widget)
            df.to_pickle(dataset_file)
            python_widget.script = "import pandas as pd\n" + python_widget.dataset_name + " = pd.read_pickle(\"" + dataset_file + "\")\n" + python_widget.script
    except Exception as e:
        logging.error("Error during dataframe conversion: {}".format(e))
        return str(e), 400
    try:
    # execute script
        namespace = {"drivers_": drivers}
        exec(python_widget.script, namespace)
    except Exception as e:
        logging.error("Error during script execution: {}".format(e))
        return str(e), 400
    # collect script result
    with open(img_file, "rb") as f:
        encoded_img = base64.b64encode(f.read())
    #delete temp files
    try:
        os.remove(img_file)
        os.remove(dataset_file)
    except Exception:
        pass
    return "<img src=\"data:image/;base64, " + encoded_img.decode('utf-8') + "\" style=\"width:100%;height:100%;\">", 200

@editMode.route('/bokeh', methods = ['POST'])
def python_bokeh():
    utils.bokeh_garbage_collector()
    # retrieve input parameters
    script = request.get_json()['script']
    widget_id = request.get_json()['widget_id']
    script_file_name = constants.TMP_FOLDER + "bokeh_script_" + str(widget_id) + ".txt"
    user_id = utils.get_knowage_token(request.headers)
    dataset_name, datastore_request = utils.get_dataset(request.get_json())
    drivers = utils.get_analytical_drivers(request.get_json())
    python_widget = PythonWidgetExecution(analytical_drivers=drivers, script=script, user_id=user_id,
                                          dataset_name=dataset_name, datastore_request=datastore_request, widget_id=widget_id)
    # check authentication
    if not security.is_user_authorized_for_functionality(python_widget, constants.EDIT_PYTHON_SCRIPTS):
        return "Error: authentication failed", 401
    #destroy old bokeh server
    if utils.server_exists(python_widget.widget_id):
        utils.destroy_server(python_widget.widget_id)
    # resolve analytical drivers
    for d in drivers:
        python_widget.script = python_widget.script.replace("$P{" + d + "}", "drivers_.get(\'" + d + "\')")
    # retrieve dataset
    if python_widget.dataset_name != None:
        dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
        df = utils.load_data_as_dataframe(python_widget)
        df.to_pickle(dataset_file)
        python_widget.script = "import pandas as pd\n" + python_widget.dataset_name + " = pd.read_pickle(\"" + dataset_file + "\")\n" + python_widget.script

    #function executed by bokeh server
    def modify_doc(doc):
        # retrieve script from file
        with open(script_file_name, "r") as bk_file:
            bk_script = bk_file.read()
        #replace curdoc() with keyword "curdoc_"
        bk_script = bk_script.replace("curdoc()", "curdoc_")
        # execute script
        namespace = {'curdoc_': doc, "drivers_": drivers}
        exec(bk_script, namespace)

    #secondary thread function (bokeh server)
    def bk_worker():
        server = Server({'/bkapp'+str(python_widget.widget_id): modify_doc}, io_loop=IOLoop(), allow_websocket_origin=["*"], port=cuncurrency_manager.bokeh_resources[python_widget.widget_id].port)
        with cuncurrency_manager.lck:
            cuncurrency_manager.active_servers.update({python_widget.widget_id:server}) #{widget_id : bokeh_server}
        server.start()
        server.io_loop.start()

    #flush script content to file so that modify_doc() can retrieve the code to be executed
    with open(script_file_name,"w") as bokeh_file:
        bokeh_file.write(python_widget.script)

    #instance a bokeh server for the widget if not instanciated yet
    if not utils.server_exists(python_widget.widget_id): #allocate bokeh server
        t = Thread(target=bk_worker) #thread that hosts bokeh server
        bk_res = BokehResourceList(thread=t, timestamp=datetime.now(), port=utils.find_free_port(), dataset_name=dataset_name)
        with cuncurrency_manager.lck:
            cuncurrency_manager.bokeh_resources.update({python_widget.widget_id : bk_res}) #{widget_id : BokehResourceList}
        t.start()
    #serve plot
    jscript = server_document(utils.get_python_engine_address() + ":" + str(cuncurrency_manager.bokeh_resources[python_widget.widget_id].port) + "/bkapp" + str(python_widget.widget_id))
    return render_template("embed.html", script=jscript)

@editMode.route('/libraries', methods = ['GET'])
def python_libraries():
    return utils.get_environment_libraries(), 200