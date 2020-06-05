from flask import Flask, request, jsonify
import base64
import ecdsa
import datetime
import numpy as np
import sys
import hashlib

app = Flask(__name__)

private_key = None
public_key = None

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
    data = request.get_json()
    # arbitrary vectyor
    feature_vector = [0,1,2,3,4]

    # decode data
    image = base64.b64decode(data['image'])
    device_image_sign = base64.b64decode(data['imageSign'])

    # obtain device public key. this will be more elaborate in the future: temp
    DEVICE_PUBLIC_KEY_FILENAME = '..\\resources\\device_public_key.pem'
    with open(DEVICE_PUBLIC_KEY_FILENAME) as f:
        device_public_key = ecdsa.VerifyingKey.from_pem(f.read())

    # check verification from device
    is_verified = device_public_key.verify(device_image_sign, image, hashfunc=hashlib.sha256, sigdecode=ecdsa.util.sigdecode_der)
    if not is_verified:
        return {'verified': is_verified}

    # sign vector
    vector_sign = private_key.sign(bytes(feature_vector), hashfunc=hashlib.sha256, sigencode=ecdsa.util.sigencode_der)

    return {
        'verified': is_verified,
        'timestamp': str(datetime.datetime.utcnow()),
        'featureVector': feature_vector,
        'featureVectorSign': base64.b64encode(vector_sign).decode('utf-8')
    }

    return {1:1}

@app.route('/verify', methods=['POST'])
def verify():
    data = request.get_json()
    feature_vector = feature_vector_fromexif(data['image'])

    # return {
    #     'verificationResult': public_key.verify(bytes(feature_vector))
    # }

    return {1:1}

def feature_vector_fromexif():
    return 1
    
if __name__ == '__main__':

    # load or generate private keys
    SERVER_PRIVATE_KEY_FILENAME = '..\\resources\\server_private_key.pem'
    try:
        # use existing keys
        with open(SERVER_PRIVATE_KEY_FILENAME) as f:
            private_key = ecdsa.SigningKey.from_pem(f.read())
    except IOError:
        # generate keys
        private_key = ecdsa.SigningKey.generate(curve=ecdsa.SECP256k1)
        try:
            with open(SERVER_PRIVATE_KEY_FILENAME, 'wb') as f:
                f.write(private_key.to_pem())
        except:
            print('ERROR: could not save private key.', file=sys.stderr)

    public_key = private_key.get_verifying_key()

    app.run(debug=True)