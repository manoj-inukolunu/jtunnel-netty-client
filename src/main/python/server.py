from flask import Flask
from flask import request

app = Flask(__name__)


@app.route('/manoj/*', methods=('GET', 'POST'))
@app.route('/test', methods=('GET', 'POST'))
def get():
  if request.method == 'POST':
    print("Received Request")
    data = request.get_data()
    if len(data) < 500:
      print(data)
    return data
  return "GET Request"


if __name__ == '__main__':
  app.run(debug=True, host='0.0.0.0', port=3030)
