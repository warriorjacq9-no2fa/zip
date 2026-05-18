package com.github.warriorjacq9.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record DataDescriptor(
        int crc32,
        int compressedSize,
        int uncompressedSize
) {
    private static final int STATIC_LEN = 12;
    public static final int MAGIC = 0x08074b50;

    public static DataDescriptor fromStream(InputStream in) throws IOException {
        byte[] headerBytes = in.readNBytes(STATIC_LEN);

        if (headerBytes.length != STATIC_LEN) {
            throw new EOFException("Incomplete data descriptor");
        }

        ByteBuffer buf = ByteBuffer
                .wrap(headerBytes)
                .order(ByteOrder.LITTLE_ENDIAN);
        int crc32 = buf.getInt();
        if(crc32 == MAGIC) crc32 = buf.getInt(); // Optional magic number should be skipped
        int compressedSize = buf.getInt();
        int uncompressedSize = buf.getInt();

        return new DataDescriptor(
                crc32,
                compressedSize,
                uncompressedSize
        );
    }
}