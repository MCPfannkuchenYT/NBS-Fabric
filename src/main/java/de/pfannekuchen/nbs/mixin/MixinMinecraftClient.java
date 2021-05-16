package de.pfannekuchen.nbs.mixin;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.nbs.NoteBlockPlayer;
import de.pfannekuchen.nbs.song.Layer;
import de.pfannekuchen.nbs.song.Song;
import de.pfannekuchen.nbs.utils.NBSReader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> implements SnooperListener, WindowEventHandler  {

	
	/* Shadow Fields/Methods */
	public MixinMinecraftClient(String string) { super(string); }
	@Shadow @Final private Window window;
	@Shadow @Nullable public ClientPlayerEntity player;
	@Shadow @Nullable public ClientWorld world;
	@Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;tick()V")
	public void onRunTick(CallbackInfo ci) {
		/* Play Button */
		if (NoteBlockPlayer.playKeybinding.wasPressed()) {
			/* Start a new Thread with a File-Picker */
			if (window.isFullscreen()) window.toggleFullscreen(); // Toggle off Fullscreen
			new Thread(() -> {
				FileDialog dialog = new FileDialog((Frame) null, "Select File to Open", FileDialog.LOAD);
			    dialog.setMultipleMode(false);
			    dialog.setVisible(true);
			    if (dialog.getFiles().length == 1) {
			    	// Load File
			    	final Song currentSong = NoteBlockPlayer.loadSong(getInputStream(dialog.getFiles()[0]), dialog.getFiles()[0]);
			    	if (currentSong == null) return;
			    	// Check Noteblocks
			    	if (!NoteBlockPlayer.checkNoteblocks(player.getBlockPos(), world, currentSong)) return;
			    	// Create a new Thread that tunes the Note Blocks and starts the Playback
			    	new Thread(new Runnable() {
						
						@Override
						public void run() {
							// Load all Noteblocks for Tuning
							HashSet<String> combos = new HashSet<>();
							for (Layer layer : currentSong.layerHashMap.values()) for (int i = 0; i < layer.notesAtTicks.size(); i++) try {
								 String msg = layer.getNote(i).instrument + "-" + (layer.getNote(i).key - 33);
								 if (combos.contains(msg)) continue;
								 combos.add(msg);
							} catch (Exception exception) {}
							/* Obtain all Note Blocks around Player */
							for (int x = -5; x < 5; x++) for (int y = -5; y < 5; y++)  for (int z = -5; z < 5; z++) { BlockPos pos = new BlockPos(x + player.getX(), y + player.getY(), z + player.getZ()); 
								BlockState state = world.getBlockState(pos);	
								if (!state.getBlock().equals(Blocks.NOTE_BLOCK)) continue; // Check for Note Blocks
								for (String string : new HashSet<String>(combos)) {
									if (string.startsWith(NBSReader.readType(state) + "-")) {
										tuneNote(pos, Integer.parseInt(string.split("-")[1]));
										combos.remove(string);
										break;
									}
								}
							}
							/* Obtain Note Blocks one last time for Playback */
							HashMap<String, BlockPos> noteBlocks = new HashMap<>();
							for (int x = -5; x < 5; x++) for (int y = -5; y < 5; y++)  for (int z = -5; z < 5; z++) { BlockPos pos = new BlockPos(x + player.getX(), y + player.getY(), z + player.getZ()); 
								BlockState state = world.getBlockState(pos);	
								if (!state.getBlock().equals(Blocks.NOTE_BLOCK)) continue; // Check for Note Blocks
								noteBlocks.put(NBSReader.readType(state) + "-" + state.get(NoteBlock.NOTE).intValue(), pos);
							}
					    	// Start Playback
							MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("Starting Playback!"));
					    	/* BPM... */
					    	long millisPerBeat = (long) (1000f / currentSong.speed);
					    	for (int i = 0; i < currentSong.length; i++) {
					    		try { 
					    			for (Layer layer : currentSong.layerHashMap.values()) 
					    				if (layer.getNote(i) != null && noteBlocks.get(layer.getNote(i).instrument + "-" + (layer.getNote(i).key - 33)) != null) interactionManager.attackBlock(noteBlocks.get(layer.getNote(i).instrument + "-" + (layer.getNote(i).key - 33)), Direction.UP);
					    			Thread.sleep(millisPerBeat);
					    		} catch (Exception e) {
					    			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("Playback Error, skipping tick!"));
					    		}
							}
					    	MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("Playback finished!"));
						}

						private void tuneNote(BlockPos pos, int output) {
							while(true) {
								int currentNote = world.getBlockState(pos).get(NoteBlock.NOTE).intValue();
								if (currentNote != output) interactionManager.interactBlock(player, world, Hand.MAIN_HAND, new BlockHitResult(player.getPos(), Direction.UP, pos, false));
								else return;
								try { Thread.sleep(105); } catch (InterruptedException e) { }
							}
						}
					}).start();

			    }
			}).start();
		}
		
	}

	private InputStream getInputStream(File file) {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
