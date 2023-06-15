package anthony.SuperCraftBrawl.worldgen;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public class VoidGenerator extends ChunkGenerator
{
	// Called if previous failed
    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[32768];
    }
	
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
    	for(int posX = 0; posX < 16; posX++)
    		for(int posZ = 0; posZ <16 ; posZ++)
    			biome.setBiome(posX, posZ, Biome.PLAINS);
    	ChunkData chunk = createChunkData(world);
    	return chunk;
    }

    @SuppressWarnings("unused")
	private Integer xyz(int x, int y, int z)
    {
        return (x * 16 + z)*128+y; //position inside the chunk
    }
}