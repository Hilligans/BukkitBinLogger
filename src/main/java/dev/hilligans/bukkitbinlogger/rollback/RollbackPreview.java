package dev.hilligans.bukkitbinlogger.rollback;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;

public class RollbackPreview {

    public RollBack rollBack;


    public RollbackPreview(RollBack rollBack) {
        this.rollBack = rollBack;
    }

    public void sendPreview() {
        System.out.println("SubChunks " + rollBack.getNewChunkSections().size());
        for(RollBack.SubChunk section : rollBack.getNewChunkSections()) {
            sendSubChunk(section);
        }
    }

    public static void sendCleared(RollBack rollBack) {
        for(RollBack.SubChunk section : rollBack.getNewChunkSections()) {
            sendCleared(section);
        }
    }

    public void sendSubChunk(RollBack.SubChunk subChunk) {
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(subChunk.pos, subChunk.positions, subChunk.chunkSection, true);

        ChunkProviderServer s = subChunk.world.getMinecraftWorld().k();
        for(EntityPlayer player : s.a.e(subChunk.pos.r())) {
            player.b.a(packet);
        }
    }

    public static void sendCleared(RollBack.SubChunk subChunk) {
        //ClientboundLevelChunkPacketData p = new ClientboundLevelChunkPacketData((Chunk) ((CraftChunk) subChunk.world.getWorld().getChunkAt(subChunk.x, subChunk.z)).getHandle(ChunkStatus.o));
        ChunkProviderServer s = subChunk.world.getMinecraftWorld().k();

        ClientboundLevelChunkWithLightPacket pp = new ClientboundLevelChunkWithLightPacket((Chunk) ((CraftChunk) subChunk.world.getWorld().getChunkAt(subChunk.x, subChunk.z)).getHandle(ChunkStatus.o), s.a(), null, null, false);
    //    ChunkSection section = ((CraftChunk) subChunk.world.getWorld().getChunkAt(subChunk.x, subChunk.z)).getHandle(ChunkStatus.o).d()[subChunk.y];
      //  PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(subChunk.pos, subChunk.positions, section, true);


        for(EntityPlayer player : s.a.e(subChunk.pos.r())) {
            player.b.a(pp);
        }
    }
}
