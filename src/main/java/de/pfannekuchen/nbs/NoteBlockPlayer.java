package de.pfannekuchen.nbs;

import static de.pfannekuchen.nbs.utils.NBSReader.readInt;
import static de.pfannekuchen.nbs.utils.NBSReader.readShort;
import static de.pfannekuchen.nbs.utils.NBSReader.readString;
import static de.pfannekuchen.nbs.utils.NBSReader.setNote;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.glfw.GLFW;

import de.pfannekuchen.nbs.song.Layer;
import de.pfannekuchen.nbs.song.Song;
import de.pfannekuchen.nbs.utils.NBSReader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoteBlockPlayer implements ClientModInitializer {

	static {
		System.setProperty("java.awt.headless", "false"); // Make it not be headless
	}
	
	public static final KeyBinding playKeybinding = new KeyBinding("Play Noteblock Studio File", GLFW.GLFW_KEY_P, "Noteblock Player");
	
	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(playKeybinding);
	}

	public static Song loadSong(InputStream inputStream, File songFile) {
		HashMap<Integer, Layer> layerHashMap = new HashMap<>();
		try {
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			short length = readShort(dataInputStream);
			int nbsversion = 0;
			if (length == 0) {
				nbsversion = dataInputStream.readByte();
				dataInputStream.readByte();
				if (nbsversion >= 3)
					length = readShort(dataInputStream); 
			} 
			short songHeight = readShort(dataInputStream);
			String title = readString(dataInputStream);
			String author = readString(dataInputStream);
			readString(dataInputStream);
			String description = readString(dataInputStream);
			float speed = readShort(dataInputStream) / 100.0F;
			dataInputStream.readBoolean();
			dataInputStream.readByte();
			dataInputStream.readByte();
			readInt(dataInputStream);
			readInt(dataInputStream);
			readInt(dataInputStream);
			readInt(dataInputStream);
			readInt(dataInputStream);
			readString(dataInputStream);
			if (nbsversion >= 4) {
				dataInputStream.readByte();
				dataInputStream.readByte();
				readShort(dataInputStream);
			}
			short tick = -1;
			loop: while (true) {
				short jumpTicks = readShort(dataInputStream);
				if (jumpTicks == 0)
					break; 
				tick = (short)(tick + jumpTicks);
				short layer = -1;
				while (true) {
					short jumpLayers = readShort(dataInputStream);
					if (jumpLayers == 0)
						continue loop; 
					layer = (short)(layer + jumpLayers);
					byte instrument = dataInputStream.readByte();
					byte key = dataInputStream.readByte();
					if (nbsversion >= 4) {
						dataInputStream.readByte();
						dataInputStream.readByte();
						readShort(dataInputStream);
					} 
					setNote(layer, tick, instrument, key, layerHashMap);
				} 
			} 
			if (nbsversion > 0 && nbsversion < 3)
				length = tick; 
			for (int i = 0; i < songHeight; i++) {
				Layer layer = layerHashMap.get(Integer.valueOf(i));
				String name = readString(dataInputStream);
				if (nbsversion >= 4)
					dataInputStream.readByte(); 
				byte volume = dataInputStream.readByte();
				if (nbsversion >= 2)
					dataInputStream.readByte(); 
				if (layer != null) {
					layer.name = name;
					layer.volume = volume;
				} 
			} 
			dataInputStream.readByte();
			return new Song(speed, layerHashMap, songHeight, length, title, author, description, songFile, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	public static boolean checkNoteblocks(BlockPos player, World world, Song song) {
		/* Load all required Instruments and Notes for the Song */
		ArrayList<String> keys = new ArrayList<>();
		HashSet<String> combos = new HashSet<>();
		int[] instrumentCounter = new int[16];
		for (Layer layer : song.layerHashMap.values()) for (int i = 0; i < layer.notesAtTicks.size(); i++) try {
			 String msg = layer.getNote(i).instrument + "_" + (layer.getNote(i).key - 33);
			 if (combos.contains(msg)) continue;
			 keys.add("" + layer.getNote(i).instrument);
			 combos.add(msg);
			 instrumentCounter[layer.getNote(i).instrument]++;
		} catch (Exception exception) {}
			
		/* Obtain and Check all Note Blocks around Player */
		for (int x = -5; x < 5; x++) for (int y = -5; y < 5; y++)  for (int z = -5; z < 5; z++) {
			BlockPos pos = new BlockPos(x + player.getX(), y + player.getY(), z + player.getZ());
			if (!world.getBlockState(pos).getBlock().equals(Blocks.NOTE_BLOCK)) continue; // Check for Note Blocks
			keys.remove("" + NBSReader.readType(world.getBlockState(pos))); // Try to remove the Instrument if it exists
			if (instrumentCounter[NBSReader.readType(world.getBlockState(pos))] > 0) instrumentCounter[NBSReader.readType(world.getBlockState(pos))]--;
		}
		if (!keys.isEmpty()) 
			for (int i = 0; i < instrumentCounter.length; i++) 
				if (instrumentCounter[i] > 0) 
					MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("You need " + instrumentCounter[i] + " Note Blocks of Type: " + NBSReader.toReadable(i)));
		return keys.isEmpty();
	}
	
}
