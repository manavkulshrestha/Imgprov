function findEXIFinJPEG(oFile) {
    var aMarkers = [];

    if (oFile.getByteAt(0) != 0xFF || oFile.getByteAt(1) != 0xD8) {
        return false; // not a valid jpeg
    }
    var iOffset = 2;
    var iLength = oFile.getLength();

    var oExifData = {};
    var oXmpData = {};

    while (iOffset < iLength) {
        if (oFile.getByteAt(iOffset) != 0xFF) {
            // return false; // not a valid marker, something is wrong
            return oExifData;
        }

        var iMarker = oFile.getByteAt(iOffset + 1);

        // we could implement handling for other markers here,
        // but we're only looking for 0xFFE1 for EXIF and XMP data

        if (iMarker == 22400) {
            return readEXIFData(oFile, iOffset + 4, oFile.getShortAt(
                    iOffset + 2, true) - 2);
            iOffset += 2 + oFile.getShortAt(iOffset + 2, true);

        } else if (iMarker == 225) {
            // 0xE1 = Application-specific 1 (for EXIF)

            var headerAsString = oFile.getStringAt(iOffset + 4, 28);
            if (headerAsString.indexOf("http://ns.adobe.com/xap/1.0/") != -1) {
                var sXmpData = oFile.getStringAt(iOffset + 33, oFile
                        .getShortAt(iOffset + 2, true) - 31);

                var xmlDoc;
                try {
                    sXmpData = sXmpData.trim();
                    sXmpData = sXmpData.substr(sXmpData.indexOf("<"),
                            sXmpData.lastIndexOf(">") + 1);
                    xmlDoc = $.parseXML(sXmpData);
                } catch (e) {
                    // error parsing xml
                }

                if (xmlDoc != null) {
                    $([ "SerialNumber", "ImageUniqueID" ])
                            .each(
                                    function(index, tagName) {
                                        var tagVal = $(xmlDoc).find(
                                                tagName);
                                        var tagValue = tagVal.text();

                                        $([ "aux", "exif" ])
                                                .each(
                                                        function(index,
                                                                namespacePrefix) {
                                                            if (typeof tagValue === "undefined"
                                                                    || tagValue.length == 0) {
                                                                // 2 backslash to escape colon
                                                                $(
                                                                        xmlDoc)
                                                                        .find(
                                                                                "["
                                                                                        + namespacePrefix
                                                                                        + "\\:"
                                                                                        + tagName
                                                                                        + "]")
                                                                        .each(
                                                                                function() {
                                                                                    // but only 1 backslash needed here:
                                                                                    tagValue = $(
                                                                                            this)
                                                                                            .attr(
                                                                                                    namespacePrefix
                                                                                                            + "\:"
                                                                                                            + tagName);
                                                                                    // #whyDoIDoThisJob?
                                                                                });
                                                            }
                                                        });

                                        if (typeof tagValue != "undefined"
                                                && tagValue.length > 0) {
                                            tagValue = tidyString(tagValue);
                                            oXmpData[tagName] = tagValue;
                                            if (typeof oExifData[tagName] === "undefined"
                                                    || tidyString(oExifData[tagName]).length == 0) {
                                                oExifData[tagName] = tagValue;
                                            }
                                        }
                                    });
                    oExifData = sortArrayByKeys(oExifData);
                }
            } else {
                oExifData = readEXIFData(oFile, iOffset + 4, oFile
                        .getShortAt(iOffset + 2, true) - 2);
            }

            iOffset += 2 + oFile.getShortAt(iOffset + 2, true);
        } else {
            iOffset += 2 + oFile.getShortAt(iOffset + 2, true);
        }
    }
    return oExifData;
}