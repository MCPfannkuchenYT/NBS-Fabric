package de.pfannekuchen.nbs.song;

import java.io.File;
import java.util.HashMap;

public class Song implements Cloneable {
	public HashMap<Integer, Layer> layerHashMap = new HashMap<>();
	public int firstCustomInstrumentIndex;
	public final short songHeight;
	public final short length;
	public final String title;
	public final File path;
	public final String author;
	public final String description;
	public final float speed;
	public final float delay;
	public final boolean isStereo;

	public Song(float speed, HashMap<Integer, Layer> layerHashMap, short songHeight, short length, String title, String author, String description, File path, boolean isStereo) {
		this.speed = speed;
		this.delay = 20.0F / speed;
		this.layerHashMap = layerHashMap;
		this.songHeight = songHeight;
		this.length = length;
		this.title = title;
		this.author = author;
		this.description = description;
		this.path = path;
		this.isStereo = isStereo;
	}

}
