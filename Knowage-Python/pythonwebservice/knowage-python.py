import sys
from app.services import create_app
from app.utilities import constants

app = create_app()

if __name__ == '__main__':
    if len(sys.argv) > 1:
        port = int(sys.argv[0])
    else:
        port = 5000
    open(constants.LOG_FILE, 'w+').close() #clean log file
    original_stderr = sys.stderr
    #sys.stderr = open(constants.LOG_FILE, 'a')
    app.run(host='0.0.0.0', debug=True, port=port)