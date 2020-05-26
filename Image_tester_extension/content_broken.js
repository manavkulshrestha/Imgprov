const readExif = require('read-exif');
const buffer = require('buffer');
const { inspect } = require('util');
const exifjs = equire('piexifjs');
var picture = "https://i.pinimg.com/474x/fb/d1/e9/fbd1e95fc924fd69a44cf8fb27c47683.jpg";
images = document.getElementsByTagName("img");


var allMetaData;
var make;
//var sMake = "";



for (var i = 0; i < images.length; i++) {
  const exif = (readExif(images[i])).Exif;
  console.log("Im here");


  this.src = picture;


};


