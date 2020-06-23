test = null;

chrome.runtime.onMessage.addListener(msg => {
	test = msg;
	// create message using data sent from background script
	alertText = `Image Provinence Result:\nThe image was`;

	alert(JSON.stringify(msg.res));

	if (msg.res['imgSigned']) {
		alertText += ` signed by our server and has ${msg.res['vectorSignVerified'] ? 'not ' : ''}been modified since`;
	} else {
		alertText += ' not signed by our server';
	}

	alert(alertText);
});