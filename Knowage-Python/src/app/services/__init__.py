#!/usr/bin/env python3

from flask import Flask
from flask_cors import CORS

def create_app():
    app = Flask(__name__)
    CORS(app)
    from app.services.widget_resource import widget
    from app.services.dataset_resource import dataset
    from app.services.catalog_resource import catalog
    app.register_blueprint(widget, url_prefix='/2.0/widget')
    app.register_blueprint(dataset, url_prefix='/dataset')
    app.register_blueprint(catalog, url_prefix='/catalog')
    return app