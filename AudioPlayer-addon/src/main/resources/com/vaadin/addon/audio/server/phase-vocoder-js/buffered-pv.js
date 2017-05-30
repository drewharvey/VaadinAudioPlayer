/**
 * Modified BufferedPV to enable use with any number of channels.
 *
 * Original:
 * http://echo66.github.io/demos/PhaseVocoder.js/buffered-pv.js
 */

function BufferedPV(frameSize) {

	var _frameSize = frameSize || 4096;
	// var _pvL = new PhaseVocoder(_frameSize, 44100); _pvL.init();
	// var _pvR = new PhaseVocoder(_frameSize, 44100); _pvR.init();
	var _pvList = [];
	var _buffer;
	var _position = 0;
	var _newAlpha = 1;

	// var _midBufL = new CBuffer(Math.round(_frameSize * 2));
	// var _midBufR = new CBuffer(Math.round(_frameSize * 2));
	var _channelBuffers = [];

	this.set_audio_buffer = function(newBuffer) {
		_buffer = newBuffer;
		_position = 0;
		_newAlpha = 1;

		_pvList = [];
		_channelBuffers = [];
		// create PhaseVocoder instance and CBuffer for each channel
		for (var i = 0; i < newBuffer.numberOfChannels; i++) {
			var pv = new PhaseVocoder(_frameSize, newBuffer.sampleRate);
			pv.init();
			_pvList.push(pv);
			// create CBuffer for channel
			var buf = new CBuffer(Math.round(_frameSize * 2));
			_channelBuffers.push(buf);
		}
	}

	this.process = function(outputAudioBuffer) {

		if (!_buffer) {
			console.error("Input AudioBuffer is null");
			return;
		}

		if (!outputAudioBuffer) {
			console.error("Output AudioBuffer is null");
			retrun;
		}

		var sampleCounter = 0;

		//

		var il = _buffer.getChannelData(0);
		// TODO:
		if (_buffer.numberOfChannels > 1) {
			var ir = _buffer.getChannelData(1);
		}
		var ol = outputAudioBuffer.getChannelData(0);
		// TODO
		if (_buffer.numberOfChannels > 1) {
			var or = outputAudioBuffer.getChannelData(1);
		}

		console.log("shifting left and right channels");
		while (_channelBuffers[0].size > 0 && sampleCounter < outputAudioBuffer.length) {
			var i = sampleCounter++;
			ol[i] = _channelBuffers[0].shift();
			// TODO
			if (_buffer.numberOfChannels > 1) {
				or[i] = _channelBuffers[1].shift();
			}

		}

		if (sampleCounter == outputAudioBuffer.length)
			return;

		console.log("Starting to process");
		do {

			var bufL = il.subarray(_position, _position + _frameSize);
			// TODO:
			if (_buffer.numberOfChannels > 1) {
				var bufR = ir.subarray(_position, _position + _frameSize);
			}

			if (_newAlpha != undefined && _newAlpha != _pvList[0].get_alpha()) {
				for (var i in _pvList) {
					_pvList[i].set_alpha(_newAlpha);
				}
				_newAlpha = undefined;
			}


			/* LEFT */
			_pvList[0].process(bufL, _channelBuffers[0]);
			// TODO:
			if (_buffer.numberOfChannels > 1) {
				_pvList[1].process(bufR, _channelBuffers[1]);
			}
			for (var i=sampleCounter; _channelBuffers[0].size > 0 && i < outputAudioBuffer.length; i++) {
				ol[i] = _channelBuffers[0].shift();
				// TODO:
				if (_buffer.numberOfChannels  > 1) {
					or[i] = _channelBuffers[1].shift();
				}
			}

			sampleCounter += _pvList[0].get_synthesis_hop();

			_position
				+= _pvList[0].get_analysis_hop();

		} while (sampleCounter < outputAudioBuffer.length);
	}

	Object.defineProperties(this, {
		'position' : {
			get : function() {
				return _position;
			},
			set : function(newPosition) {
				_position = newPosition;
			}
		},
		'alpha' : {
			get : function() {
				return _pvL.get_alpha();
			},
			set : function(newAlpha) {
				_newAlpha = newAlpha;
			}
		}
	});
}