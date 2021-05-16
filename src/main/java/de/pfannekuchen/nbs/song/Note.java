package de.pfannekuchen.nbs.song;

import net.minecraft.util.math.MathHelper;

public class Note {
	public byte instrument;
	public byte key;
	public byte velocity;
	public int panning;
	public short pitch;

	public Note(byte instrument, byte key) {
		this(instrument, key, (byte)100, 100, (short)0);
	}

	public Note(byte instrument, byte key, byte velocity, int panning, short pitch) {
		this.instrument = instrument;
		this.key = key;
		this.velocity = velocity;
		this.panning = panning;
		this.pitch = pitch;
	}

	public void setVelocity(byte velocity) {
		this.velocity = (byte) MathHelper.clamp(velocity, 0, 100);
	}
	
}
