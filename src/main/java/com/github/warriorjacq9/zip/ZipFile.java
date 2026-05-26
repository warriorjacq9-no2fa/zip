package com.github.warriorjacq9.zip;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.CRC32;

public final class ZipFile {

    private static final int MAX_SIZE = 1048576;

    private final List<Entry> entries = new ArrayList<>();

    public static ZipFile create() {
        return new ZipFile();
    }

    public void addFile(String name, byte[] data) {
        entries.add(Entry.normal(name, data));
    }

    public void addFileCompressed(String name, byte[] data) {

    }

    public void addOverlappingFile(String name) throws IOException {
        // overlap local file region only
        int overlapSize = calculateLocalRegionSize();
        if(overlapSize > MAX_SIZE) overlapSize = MAX_SIZE;

        Entry e = Entry.overlap(name, overlapSize);

        entries.add(e);
    }

    public void write(OutputStream out) throws IOException {

        List<CentralDirectoryFileHeader> centralHeaders = new ArrayList<>();

        int offset = 0;

        // write local entries
        for (Entry entry : entries) {

            LocalFileHeader lfh = entry.createLocalHeader();

            lfh.writeStream(out);
            out.write(entry.data);

            CentralDirectoryFileHeader cdfh =
                    entry.createCentralDirectoryHeader(offset);

            centralHeaders.add(cdfh);

            offset += LocalFileHeader.STATIC_LEN + lfh.nameLength() + lfh.extraLength();
            if(!entry.isOverlap) offset += entry.length();
        }

        int centralDirectoryOffset = offset;

        // write central directory
        for (CentralDirectoryFileHeader cdfh : centralHeaders) {
            cdfh.writeStream(out);
            offset += CentralDirectoryFileHeader.STATIC_LEN + cdfh.nameLength() + cdfh.extraLength() + cdfh.commentLength();
        }

        int centralDirectorySize =
                offset - centralDirectoryOffset;

        EOCDRecord eocd = new EOCDRecord(
                EOCDRecord.MAGIC,
                (short) 0,
                (short) 0,
                (short) entries.size(),
                (short) entries.size(),
                centralDirectorySize,
                centralDirectoryOffset,
                (short) 0,
                ""
        );

        eocd.writeStream(out);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out);
        return out.toByteArray();
    }

    private int calculateLocalRegionSize() {

        int size = 0;

        for (Entry entry : entries) {
            size += entry.localHeaderSize();
            if(!entry.isOverlap) size += entry.length();
        }

        return size;
    }

    public static long toDosTime(LocalDateTime time) {

        int dosTime =
                (time.getHour() << 11)
                        | (time.getMinute() << 5)
                        | (time.getSecond() / 2);

        int dosDate =
                ((time.getYear() - 1980) << 9)
                        | (time.getMonthValue() << 5)
                        | time.getDayOfMonth();

        return ((long) dosDate << 16) | (dosTime & 0xFFFFL);
    }

    private static final class Entry {

        private final String name;
        private final byte[] data;
        private final int length;
        private final int crc32;
        private final short dosTime;
        private final short dosDate;
        private final boolean isOverlap;
        private final boolean isCompressed;

        private Entry(
                String name,
                byte[] data,
                int length,
                int crc32,
                short dosTime,
                short dosDate,
                boolean isOverlap,
                boolean isCompressed
        ) {
            this.name = name;
            this.data = data;
            this.length = length;
            this.crc32 = crc32;
            this.dosTime = dosTime;
            this.dosDate = dosDate;
            this.isOverlap = isOverlap;
            this.isCompressed = isCompressed;
        }

        static Entry normal(String name, byte[] data) {

            CRC32 crc = new CRC32();
            crc.update(data);

            long dos = ZipFile.toDosTime(LocalDateTime.now());

            return new Entry(
                    name,
                    data,
                    data.length,
                    (int) crc.getValue(),
                    (short) (dos & 0xFFFF),
                    ((short) (dos >>> 16)),
                    false,
                    false
            );
        }

        static Entry compressed(String name, byte[] data) {

            CRC32 crc = new CRC32();
            crc.update(data);

            long dos = ZipFile.toDosTime(LocalDateTime.now());

            return new Entry(
                    name,
                    data,
                    data.length,
                    (int) crc.getValue(),
                    (short) (dos & 0xFFFF),
                    ((short) (dos >>> 16)),
                    false,
                    true
            );
        }

        static Entry overlap(String name, int length) {
            long dos = ZipFile.toDosTime(LocalDateTime.now());
            return new Entry(
                    name,
                    new byte[0],
                    length,
                    0,
                    (short) (dos & 0xFFFF),
                    (short) (dos >>> 16),
                    true,
                    false
            );
        }

        LocalFileHeader createLocalHeader() {
            short effectiveFlags = isOverlap ? (short)0x08 : (short)0;
            int effectiveCrc = isOverlap ? 0 : crc32;

            return new LocalFileHeader(
                    LocalFileHeader.MAGIC,
                    (short) 20,
                    effectiveFlags,
                    (short) 0,
                    dosTime,
                    dosDate,
                    effectiveCrc,
                    length,
                    length,
                    (short) name.length(),
                    (short) 0,
                    name,
                    new byte[0]
            );
        }

        CentralDirectoryFileHeader createCentralDirectoryHeader(
                int offset
        ) {
            short effectiveFlags = isOverlap ? (short)0x08 : (short)0;
            int effectiveCrc = isOverlap ? 0 : crc32;

            return new CentralDirectoryFileHeader(
                    CentralDirectoryFileHeader.MAGIC,
                    (short) 20,
                    (short) 20,
                    effectiveFlags,
                    (short) 0,
                    dosTime,
                    dosDate,
                    effectiveCrc,
                    length,
                    length,
                    (short) name.length(),
                    (short) 0,
                    (short) 0,
                    (short) 0,
                    (short) 0,
                    0,
                    offset,
                    name,
                    new byte[0],
                    ""
            );
        }

        int localHeaderSize() {
            return LocalFileHeader.STATIC_LEN + name.length();
        }

        public int length() {
            return length;
        }
    }
}