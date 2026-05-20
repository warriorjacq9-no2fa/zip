package com.github.warriorjacq9.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record CentralDirectoryFileHeader(
        int signature,
        short version,
        short minVersion,
        short flags,
        short compression,
        short modifiedTime,
        short modifiedDate,
        int crc32,
        int compressedSize,
        int uncompressedSize,
        short nameLength,
        short extraLength,
        short commentLength,
        short diskNumber,
        short internalAttrs,
        int externalAttrs,
        int offset,
        String name,
        byte[] extra,
        String comment
) {
    private static final int STATIC_LEN = 46;
    public static final int MAGIC = 0x02014b50;

    public static CentralDirectoryFileHeader fromStream(InputStream in) throws IOException {
        byte[] headerBytes = in.readNBytes(STATIC_LEN);

        if (headerBytes.length != STATIC_LEN) {
            throw new EOFException("Incomplete ZIP header");
        }

        ByteBuffer buf = ByteBuffer
                .wrap(headerBytes)
                .order(ByteOrder.LITTLE_ENDIAN);

        int signature = buf.getInt();

        if (signature != MAGIC) {
            throw new IOException("Input stream is not a ZIP CDFH");
        }

        short version = buf.getShort();
        short minVersion = buf.getShort();
        short flags = buf.getShort();
        short compression = buf.getShort();
        short modifiedTime = buf.getShort();
        short modifiedDate = buf.getShort();
        int crc32 = buf.getInt();
        int compressedSize = buf.getInt();
        int uncompressedSize = buf.getInt();
        short nameLength = buf.getShort();
        short extraLength = buf.getShort();
        short commentLength = buf.getShort();
        short diskNumber = buf.getShort();
        short internalAttrs = buf.getShort();
        int externalAttrs = buf.getInt();
        int offset = buf.getInt();

        String name = new String(
                in.readNBytes(Short.toUnsignedInt(nameLength)),
                StandardCharsets.UTF_8
        );

        byte[] extra = in.readNBytes(Short.toUnsignedInt(extraLength));

        String comment = new String(
                in.readNBytes(Short.toUnsignedInt(commentLength)),
                StandardCharsets.UTF_8
        );

        return new CentralDirectoryFileHeader(
                signature,
                version,
                minVersion,
                flags,
                compression,
                modifiedTime,
                modifiedDate,
                crc32,
                compressedSize,
                uncompressedSize,
                nameLength,
                extraLength,
                commentLength,
                diskNumber,
                internalAttrs,
                externalAttrs,
                offset,
                name,
                extra,
                comment
        );
    }

    public void writeStream(OutputStream out) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(STATIC_LEN + commentLength).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(signature);
        buf.putShort(version);
        buf.putShort(minVersion);
        buf.putShort(flags);
        buf.putShort(compression);
        buf.putShort(modifiedTime);
        buf.putShort(modifiedDate);
        buf.putInt(crc32);
        buf.putInt(compressedSize);
        buf.putInt(uncompressedSize);
        buf.putShort(nameLength);
        buf.putShort(extraLength);
        buf.putShort(commentLength);
        buf.putShort(diskNumber);
        buf.putShort(internalAttrs);
        buf.putInt(externalAttrs);
        buf.putInt(offset);
        buf.put(name.getBytes(StandardCharsets.UTF_8));
        buf.put(extra);
        buf.put(comment.getBytes(StandardCharsets.UTF_8));
        out.write(buf.array());
    }
}