package anthony.SuperCraftBrawl.Game.classes_2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassData;
import anthony.util.UtilClassName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;

public class ClassDataManager {
    private static final String CONFIG_DIRECTORY = System.getProperty("user.dir") + File.separator + "config" + File.separator + "classData";
    private static final String FILE_EXTENSION = ".json";
    private static final Map<String, ClassData> classDataConfigs = new HashMap<>();

    private static ClassData loadSpecificConfig(String filename) {
        if (classDataConfigs.containsKey(filename))
            return classDataConfigs.get(filename);

        String fullPath = CONFIG_DIRECTORY + File.separator + filename + FILE_EXTENSION;
        File file = new File(fullPath);
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new VavrModule());
            return mapper.readValue(file, ClassData.class);
        } catch (IOException e) {
            throw new RuntimeException("Config file reading failed! (malformed file?): " + fullPath, e);
        }
    }

    public static Map<String, ClassData> loadAllConfigs() {
        Path directory = Path.of(CONFIG_DIRECTORY);

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Config directory not found: " + CONFIG_DIRECTORY);
        }

        try (Stream<Path> paths = Files.list(directory)) {
            paths.filter(path -> path.toString().endsWith(FILE_EXTENSION)).forEach(path -> {
                String fullFileName = path.getFileName().toString();
                String className = fullFileName.substring(0, fullFileName.lastIndexOf('.'));
                classDataConfigs.put(className, loadSpecificConfig(className));
            });
        } catch (IOException e) {
            throw new RuntimeException("Config directory reading failed: " + CONFIG_DIRECTORY, e);
        }

        return classDataConfigs;
    }

    public static ClassData loadCurrentClassConfig() {
        Optional<String> className = UtilClassName.getCallingClassName();

        if (className.isPresent()) {
            return loadSpecificConfig(className.get() + FILE_EXTENSION);
        } else {
            throw new RuntimeException("Failed to properly find calling class name!");
        }
    }
}
