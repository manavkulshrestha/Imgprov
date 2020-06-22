chrome.contextMenus.create({
    'id': 'verify',
    'title': 'Verify Image',
    'contexts': ['image']
});

chrome.contextMenus.onClicked.addListener(data => {
    // check that request is image that needs to be verified
    if (data.menuItemId === 'verify' && data.mediaType === 'image') {
        console.log(data);

        // get the base64 version of the image
        function getImageBase64(url, callback) {
            var img = new Image();
            img.crossOrigin = 'Anonymous';

            img.onload = function() {
                let canvas = document.createElement('CANVAS');
                let ctx = canvas.getContext('2d');
                let dataURL;

                canvas.height = this.height;
                canvas.width = this.width;

                ctx.drawImage(this, 0, 0);
                callback(canvas.toDataURL('image/jpeg', 1.0));
                canvas = null;
            };

            img.src = url;
        }

        getImageBase64(data.srcUrl, function(base64_data) {
            // strip the info at the beginning
            console.log(base64_data);
            // console.log(base64_data.substring(base64_data.indexOf(',')+1));

            fetch('http://127.0.0.1:5000/verify', {
                method: 'POST',
                body: JSON.stringify({'image': base64_data.substring(base64_data.indexOf(',')+1)}),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            })
            .then(res => res.json())
            .then(res => console.log(res));
        });

        
    }    
})

// chrome.browserAction.onClicked.addListener(() => {
//     chrome.tabs.query({active: true, currentWindow: true}, tabs => {
//         chrome.tabs.sendMessage(tabs[0].id, {from:'background', data:'imgSrcs'}, response => {

//         });
//     });
// });
