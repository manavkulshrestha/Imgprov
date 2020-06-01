from flask import Flask
from flask import request
import numpy as np
app = Flask(__name__)

device_public_keys = set()

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
	json = request.get_json()
	print(image)
	# TODO verify image checksum, verify the device sign, send feature vector

	return {1:1}

@app.route('/verify', methods=['POST'])
def verify():
	# image = request.get_data()
	print('RECIEVED2')
	# TODO verify image checksum, verify device sign, 

if __name__ == '__main__':
	app.run(debug=True)