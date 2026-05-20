import com.github.warriorjacq9.zip.ZipFile;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ZipFile file = ZipFile.create();
        try(FileOutputStream out = new FileOutputStream("test.zip")) {
            file.write(out);
        } catch (IOException e) {
            System.out.println("File not found: test.zip");
        }
    }
}
