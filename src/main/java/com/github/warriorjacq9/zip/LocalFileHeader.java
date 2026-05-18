package com.github.warriorjacq9.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record LocalFileHeader(
        int signature,
        short version,
        short flags,
        short compression,
        short modifiedTime,
        short modifiedDate,
        int crc32,
        int compressedSize,
        int uncompressedSize,
        short nameLength,
        short extraLength,
        String name,
        byte[] extra
) {
    private static final int STATIC_LEN = 30;
    public static final int MAGIC = 0x04034b50;

    public static LocalFileHeader fromStream(InputStream in) throws IOException {
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

        short version = buf.getShort();
        short flags = buf.getShort();
        short compression = buf.getShort();
        short modifiedTime = buf.getShort();
        short modifiedDate = buf.getShort();
        int crc32 = buf.getInt();
        int compressedSize = buf.getInt();
        int uncompressedSize = buf.getInt();
        short nameLength = buf.getShort();
        short extraLength = buf.getShort();

        String name = new String(
                in.readNBytes(Short.toUnsignedInt(nameLength)),
                StandardCharsets.UTF_8
        );

        byte[] extra = in.readNBytes(Short.toUnsignedInt(extraLength));

        return new LocalFileHeader(
                signature,
                version,
                flags,
                compression,
                modifiedTime,
                modifiedDate,
                crc32,
                compressedSize,
                uncompressedSize,
                nameLength,
                extraLength,
                name,
                extra
        );
    }
}