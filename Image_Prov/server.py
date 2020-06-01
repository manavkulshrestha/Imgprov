from flask import Flask
from flask import request

from fastecdsa import curve, ecdsa, keys
from fastecdsa.point import Point

import numpy as np


app = Flask(__name__)

device_public_keys = set()

# p = int('ffffffff ffffffff ffffffff ffffffff ffffffff ffffffff fffffffe fffffc2f'.replace(' ', ''), 16)
# a = 0
# b = 7
# n = int('ffffffff ffffffff ffffffff fffffffe baaedce6 af48a03b bfd25e8c d0364141'.replace(' ', ''), 16)

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
	data = request.get_json()

	device_img_sign = tuple(int(data['imageSign'][x]) for x in ['r', 's'])
	device_public_key = Point(int(data['publicKey']['x']), int(data['publicKey']['y']), curve=curve.secp256k1)
	valid = ecdsa.verify(device_img_sign, data['image'], )
	print(valid)

	return {
		'timestamp': '',
		'featureVector': [0,1,2,3,4],
		'featureVectorSign': ''
	}

@app.route('/verify', methods=['POST'])
def verify():
	# image = request.get_data()
	print('RECIEVED2')
	# TODO verify device sign

if __name__ == '__main__':
	app.run(debug=True)