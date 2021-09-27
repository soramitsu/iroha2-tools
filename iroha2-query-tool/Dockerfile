FROM python:3.7-buster

WORKDIR /app
COPY iroha-python-wheels/*.whl ./
RUN pip3 install *.whl
COPY *.py config.json ./

ENTRYPOINT [ "python3", "iroha2-query-tool.py" ]
