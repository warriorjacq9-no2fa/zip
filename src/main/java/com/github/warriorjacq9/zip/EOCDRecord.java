package com.github.warriorjacq9.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record EOCDRecord(
        int signature,
        short diskNumber,
        short centralDirectoryDiskNumber,
        short numRecords,
        short totalRecords,
        int centralDirectorySize,
        int centralDirectoryOffset,
        short commentLength,
        String comment
) {
    private static final int STATIC_LEN = 22;
    public static final int MAGIC = 0x06054b50;

    public static EOCDRecord fromStream(InputStream in) throws IOException {
        byte[] headerBytes = in.readNBytes(STATIC_LEN);

        if (headerBytes.length != STATIC_LEN) {
            throw new EOFException("Incomplete ZIP header");
        }

        ByteBuffer buf = ByteBuffer
                .wrap(headerBytes)
                .order(ByteOrder.LITTLE_ENDIAN);

        int signature = buf.getInt();

        if (signature != MAGIC) {
            throw new IOException("Input stream is not a ZIP local file header");
        }

        short diskNumber = buf.getShort();
        short centralDirectoryDiskNumber = buf.getShort();
        short numRecords = buf.getShort();
        short totalRecords = buf.getShort();
        int centralDirectorySize = buf.getInt();
        int centralDirectoryOffset = buf.getInt();
        short commentLength = buf.getShort();

        String comment = new String(
                in.readNBytes(Short.toUnsignedInt(commentLength)),
                StandardCharsets.UTF_8
        );

        return new EOCDRecord(
                signature,
                diskNumber,
                centralDirectoryDiskNumber,
                numRecords,
                totalRecords,
                centralDirectorySize,
                centralDirectoryOffset,
                commentLength,
                comment
        );
    }
}