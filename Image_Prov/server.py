from flask import Flask
from flask import request

from fastecdsa import curve, ecdsa, keys
from fastecdsa.point import Point

import datetime

import numpy as np


app = Flask(__name__)

device_public_keys = set()

# p = int('ffffffff ffffffff ffffffff ffffffff ffffffff ffffffff fffffffe fffffc2f'.replace(' ', ''), 16)
# a = 0
# b = 7
# n = int('ffffffff ffffffff ffffffff fffffffe baaedce6 af48a03b bfd25e8c d0364141'.replace(' ', ''), 16)

server_priv_key, server_pub_key = keys.gen_keypair(curve.secp256k1)

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
    data = request.get_json()
    feature_vector = [1,2,3,4]

    device_img_sign = tuple(int(data['imageSign'][x]) for x in ['r', 's'])
    device_public_x = int(data['publicKey']['x'])
    device_public_y = int(data['publicKey']['y'])

    # ccurately being verified on the serverccuratelyimage = bytes(data['image']['data'])
    image = bytes(1)

    device_public_key = Point(device_public_x, device_public_y, curve=curve.secp256k1)
    valid = ecdsa.verify(device_img_sign, image, device_public_key, curve=curve.secp256k1)
    print(valid)

    r, s = ecdsa.sign(bytes(feature_vector), server_priv_key)

    return {
	'timestamp': str(datetime.datetime.utcnow()),
	'featureVector': feature_vector,
        'featureVectorSign': str(r)+str(s)
    }

@app.route('/verify', methods=['POST'])
def verify():
    # image = request.get_data()
    print('RECIEVED2')
    # TODO verify device sign

if __name__ == '__main__':
    app.run(debug=True)
