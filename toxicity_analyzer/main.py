from flask import Flask, request, jsonify
from transformers import pipeline
from concurrent.futures import ThreadPoolExecutor
import os

executor = ThreadPoolExecutor(max_workers=3)
classifier = pipeline("text-classification", model='EIStakovskii/french_toxicity_classifier_plus_v2')
app = Flask(__name__)

DOCKER_ENV = os.environ.get('DOCKER_ENV')

@app.route('/analyze', methods=['POST'])
def execute_code():
    data = request.get_json()
    sentences = [data.get('message')]

    print(f'Input: {sentences[0]}')

    future = executor.submit(classifier, sentences)
    model_outputs = future.result()

    if model_outputs[0]['label'] == 'neutral':
        model_outputs[0]['label'] = 'NT'
    else:
        model_outputs[0]['label'] = 'T'

    print(model_outputs[0])

    return jsonify(model_outputs[0]), 200


if __name__ == '__main__':
    if DOCKER_ENV is not None:
        app.run(debug=True, port=8090, host="0.0.0.0")
    app.run(debug=True, port=8090)