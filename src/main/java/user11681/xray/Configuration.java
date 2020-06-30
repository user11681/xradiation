package user11681.xray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Configuration {
    public static final Configuration INSTANCE = new Configuration("config/xray.json");

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(Block.class, new BlockSerializer())
            .registerTypeAdapter(Configuration.class, new Serializer())
            .create();
    private static final JsonParser PARSER = new JsonParser();

    private final File file;

    public final List<Block> allowedBlocks;

    public boolean enabled;

    public Configuration(final String path) {
        this.file = new File(path);
        this.allowedBlocks = new ArrayList<>();
    }

    public final void read() {
        if (this.tryCreateFile()) {
            this.reset();
            this.write();
        } else {
            try {
                final JsonElement content = PARSER.parse(this.readFile());

                if (content instanceof JsonObject) {
                    final JsonObject configuration = (JsonObject) content;
                    final JsonArray blockIdentifiers = configuration.getAsJsonArray("allowedBlocks");

                    for (final JsonElement identifier : blockIdentifiers) {
                        try {
                            final Block block = Registry.BLOCK.get(new Identifier(identifier.getAsString()));

                            if (!block.is(Blocks.AIR)) {
                                allowedBlocks.add(block);
                            }
                        } catch (final UnsupportedOperationException exception) {
                            Main.LOGGER.warn("Skipping {} because it is not a valid block identifier", identifier.toString());
                        }
                    }

                    this.enabled = configuration.get("enabled").getAsBoolean();

                    Main.LOGGER.info("Successfully loaded X-ray configuration file {}.", this.file);
                } else {
                    Main.LOGGER.error("X-ray configuration file {} format is incorrect. It must contain an object with string \"enabled\" and array \"allowedBlocks\"", this.file);
                }
            } catch (final JsonParseException exception) {
                Main.LOGGER.error("X-ray configuration file {} format is not a valid JSON format.", this.file);
            }
        }
    }

    public final void write() {
        final String configuration = GSON.toJson(this);

        if (this.file.exists() || this.tryCreateFile()) {
            try {
                final OutputStream output = new FileOutputStream(this.file);

                Main.LOGGER.info("Attempting to write to X-ray configuration file {}.", this.file);

                output.write(configuration.getBytes());

                Main.LOGGER.info("Successfully wrote to X-ray configuration file {}.", this.file);
            } catch (final FileNotFoundException exception) {
                Main.LOGGER.error("X-ray configuration file {} was not found despite existing.", this.file);
            } catch (final IOException exception) {
                Main.LOGGER.error("An error occurred while attempting to write to X-ray configuration file {}.", this.file);
            }
        }
    }

    public final void reset() {
        this.allowedBlocks.clear();
        this.allowedBlocks.addAll(Arrays.asList(
                Blocks.COAL_ORE,
                Blocks.IRON_ORE,
                Blocks.GOLD_ORE,
                Blocks.REDSTONE_ORE,
                Blocks.LAPIS_ORE,
                Blocks.DIAMOND_ORE,
                Blocks.EMERALD_ORE,
                Blocks.NETHER_QUARTZ_ORE,
                Blocks.NETHER_GOLD_ORE,
                Blocks.ANCIENT_DEBRIS
        ));
    }

    public final void toggle() {
        this.enabled = !this.enabled;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private String readFile() {
        try {
            final InputStream input = new FileInputStream(file);
            final byte[] content = new byte[input.available()];

            while (input.read(content) > -1) {}

            return (new String(content));
        } catch (final FileNotFoundException exception) {
            Main.LOGGER.error(String.format("X-ray configuration file %s was not found.", this.file), exception);
        } catch (final IOException exception) {
            Main.LOGGER.error("An error occurred while attempting to read X-ray configuration file.", exception);
        }

        return "";
    }

    private boolean tryCreateFile() {
        try {
            if (this.file.getParentFile().createNewFile()) {
                Main.LOGGER.info("Made a new directory \"config\".");
            }

            final boolean didNotExist = this.file.createNewFile();

            if (didNotExist) {
                Main.LOGGER.info("Made a new X-ray configuration file {}.", this.file);
            }

            return didNotExist;
        } catch (final IOException exception) {
            Main.LOGGER.error(String.format("An error occured while attempting to load X-ray configuration file %s.", this.file), exception);

            this.reset();
            this.write();

            return false;
        }
    }

    public static class Serializer implements JsonSerializer<Configuration> {
        @Override
        public JsonElement serialize(final Configuration src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject object = new JsonObject();
            final JsonArray allowedBlocks = new JsonArray();

            for (final Block block : src.allowedBlocks) {
                allowedBlocks.add(Registry.BLOCK.getId(block).toString());
            }

            object.addProperty("enabled", src.enabled);
            object.add("allowedBlocks", allowedBlocks);

            return object;
        }
    }

    public static class BlockSerializer implements JsonSerializer<Block> {
        @Override
        public JsonElement serialize(final Block src, final Type typeOfSrc, final JsonSerializationContext context) {
            return context.serialize(Registry.BLOCK.getId(src).toString());
        }
    }
}
