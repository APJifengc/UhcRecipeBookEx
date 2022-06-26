package com.gmail.val59000mc.maploader;

import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

import static org.bukkit.Bukkit.broadcastMessage;

public class SurgarCanePopulator extends BlockPopulator{

    private final int percentage;

    public SurgarCanePopulator(int percentage){
        this.percentage = percentage;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk){
        for (int x = 1; x < 15; x++) {
            for (int z = 1; z < 15; z++) {
                Block block = world.getHighestBlockAt(chunk.getBlock(x, 0, z).getLocation());
                Block above = block.getRelative(BlockFace.UP);
                //Block below = block.getRelative(BlockFace.DOWN);

                if (percentage > random.nextInt(100) && (block.getType() == Material.SAND || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.GRASS)){

                    Material water = UniversalMaterial.STATIONARY_WATER.getType();
                    //Material water = Material.WATER;
                    if (
                                    block.getRelative(BlockFace.NORTH).getType() == water ||
                                    block.getRelative(BlockFace.EAST).getType() == water ||
                                    block.getRelative(BlockFace.SOUTH).getType() == water ||
                                    block.getRelative(BlockFace.WEST).getType() == water
                    ){
                        //world.getBlockAt(block.getLocation()).setType(Material.DIAMOND_BLOCK);
                        if (above.getType() == Material.AIR){
                            int height = random.nextInt(3)+1;
                            Location location = above.getLocation();
                            while (height > 0){
                                world.getBlockAt(location).setType(Material.SUGAR_CANE);
                                location = location.add(0, 1, 0);
                                height--;
                            }
                        }
                    }
                }
            }
        }
    }

}