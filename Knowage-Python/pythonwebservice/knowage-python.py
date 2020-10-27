#!/usr/bin/env python3

import sys
from app.services import create_app
from app.utilities import constants
import logging

LOG_FORMAT = "[%(filename)s:%(lineno)s - %(funcName)20s() ] %(message)s"
logging.basicConfig(format=LOG_FORMAT)

application = create_app()

if __name__ == '__main__':
    if len(sys.argv) > 1:
        port = int(sys.argv[0])
    else:
        port = 5000
    open(constants.LOG_FILE, 'w+').close() #clean log file
    original_stderr = sys.stderr
    #sys.stderr = open(constants.LOG_FILE, 'a')
    application.run(host='0.0.0.0', debug=False, port=port)
    #application.run(host='0.0.0.0', debug=False, port=port, ssl_context=('cert.pem', 'key.pem'))
