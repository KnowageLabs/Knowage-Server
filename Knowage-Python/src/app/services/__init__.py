#!/usr/bin/env python3

from flask import Flask
from flask_cors import CORS

def create_app():
    app = Flask(__name__)
    CORS(app)
    from app.services.widgetedit_resource import editMode
    from app.services.widgetview_resource import viewMode
    from app.services.dataset_resource import dataset
    from app.services.catalog_resource import catalog
    app.register_blueprint(editMode, url_prefix='/widget/edit')
    app.register_blueprint(viewMode, url_prefix='/widget/view')
    app.register_blueprint(dataset, url_prefix='/dataset')
    app.register_blueprint(catalog, url_prefix='/catalog')
    return app