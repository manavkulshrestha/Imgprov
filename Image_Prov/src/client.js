const fetch = require('node-fetch');

//TODO, read file. no clue how to do that on javascript...

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

const baseUrl = 'http://127.0.0.1:5000';
const verifyEndpoint = baseUrl+'/verify';

function request_options(req_method, data) {
	return {
    	method: req_method,
    	body: JSON.stringify(data),
    	headers: {
        	'Content-Type': 'application/json'
        }
    }
}

// read image
let image = '0';
test = {
	'image': image
};

fetch(verifyEndpoint, request_options('POST', test))
    .then(res => res.json())
    .then(res => console.log(res));
