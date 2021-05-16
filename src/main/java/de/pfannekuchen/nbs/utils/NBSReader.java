package de.pfannekuchen.nbs.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import de.pfannekuchen.nbs.song.Layer;
import de.pfannekuchen.nbs.song.Note;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;

public class NBSReader {

	public static void setNote(int layerIndex, int ticks, byte instrument, byte key, HashMap<Integer, Layer> layerHashMap) {
		Layer layer = layerHashMap.get(Integer.valueOf(layerIndex));
		if (layer == null) {
			layer = new Layer();
			layerHashMap.put(Integer.valueOf(layerIndex), layer);
		} 
		layer.setNote(ticks, new Note(instrument, key));
	}

	public static short readShort(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		return (short)(byte1 + (byte2 << 8));
	}

	public static int readInt(DataInputStream dataInputStream) throws IOException {
		int byte1 = dataInputStream.readUnsignedByte();
		int byte2 = dataInputStream.readUnsignedByte();
		int byte3 = dataInputStream.readUnsignedByte();
		int byte4 = dataInputStream.readUnsignedByte();
		return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
	}

	public static String readString(DataInputStream dataInputStream) throws IOException {
		int length = readInt(dataInputStream);
		StringBuilder builder = new StringBuilder(length);
		for (; length > 0; length--) {
			char c = (char)dataInputStream.readByte();
			if (c == '\r')
				c = ' '; 
			builder.append(c);
		} 
		return builder.toString();
	}

	public static int readType(BlockState blockState) {
		int instru = -1;
		switch (blockState.get(NoteBlock.INSTRUMENT).asString().toLowerCase()) {
		case "harp":
			instru = 0;
			break;
		case "bass":
			instru = 1;
			break;
		case "basedrum":
			instru = 2;
			break;
		case "snare":
			instru = 3;
			break;
		case "hat":
			instru = 4;
			break;
		case "guitar":
			instru = 5;
			break;
		case "flute":
			instru = 6;
			break;
		case "bell":
			instru = 7;
			break;
		case "chime":
			instru = 8;
			break;
		case "xylophone":
			instru = 9;
			break;
		case "iron_xylophone":
			instru = 10;
			break;
		case "cow_bell":
			instru = 11;
			break;
		case "didgeridoo":
			instru = 12;
			break;
		case "bit":
			instru = 13;
			break;
		case "banjo":
			instru = 14;
			break;
		case "pling":
			instru = 15;
			break;
		} 
		return instru;
	}

	public static String toReadable(int id) {
		switch (id) {
		case 0:
			return "Harp, Air";
		case 1:
			return "Bass, Wood";
		case 2:
			return "Basedrum, Stone";
		case 3:
			return "Snare, Sand";
		case 4:
			return "Hat, Glass";
		case 5:
			return "Guitar, Wool";
		case 6:
			return "Flute, Clay";
		case 7:
			return "Bell, Gold";
		case 8:
			return "Chime, Packed Ice";
		case 9:
			return "Xylophone, Bone Block";
		case 10:
			return "Vibraphone, Iron Block";
		case 11:
			return "Cow Bell, Soul Sand";
		case 12:
			return "Didgeridoo, Pumpkin";
		case 13:
			return "Bit, Emerald Block";
		case 14:
			return "Banjo, Hay Bale";
		case 15:
			return "Electric piano, GLowstone";
		} 
		return "ERROR";
	}
	
}
