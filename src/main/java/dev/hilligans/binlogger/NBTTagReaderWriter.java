package dev.hilligans.binlogger;

import dev.hilligans.binlogger.util.DataInputByteBuffer;
import dev.hilligans.binlogger.util.DataOutputByteBuffer;

import java.io.IOException;

public interface NBTTagReaderWriter {

    void write(DataOutputByteBuffer buffer) throws IOException;
    void read(DataInputByteBuffer buffer) throws IOException;

    int hashCode();

}
