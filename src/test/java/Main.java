import com.github.warriorjacq9.zip.ZipFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ZipFile file = ZipFile.create();
        try(
                FileInputStream in = new FileInputStream("test.txt");
                FileOutputStream out = new FileOutputStream("test.zip")) {
            byte[] data = in.readAllBytes();
            for(int i = 0; i < 128; i++)  file.addFile("test%d.txt".formatted(i), data);
            file.write(out);
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
