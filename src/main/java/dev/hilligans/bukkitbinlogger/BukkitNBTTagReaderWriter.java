package dev.hilligans.bukkitbinlogger;

import dev.hilligans.binlogger.NBTTagReaderWriter;
import dev.hilligans.binlogger.util.DataInputByteBuffer;
import dev.hilligans.binlogger.util.DataOutputByteBuffer;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.Objects;

public class BukkitNBTTagReaderWriter implements NBTTagReaderWriter {

    public NBTTagCompound nbt;

    public BukkitNBTTagReaderWriter(NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public void write(DataOutputByteBuffer buffer) throws IOException {
        nbt.a(buffer);
    }

    @Override
    public void read(DataInputByteBuffer buffer) throws IOException {
        NBTReadLimiter reader = new NBTReadLimiter(0x200000L);
        nbt = NBTTagCompound.b.b(buffer, 0, reader);
    }

    @Override
    public int hashCode() {
        return nbt.hashCode();
    }
}
