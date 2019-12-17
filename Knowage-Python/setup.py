import setuptools

with open("README.md", "r") as f:
    long_description = f.read()

setuptools.setup(
    name="knowage-python",
    version="0.1.2",
    license='AGPL v3',
    author="Marco Balestri",
    author_email="marco.balestri@eng.it",
    description="Web service for Knowage python widget and python dataset",
    long_description=long_description,
    long_description_content_type="text/markdown",
    packages=setuptools.find_packages(),
    install_requires=[
          'flask',
          'base64',
          'bokeh',
          'tornado',
          'requests',
          'pandas',
      ],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Affero General Public License v3",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.7',
)