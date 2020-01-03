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
from app.utilities.objects import PythonWidgetExecution

viewMode = Blueprint('view', __name__)
#url: knowage_addr:port/view

@viewMode.route('/html', methods = ['POST'])
def python_html():
    #retrieve input parameters
    script, output_variable = utils.retrieveScriptInfo(request.get_json())
    user_id, knowage_address = utils.retrieveKnowageInfo(request.headers, request.get_json())
    dataset_name, datastore_request = utils.retrieveDatasetInfo(request.get_json())
    document_id, widget_id = utils.retrieveWidgetInfo(request.get_json())
    python_widget = PythonWidgetExecution(script=script, output_variable=output_variable, user_id=user_id, document_id=document_id, widget_id=widget_id,
                                          knowage_address=knowage_address, dataset_name=dataset_name, datastore_request=datastore_request)
    python_widget.script = security.loadScriptFromDB(python_widget)
    #retrieve dataset
    if python_widget.dataset_name != "":
        dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
        df = utils.getDatasetAsDataframe(python_widget)
        df.to_pickle(dataset_file)
        python_widget.script = "import pandas as pd\n" + python_widget.dataset_name + " = pd.read_pickle(\"" + dataset_file + "\")\n" + python_widget.script
    #execute script
    try:
        namespace = {python_widget.output_variable: ""}
        exec(python_widget.script, namespace)
    except Exception as e:
        return str(e), 400
    #collect script result
    html = namespace[python_widget.output_variable]
    #remove dataset tmp file
    try:
        os.remove(dataset_file)
    except Exception:
        pass
    return html, 200

@viewMode.route('/img', methods = ['POST'])
def python_img():
    # retrieve input parameters
    script, img_file = utils.retrieveScriptInfo(request.get_json())
    user_id, knowage_address = utils.retrieveKnowageInfo(request.headers, request.get_json())
    dataset_name, datastore_request = utils.retrieveDatasetInfo(request.get_json())
    document_id, widget_id = utils.retrieveWidgetInfo(request.get_json())
    python_widget = PythonWidgetExecution(script=script, output_variable=img_file, user_id=user_id, document_id=document_id, widget_id=widget_id,
                                          knowage_address=knowage_address, dataset_name=dataset_name, datastore_request=datastore_request)
    python_widget.script = security.loadScriptFromDB(python_widget)
    # retrieve dataset
    if python_widget.dataset_name != "":
        dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
        df = utils.getDatasetAsDataframe(python_widget)
        df.to_pickle(dataset_file)
        python_widget.script = "import pandas as pd\n" + python_widget.dataset_name + " = pd.read_pickle(\"" + dataset_file + "\")\n" + python_widget.script
    # execute script
    try:
        namespace = {}
        exec(python_widget.script, namespace)
    except Exception as e:
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

@viewMode.route('/bokeh', methods = ['POST'])
def python_bokeh():
    # retrieve input parameters
    document_id, widget_id = utils.retrieveWidgetInfo(request.get_json())
    script_file_name = constants.TMP_FOLDER + "bokeh_script_" + str(widget_id) + ".txt"
    user_id, knowage_address = utils.retrieveKnowageInfo(request.headers, request.get_json())
    dataset_name, datastore_request = utils.retrieveDatasetInfo(request.get_json())
    python_widget = PythonWidgetExecution(user_id=user_id, document_id=document_id, widget_id=widget_id,
                                          knowage_address=knowage_address, dataset_name=dataset_name, datastore_request=datastore_request)
    python_widget.script = security.loadScriptFromDB(python_widget)
    #destroy old bokeh server
    if utils.serverExists(python_widget.widget_id):
        utils.destroyServer(python_widget.widget_id)
    # retrieve dataset
    if python_widget.dataset_name != "":
        dataset_file = constants.TMP_FOLDER + python_widget.dataset_name + ".pckl"
        df = utils.getDatasetAsDataframe(python_widget)
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
        namespace = {'curdoc_': doc}
        exec(bk_script, namespace)

    #secondary thread function (bokeh server)
    def bk_worker():
        server = Server({'/bkapp'+str(python_widget.widget_id): modify_doc}, io_loop=IOLoop(), allow_websocket_origin=["localhost:8080"], port=cuncurrency_manager.ports_dict[python_widget.widget_id])
        with cuncurrency_manager.lck:
            cuncurrency_manager.active_servers.update({python_widget.widget_id:server}) #{widget_id : bokeh_server}
        server.start()
        server.io_loop.start()

    #flush script content to file so that modify_doc() can retrieve the code to be executed
    with open(script_file_name,"w") as bokeh_file:
        bokeh_file.write(python_widget.script)

    #instance a bokeh server for the widget if not instanciated yet
    if not utils.serverExists(python_widget.widget_id): #allocate bokeh server
        t = Thread(target=bk_worker) #thread that hosts bokeh server
        with cuncurrency_manager.lck:
            cuncurrency_manager.active_threads.update({python_widget.widget_id : t}) #{widget_id : thread}
            cuncurrency_manager.ports_dict.update({python_widget.widget_id : utils.findFreePort()}) #{widget_id : port_number_of_bokeh_server}
        t.start()
    #serve plot
    jscript = server_document("http://localhost:" + str(cuncurrency_manager.ports_dict[python_widget.widget_id]) + "/bkapp" + str(python_widget.widget_id))
    return render_template("embed.html", script=jscript)