from threading import Lock

active_servers = {}
active_threads = {}
ports_dict = {}
lck = Lock()