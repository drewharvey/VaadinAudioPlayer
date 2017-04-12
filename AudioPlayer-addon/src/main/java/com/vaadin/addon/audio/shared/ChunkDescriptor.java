package com.vaadin.addon.audio.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ChunkDescriptor implements Serializable {

	// Common chunk ID
	private int id;

	// General info
	private int startTimeOffset;
	private int endTimeOffset;
	private int leadInDuration;
	private int leadOutDuration;

	// PCM source stream data offsets
	private int startSampleOffset;
	private int endSampleOffset;

	public void setId(int id) {
		this.id = id;
	}

	public void setStartTimeOffset(int startTimeOffset) {
		this.startTimeOffset = startTimeOffset;
	}

	public void setEndTimeOffset(int endTimeOffset) {
		this.endTimeOffset = endTimeOffset;
	}

	public void setLeadInDuration(int leadInDuration) {
		this.leadInDuration = leadInDuration;
	}

	public void setLeadOutDuration(int leadOutDuration) {
		this.leadOutDuration = leadOutDuration;
	}

	public void setStartSampleOffset(int startSampleOffset) {
		this.startSampleOffset = startSampleOffset;
	}

	public void setEndSampleOffset(int endSampleOffset) {
		this.endSampleOffset = endSampleOffset;
	}

	public int getId() {
		return id;
	}

	public int getStartTimeOffset() {
		return startTimeOffset;
	}

	public int getEndTimeOffset() {
		return endTimeOffset;
	}

	public int getLeadInDuration() {
		return leadInDuration;
	}

	public int getLeadOutDuration() {
		return leadOutDuration;
	}

	public int getStartSampleOffset() {
		return startSampleOffset;
	}

	public int getEndSampleOffset() {
		return endSampleOffset;
	}

	@Override
	public String toString() {
		return "[Descriptor for chunk " + id + " time: " + startTimeOffset + "(-" + leadInDuration + ") - "
				+ endTimeOffset + "(+" + leadOutDuration + ")" + " samples: " + startSampleOffset + "-"
				+ endSampleOffset + ", size: " + (endSampleOffset - startSampleOffset) + " samples ]";
	}

}
