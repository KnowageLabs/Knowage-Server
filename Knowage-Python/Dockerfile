FROM python:3.7

COPY . .

RUN pip install -r requirements.txt

WORKDIR /pythonwebservice

EXPOSE 5000

CMD [ "python3", "knowage-python.py" ]