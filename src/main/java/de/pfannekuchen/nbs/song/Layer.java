package de.pfannekuchen.nbs.song;

import java.util.HashMap;

public class Layer {
	public HashMap<Integer, Note> notesAtTicks = new HashMap<>();
	public byte volume = 100;
	public int panning = 100;
	public String name = "";

	public Note getNote(int tick) {
		return this.notesAtTicks.get(Integer.valueOf(tick));
	}

	public void setNote(int tick, Note note) {
		this.notesAtTicks.put(Integer.valueOf(tick), note);
	}

}
