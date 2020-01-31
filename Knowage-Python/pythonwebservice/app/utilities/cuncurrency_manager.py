from threading import Lock

active_servers = {} #{widget_id : bokeh_server}
bokeh_resources = {} #{widget_id : BokehResourceList}
lck = Lock()