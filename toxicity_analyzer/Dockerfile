FROM python:3.11
LABEL authors="Rayan<"
LABEL description="Toxicity Analyzer + Flask API For SR03 Project"

COPY ./requirements.txt /app/requirements.txt

WORKDIR /app
RUN pip install -r requirements.txt

COPY . /app

ENV DOCKER_ENV="DOCKER"

CMD ["python", "main.py"]