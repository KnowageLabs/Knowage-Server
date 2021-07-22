#!/usr/bin/env python3

import sys
from app.services import create_app
from app.utilities import configs
import logging

logging.basicConfig(format=configs.LOG_FORMAT, level=logging.WARNING)
#logging.basicConfig(filename=constants.LOG_FILE, filemode='w', format=constants.LOG_FORMAT, level=logging.WARNING)

application = create_app()

if __name__ == '__main__':
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
    else:
        port = 5000
    open(configs.LOG_FILE, 'w+').close() #clean log file
    original_stderr = sys.stderr
    #sys.stderr = open(constants.LOG_FILE, 'a')
    application.run(host='0.0.0.0', debug=False, port=port)
    #application.run(host='0.0.0.0', debug=False, port=port, ssl_context=('cert.pem', 'key.pem'))
