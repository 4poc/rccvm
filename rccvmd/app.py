import flask
from flask import request, jsonify
from werkzeug.exceptions import default_exceptions
from werkzeug.exceptions import HTTPException
import json
import backend

# discovers and import backends
backend.load()

runner = backend.Runner()

app = flask.Flask(__name__)

def make_json_error(ex):
    response = jsonify(error=type(ex).__name__, message=str(ex))
    response.status_code = (ex.code
            if isinstance(ex, HTTPException)
            else 500)
    return response
for code in default_exceptions.iterkeys():
    app.error_handler_spec[None][code] = make_json_error

# Lists all available backends.
@app.route('/backends')
def list_backends():
    return jsonify(**{'backends': [{
        'name': x['name'],
        'description': x['description']} for x in backend.registry]})

@app.route('/execute', methods=['POST'])
def execute():
    result = runner.delegate(request.get_json(force=True))
    return jsonify(**result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)

