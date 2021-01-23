package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    public void placeSchematic(WorldBuffer w, Location l, String key) {
        File schem = new File(JustGuns.SCHEMATIC_FOLDER, key);
        if(schem.isFile()) {
            w.placeSchematic(l, schem);
        }
    }
}
