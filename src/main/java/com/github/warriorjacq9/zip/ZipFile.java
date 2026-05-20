package com.github.warriorjacq9.zip;

import java.io.*;

public class ZipFile {
    private EOCDRecord eocdRecord;

    private ZipFile() {}

    public static ZipFile read(InputStream in) throws IOException {
        ZipFile zip = new ZipFile();
        zip.eocdRecord = EOCDRecord.fromStream(in);
        return zip;
    }

    public void write(OutputStream out) throws IOException {
        eocdRecord.writeStream(out);
    }

    public static ZipFile create() {
        ZipFile zip = new ZipFile();

        zip.eocdRecord = new EOCDRecord(
                EOCDRecord.MAGIC,
                (short) 0,
                (short) 0,
                (short) 0,
                (short) 0,
                0,
                0,
                (short) 0,
                ""
        );
        return zip;
    }
}
