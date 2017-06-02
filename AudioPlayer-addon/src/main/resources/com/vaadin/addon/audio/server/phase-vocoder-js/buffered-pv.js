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
		var inputBuffers = [];
		var outputBuffers = [];

		// push all the input/output buffer channels into arrays
		for (var i = 0; i < _buffer.numberOfChannels; i++) {
			inputBuffers.push(_buffer.getChannelData(i));
			outputBuffers.push(outputAudioBuffer.getChannelData(i));
		}

		// TODO: what is the point of this?
		// while (_channelBuffers[0].size > 0 && sampleCounter < outputAudioBuffer.length) {
		// 	var byteIndex = sampleCounter++;
		// 	for (var i in _channelBuffers) {
		// 		outputBuffers[i][byteIndex] = _channelBuffers[i].shift();
		// 	}
		// }

		// if (sampleCounter == outputAudioBuffer.length)
		// 	return;

		do {

			// get chunk of the input buffer based on the frame size provided
			var currentFrames = [];
			for (var i in inputBuffers) {
				currentFrames.push(inputBuffers[i].subarray(_position, _position + _frameSize));
			}

			// set alpha value (time stretch value) for each PhaseVocoder instance
			if (_newAlpha != undefined && _newAlpha != _pvList[0].get_alpha()) {
				for (var i in _pvList) {
					_pvList[i].set_alpha(_newAlpha);
				}
				_newAlpha = undefined;
			}

			// time stretch the current frame
			for (var i in _pvList) {
				_pvList[i].process(currentFrames[i], _channelBuffers[i]);
			}

			// push the warped buffer data into the output buffers
			for (var byteIndex = sampleCounter; _channelBuffers[0].size > 0 && byteIndex < outputAudioBuffer.length; byteIndex++) {
				for (var i in outputBuffers) {
					outputBuffers[i][byteIndex] = _channelBuffers[i].shift();
				}
			}

			sampleCounter += _pvList[0].get_synthesis_hop();

			_position += _pvList[0].get_analysis_hop();

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