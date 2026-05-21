package com.github.warriorjacq9.zip;

import java.io.*;
import java.util.HashMap;
import java.util.zip.CRC32;
import java.time.LocalDateTime;
import java.util.Map;

public class ZipFile {
    public static long toDosTime(LocalDateTime dateTime) {
        int year = dateTime.getYear() - 1980;
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond() / 2; // DOS stores seconds in 2-second increments

        int dosDate = (year << 9) | (month << 5) | day;
        int dosTime = (hour << 11) | (minute << 5) | second;

        // Combined 32-bit value (Date in high bits, Time in low)
        return ((long) dosDate << 16) | (dosTime & 0xFFFFL);
    }

    record LocalFile(LocalFileHeader lfh, byte[] data) {}

    private EOCDRecord eocdRecord;
    private Map<CentralDirectoryFileHeader, LocalFile> files;

    private ZipFile() {
        files = new HashMap<>();
    }

    public static ZipFile read(InputStream in) throws IOException {
        ZipFile zip = new ZipFile();
        zip.eocdRecord = EOCDRecord.fromStream(in);
        return zip;
    }

    public void write(OutputStream out) throws IOException {
        int cdOffset = 0;
        for(LocalFile file: files.values()) {
            cdOffset += LocalFileHeader.STATIC_LEN;
            cdOffset += file.lfh.nameLength();
            cdOffset += file.lfh.extraLength();
            cdOffset += file.data.length;
            file.lfh.writeStream(out);
            out.write(file.data);
        }
        int cdSize = 0;
        for(CentralDirectoryFileHeader header: files.keySet()) {
            cdSize += CentralDirectoryFileHeader.STATIC_LEN;
            cdSize += header.nameLength();
            cdSize += header.extraLength();
            cdSize += header.commentLength();
            header.writeStream(out);
        }
        eocdRecord = new EOCDRecord(
                EOCDRecord.MAGIC,
                (short) 0,
                (short) 0,
                (short) files.size(),
                (short) files.size(),
                cdSize,
                cdOffset,
                (short) 0,
                ""
        );
        eocdRecord.writeStream(out);
    }

    public static ZipFile create() {
        return new ZipFile();
    }

    public void addFile(String name, byte[] data) {
        CRC32 checksum = new CRC32();
        checksum.update(data);
        CentralDirectoryFileHeader header = new CentralDirectoryFileHeader(
                CentralDirectoryFileHeader.MAGIC,
                (short) 0x0014,
                (short) 0x0014,
                (short) 0,
                (short) 0,
                (short) (toDosTime(LocalDateTime.now()) & 0xFFFFL),
                (short) (toDosTime(LocalDateTime.now()) >> 16),
                (int) checksum.getValue(),
                data.length,
                data.length,
                (short) name.length(),
                (short) 0,
                (short) 0,
                (short) 0,
                (short) 0,
                0,
                0,
                name,
                new byte[0],
                ""
        );
        files.put(header,new LocalFile(LocalFileHeader.fromCDFH(header), data));
    }
}
