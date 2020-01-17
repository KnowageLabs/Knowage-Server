from threading import Lock

active_servers = {}
bokeh_resources = {}
lck = Lock()