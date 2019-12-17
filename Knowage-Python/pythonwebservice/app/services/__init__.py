from flask import Flask
from flask_cors import CORS

def create_app():
    app = Flask(__name__)
    CORS(app)
    from app.services.editmode_service import editMode
    from app.services.viewmode_service import viewMode
    from app.services.dataset_service import dataset
    app.register_blueprint(editMode, url_prefix='/edit')
    app.register_blueprint(viewMode, url_prefix='/view')
    app.register_blueprint(dataset, url_prefix='/dataset')
    return app