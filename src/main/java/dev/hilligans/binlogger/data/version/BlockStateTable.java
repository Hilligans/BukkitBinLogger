package dev.hilligans.binlogger.data.version;

import dev.hilligans.binlogger.data.BinaryFile;
import dev.hilligans.binlogger.data.Segment;
import dev.hilligans.binlogger.data.SegmentTable;

import java.lang.foreign.MemorySegment;

public class BlockStateTable extends BinaryFile {

    @Override
    public long getWantedSize() {
        return 0;
    }

    @Override
    public void readHeader(Segment segment) {

    }

    @Override
    public void setBackingSegment(Segment segment) {

    }

    @Override
    public void writeHeader(SegmentTable segment) {

    }
}
