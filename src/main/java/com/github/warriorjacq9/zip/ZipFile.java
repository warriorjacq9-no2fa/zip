package com.github.warriorjacq9.zip;

import java.io.*;

public class ZipFile {
    private EOCDRecord eocdRecord;

    public ZipFile(String filename) throws IOException {
        try(InputStream in = new FileInputStream(filename)) {
            eocdRecord = EOCDRecord.fromStream(in);
        } catch (Exception e) {
            if(e instanceof FileNotFoundException ex) {
                System.out.printf("File not found: %s", filename);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static ZipFile create(String filename) throws IOException {
        try(OutputStream out = new FileOutputStream(filename)) {
            EOCDRecord record = new EOCDRecord(
                    EOCDRecord.MAGIC,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,

            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
