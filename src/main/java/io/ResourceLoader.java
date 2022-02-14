package io;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public final class ResourceLoader {
    private ResourceLoader() {}

    public static String loadShader(Path shaderPath) throws IOException {
        //TODO: check this is a shader, or at least try to.
//        if (shaderPath.getFileName().toString().equals("glsl"))

        return loadFile(shaderPath);
    }

    public static String loadFile(Path path) throws IOException {
//        try {
//            File file = path.toFile();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try (BufferedReader file = new BufferedReader(new FileReader(String.valueOf(path)))){
            StringBuilder text = new StringBuilder();

            try {
                String line = file.readLine();
                while (null != line) {
                    text.append(line).append("\n");
                    line = file.readLine();
                }

                return text.toString();
            } finally {
                file.close();
            }
        } catch (IOException ioEx) {
            throw ioEx;
        }

    }
//
//    public static ? model(Path path){}
//
//    public static Sound sound(Path path){}
//
//    public static Image image(Path path){}

}
