package uk.oczadly.karl.nanopowbench.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    public static String readFileAsString(InputStream is) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            return sb.toString();
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {}
        }
    }

}
