from flask import Flask, request, jsonify
import base64
import ecdsa
import datetime
import numpy as np
import sys
import hashlib
from exif import Image
import json

app = Flask(__name__)

private_key = None
public_key = None
RES_DIR = '..\\Resources'

@app.route('/featureVector', methods=['POST'])
def obtain_feature_vector():
    data = request.get_json()
    # arbitrary vectyor
    feature_vector = [0,1,2,3,4]

    # decode data
    image = base64.b64decode(data['image'])
    device_image_sign = base64.b64decode(data['imageSign'])

    # obtain device public key. this will be more elaborate in the future: temp
    with open(f'{RES_DIR}\\device_public_key.pem') as f:
        device_public_key = ecdsa.VerifyingKey.from_pem(f.read())

    # check verification from device
    device_verified = device_public_key.verify(device_image_sign, image, hashfunc=hashlib.sha256, sigdecode=ecdsa.util.sigdecode_der)
    if not device_verified:
        return {'deviceVerified': device_verified}

    # sign vector
    vector_sign = private_key.sign(bytes(feature_vector), hashfunc=hashlib.sha256, sigencode=ecdsa.util.sigencode_der)

    return {
        'deviceVerified': device_verified,
        'timestamp': str(datetime.datetime.utcnow()),
        'featureVector': feature_vector,
        'featureVectorSign': base64.b64encode(vector_sign).decode('utf-8')
    }

    return {1:1}

@app.route('/verify', methods=['POST'])
def verify():
    data = request.get_json()

    # fix so images dont rename each other. race conditions. save image as temp?
    print(type(data['image']))
    # print(data['image'])
    byt = base64.decodebytes(bytes(data['image'], 'raw_unicode_escape'))
    print()

    with open(f'{RES_DIR}\\image.jpg', 'wb') as f:
        f.write(byt)

    image = None
    with open(f'{RES_DIR}\\image.jpg', 'rb') as f:
        image = Image(f)
    os.remove(f'{RES_DIR}\\image.jpg')
        
    if image.has_exif:
        # try:
        image_data = json.loads(image.image_description)

        feature_vector = image_data['featureVector']
        feature_vector_sign = base64.b64decode(image_data['featureVectorSign'])

        vector_verified = public_key.verify(feature_vector_sign, data['image'], hashfunc=hashlib.sha256, sigdecode=ecdsa.util.sigdecode_der)

        return {
            'imageSigned': True,
            'vectorSignVerified': vector_verified,
        }
        # except Exception as e:
        #     print(e)

    return {'imageSigned': False}

def feature_vector_fromexif():
    return 1
    
if __name__ == '__main__':

    # load or generate private keys
    SERVER_PRIVATE_KEY_FILENAME = f'{RES_DIR}\\server_private_key.pem'
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