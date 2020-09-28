#!/usr/bin/env python3

import setuptools

def parse_requirements(filename):
    """ load requirements from a pip requirements file """
    lineiter = (line.strip() for line in open(filename))
    return [line for line in lineiter if line and not line.startswith("#")]

with open("README.md", "r") as f:
    long_description = f.read()

setuptools.setup(
    name="knowage-python",
    version="1.1",
    license='AGPL v3',
    author="Marco Balestri",
    author_email="marco.balestri@eng.it",
    url = "https://github.com/KnowageLabs/Knowage-Server/tree/master/Knowage-Python",
    description="Web service for Knowage python widget and python dataset",
    long_description=long_description,
    long_description_content_type="text/markdown",
    packages=setuptools.find_packages(),
    package_data={
        '': ['*.html'],
    },
    install_requires = parse_requirements('requirements.txt'),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Affero General Public License v3",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.7',
)