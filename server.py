from flask import Flask
import numpy as np
app = Flask(__name__)

device_public_keys = set()

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
	image = request.get_json()
	print(image)
	# TODO verify image checksum, verify the device sign, send feature vector

@app.route('/verify', methods=['POST'])
def verify():
	image = request.get_data()
	# TODO verify image checksum, verify device sign, 

if __name__ == '__main__':
	app.run(debug=True, ssl_context='adhoc')