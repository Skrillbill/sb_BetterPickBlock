package fhannenheim.betterpickblock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.CallbackI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class BetterPickBlock implements ModInitializer {
    public static List<String> dontReplaceItemIDs = new ArrayList<>();
    public static List<String> dontReplaceItemTags = new ArrayList<>();
    public static HashMap<String, String[]> itemAlternatives = new HashMap<>();
    public static Logger LOGGER;
    @Override
    public void onInitialize() {
        LOGGER = LogManager.getLogger(getClass().getName());

        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "bpbconfig.hcl");
        Scanner fileReader;
        try {
             fileReader = new Scanner(configFile);
        } catch (FileNotFoundException e) {
            LOGGER.info("No config file found. Creating default config");
            InputStream stream = getClass().getClassLoader().getResourceAsStream("config_template.hcl");
            if(stream == null){
                throw new IllegalStateException("The mod file you have is not working correctly. " +
                        "Please contact the mod author. Config template not found in resources");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String[] lines = reader.lines().toArray(String[]::new);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                for (int i = 0; i < lines.length; i++){
                    writer.write(lines[i] + "\n");
                }
                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            onInitialize();
            return;
        }
        byte readMode = 0;
        while (fileReader.hasNextLine()){

            String line = fileReader.nextLine().trim();
            if(line.startsWith("//") || line.equals(""))
                continue;
            if(line.startsWith("[")){
                switch (line){
                    case "[dont_replace]":
                        readMode = 1;
                        continue;
                    case "[block_alternatives]":
                        readMode = 2;
                        continue;
                    default:
                        throw new IllegalStateException("Unrecognized line starting with [ in config file. Options are [dont_replace] and [block_alternatives]");
                }
            }
            switch (readMode){
                case 1:
                    if(line.startsWith("#"))
                        dontReplaceItemTags.add(line.replace("#",""));
                    else
                        dontReplaceItemIDs.add(line);
                    break;
                case 2:
                    try {
                        String[] split = line.split("->");
                        String[] alternatives = split[1].split("\\|");
                        itemAlternatives.put(split[0],alternatives);
                    } catch (ArrayIndexOutOfBoundsException e){
                        throw new IllegalStateException("Something went wrong parsing the alternative items config.");
                    }
                    break;
                default:
                    throw new IllegalStateException("Specify type with [] before you do anything else in config file.");
            }
        }
        fileReader.close();
    }
}
