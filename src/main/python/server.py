from flask import Flask
from flask import request

app = Flask(__name__)


@app.route('/manoj/*', methods=('GET', 'POST'))
@app.route('/test', methods=('GET', 'POST'))
def get():
  if request.method == 'POST':
    print("Received Request")
    data = request.get_json()
    print(data)
    return data
  return "GET Request"


@app.route('/slack/events', methods=('GET', 'POST'))
def process_slack_event():
  if request.method == 'POST':
    print(request.get_json())
    if 'challenge' in request.get_json():
      return request.get_json()['challenge']
    return ""


if __name__ == '__main__':
  app.run(debug=True, host='0.0.0.0', port=8080)

