const fetch = require('node-fetch');
const EC = require('elliptic').ec

//TODO, read file. no clue how to do that...

// var reader = new FileReader();
// var fileByteArray = [];
// reader.readAsArrayBuffer(myFile);
// reader.onloadend = function (evt) {
//     if (evt.target.readyState == FileReader.DONE) {
//        var arrayBuffer = evt.target.result,
//            array = new Uint8Array(arrayBuffer);
//        for (var i = 0; i < array.length; i++) {
//            fileByteArray.push(array[i]);
//         }
//     }
// }

const base_url = 'http://127.0.0.1:5000';
const vectorEndpoint = base_url+'/featureVector';
const verifyEndpoint = base_url+'/verify';

function request_options(req_method, data) {
	return {
    	method: req_method,
    	body: JSON.stringify(data),
    	headers: {
        	'Content-Type': 'application/json'
        }
    }
}

const test = {
	'data': 'image',
	'sign': 'x',
	'publicKey': {
		'x': 'xpoint',
		'y': 'ypoint'
	}
};

fetch('http://127.0.0.1:5000/featureVector', request_options('POST', test))
    .then(res => res.json())
    .then(res => console.log(res));