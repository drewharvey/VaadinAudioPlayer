package com.vaadin.addon.audio.client;

import elemental.html.Uint8Array;

/**
 * Functions for handling decoding of data received from server.
 */
public final class StreamDataDecoder {

	/**
	 * Perform Base64 decode of assumed Base64 data
	 * 
	 * @param encodedData encoded data as string
	 * @return a JavaScript native array with decoded data
	 */
	public static Uint8Array decode(String encodedData) {
		return decodeBase64(encodedData);
	}
	
	/**
	 * Method borrowed from
	 * http://blog.danguer.com/2011/10/24/base64-binary-decoding-in-javascript/
	 * 
	 * TODO: see if we can change this to a GWT intrinsic that doesn't suck
	 */
	private static final native Uint8Array decodeBase64(String str) /*-{
		var Base64Binary = {
			_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
			
			decodeArrayBuffer: function(input) {
				var bytes = (input.length/4) * 3;
				var ab = new ArrayBuffer(bytes);
				this.decode(input, ab);
				
				return ab;
			},
		
			removePaddingChars: function(input){
				var lkey = this._keyStr.indexOf(input.charAt(input.length - 1));
				if(lkey == 64){
					return input.substring(0,input.length - 1);
				}
				return input;
			},
		
			decode: function (input, arrayBuffer) {
				//get last chars to see if are valid
				input = this.removePaddingChars(input);
				input = this.removePaddingChars(input);
		
				var bytes = parseInt((input.length / 4) * 3, 10);
				
				var uarray;
				var chr1, chr2, chr3;
				var enc1, enc2, enc3, enc4;
				var i = 0;
				var j = 0;
				
				if (arrayBuffer)
					uarray = new Uint8Array(arrayBuffer);
				else
					uarray = new Uint8Array(bytes);
				
				input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
				
				for (i=0; i<bytes; i+=3) {	
					//get the 3 octects in 4 ascii chars
					enc1 = this._keyStr.indexOf(input.charAt(j++));
					enc2 = this._keyStr.indexOf(input.charAt(j++));
					enc3 = this._keyStr.indexOf(input.charAt(j++));
					enc4 = this._keyStr.indexOf(input.charAt(j++));
			
					chr1 = (enc1 << 2) | (enc2 >> 4);
					chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
					chr3 = ((enc3 & 3) << 6) | enc4;
			
					uarray[i] = chr1;			
					if (enc3 != 64) uarray[i+1] = chr2;
					if (enc4 != 64) uarray[i+2] = chr3;
				}
			
				return uarray;	
			}
		};
		
		return Base64Binary.decodeArrayBuffer(str);  
	}-*/;
	
}
