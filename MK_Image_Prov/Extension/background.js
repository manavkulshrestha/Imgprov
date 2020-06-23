chrome.contextMenus.create({
    'id': 'verify',
    'title': 'Verify Image',
    'contexts': ['image']
});

chrome.contextMenus.onClicked.addListener(data => {
    // check that request is image that needs to be verified
    if (data.menuItemId === 'verify' && data.mediaType === 'image') {
        // get response for image from server
        fetch('http://127.0.0.1:5000/verify', {
            method: 'POST',
            body: JSON.stringify({'imageSrc':data.srcUrl}),
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }
        // then send response to popup for display
        })
        .then(res => res.json())
        .then(res_json => {
            data = {imgSrc: data.srcUrl, res: res_json};
            console.log(data);
            chrome.tabs.query({active: true, currentWindow: true}, tabs => {
                chrome.tabs.sendMessage(tabs[0].id, data);
            });
        });
    }
})