package user11681.xradiation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import user11681.xradiation.mixin.duck.BucketFluidAccessor;
import user11681.xradiation.registry.RegistryData;

public class Configuration {
    public static final Configuration instance = new Configuration(String.format("%s.json", XRadiation.ID));

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final JsonParser PARSER = new JsonParser();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(Configuration.class, new Serializer())
        .create();

    public final Set<Block> allowedBlocks;
    public final Set<Fluid> allowedFluids;
    public final Set<Block> addBlocks;
    public final Set<Fluid> addFluids;
    public final Set<Block> removeBlocks;
    public final Set<Fluid> removeFluids;
    public final Set<Block> defaultBlocks;
    public final Set<Fluid> defaultFluids;

    public boolean enabled;

    private final File file;

    private boolean previousChunkCullingEnabled;

    protected Configuration(String filename) {
        this.file = new File(FabricLoader.getInstance().getConfigDir().toString(), filename);
        this.allowedBlocks = new ObjectLinkedOpenHashSet<>();
        this.allowedFluids = new ObjectLinkedOpenHashSet<>();
        this.addBlocks = new ObjectLinkedOpenHashSet<>();
        this.addFluids = new ObjectLinkedOpenHashSet<>();
        this.removeBlocks = new ObjectLinkedOpenHashSet<>();
        this.removeFluids = new ObjectLinkedOpenHashSet<>();
        this.defaultBlocks = new ObjectLinkedOpenHashSet<>(Arrays.asList(
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
        this.defaultFluids = new ObjectLinkedOpenHashSet<>(5);
        this.defaultFluids.addAll(RegistryData.fluids.get(Fluids.WATER));
        this.defaultFluids.addAll(RegistryData.fluids.get(Fluids.LAVA));
    }

    public static boolean isAcceptable(ItemStack itemStack) {
        return isAcceptable(itemStack.getItem());
    }

    public static boolean isAcceptable(Item item) {
        if (item instanceof BucketItem) {
            final Fluid fluid = ((BucketFluidAccessor) item).getFluid();

            return !(item instanceof FishBucketItem || fluid == Fluids.EMPTY);
        }

        return item instanceof BlockItem;
    }

    public void read() {
        if (this.tryCreateFile()) {
            this.reset();
            this.apply();
        } else {
            try {
                final JsonElement content = PARSER.parse(this.readFile());

                if (content instanceof JsonObject) {
                    final JsonObject configuration = (JsonObject) content;
                    final Predicate<Block> blockPredicate = (Block block) -> block != Blocks.AIR;
                    final Predicate<Fluid> fluidPredicate = (Fluid fluid) -> !fluid.matchesType(Fluids.EMPTY);

                    this.readArrayAllowed("block", "allowedBlocks", configuration, this.allowedBlocks, this::resetBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayAllowed("fluid", "allowedFluids", configuration, this.allowedFluids, this::resetFluids, fluidPredicate, Registry.FLUID::get);
                    this.readArrayCache("block", "addBlocks", configuration, this.addBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayCache("fluid", "addFluids", configuration, this.addFluids, fluidPredicate, Registry.FLUID::get);
                    this.readArrayCache("block", "removeBlocks", configuration, this.removeBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayCache("fluid", "removeFluids", configuration, this.removeFluids, fluidPredicate, Registry.FLUID::get);

                    this.enabled = configuration.get("enabled").getAsBoolean();

                    XRadiation.logger.info("Successfully loaded Configuration file {}.", this.file);
                } else {
                    XRadiation.logger.error("Configuration file {} format is incorrect. It must contain an object with string \"enabled\" and arrays \"allowedBlocks\" and \"allowedFluids\"", this.file);
                }
            } catch (JsonParseException exception) {
                XRadiation.logger.error("Configuration file {} format is not a valid JSON format.", this.file);
            }
        }

        this.write(true);
    }

    private <T> void readArrayCache(String type, String name, JsonObject configuration, Collection<T> output, Predicate<T> addPredicate, Function<Identifier, T> fromString) {
        this.readArray(type, name, configuration, output, null, "{} array in {} is not present; ignoring it.", addPredicate, fromString);
    }

    private <T> void readArrayAllowed(String type, String name, JsonObject configuration, Collection<T> output, Runnable onFail, Predicate<T> addPredicate, Function<Identifier, T> fromString) {
        this.readArray(type, name, configuration, output, onFail, "{} array in {} is not present; generating a default array instead.", addPredicate, fromString);
    }

    private <T> void readArray(String type, String name, JsonObject configuration, Collection<T> output, Runnable onFail, String message, Predicate<T> addPredicate, Function<Identifier, T> fromString) {
        final JsonArray array = configuration.getAsJsonArray(name);

        if (array == null) {
            XRadiation.logger.error(message, name, this.file);

            if (onFail != null) {
                onFail.run();
            }
        } else {
            for (JsonElement identifier : array) {
                try {
                    final T block = fromString.apply(new Identifier(identifier.getAsString()));

                    if (addPredicate.test(block)) {
                        output.add(block);
                    }
                } catch (UnsupportedOperationException exception) {
                    XRadiation.logger.warn("Skipping {} because it is not a valid {} identifier", identifier, type);
                }
            }
        }
    }

    public void write() {
        this.write(false);
    }

    public void write(boolean verbose) {
        final String configuration = GSON.toJson(this);

        if (this.file.exists() || this.tryCreateFile()) {
            try {
                final OutputStream output = new FileOutputStream(this.file);

                if (verbose) {
                    XRadiation.logger.info("Attempting to write to configuration file {}.", this.file);
                }

                output.write(configuration.getBytes());

                if (verbose) {
                    XRadiation.logger.info("Successfully wrote to configuration file {}.", this.file);
                }
            } catch (FileNotFoundException exception) {
                XRadiation.logger.error("Configuration file {} was not found despite existing.", this.file);
            } catch (IOException exception) {
                XRadiation.logger.error("An error occurred while attempting to write to configuration file {}.", this.file);
            }
        }
    }

    private String readFile() {
        try {
            final InputStream input = new FileInputStream(file);
            final byte[] content = new byte[input.available()];

            while (input.read(content) > -1) {}

            return (new String(content));
        } catch (FileNotFoundException exception) {
            XRadiation.logger.error(String.format("Configuration file %s was not found.", this.file), exception);
        } catch (IOException exception) {
            XRadiation.logger.error("An error occurred while attempting to read the configuration file.", exception);
        }

        return "";
    }

    private boolean tryCreateFile() {
        try {
            final boolean didNotExist = this.file.createNewFile();

            if (didNotExist) {
                XRadiation.logger.info("Made a new configuration file {}.", this.file);
            }

            return didNotExist;
        } catch (IOException exception) {
            XRadiation.logger.error(String.format("An error occured while attempting to make a new configuration file %s.", this.file), exception);

            this.reset();
            this.apply();

            return false;
        }
    }

    public boolean shouldFilter(BlockEntity entity) {
        return entity.hasWorld() && this.shouldFilter(entity.getCachedState().getBlock());
    }

    public boolean shouldFilter(BlockState state) {
        return this.shouldFilter(state.getBlock());
    }

    public boolean shouldFilter(Block block) {
        return this.enabled && !this.isAllowed(block);
    }

    public boolean shouldFilter(FluidState state) {
        return this.shouldFilter(state.getFluid());
    }

    public boolean shouldFilter(Fluid fluid) {
        return this.enabled && !this.isAllowed(fluid);
    }

    public boolean isAllowed(ItemStack itemStack) {
        return this.isAllowed(itemStack.getItem());
    }

    public boolean isAllowed(Item item) {
        return item instanceof BlockItem && this.isAllowed(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.isAllowed(((BucketFluidAccessor) item).getFluid());
    }

    public boolean isAllowed(BlockEntity entity) {
        return this.isAllowed(entity.getCachedState().getBlock());
    }

    public boolean isAllowed(BlockState state) {
        return this.isAllowed(state.getBlock());
    }

    public boolean isAllowed(Block block) {
        return this.allowedBlocks.contains(block);
    }

    public boolean isAllowed(Fluid fluid) {
        return this.allowedFluids.contains(fluid);
    }

    public boolean shouldAdd(ItemStack itemStack) {
        return this.shouldAdd(itemStack.getItem());
    }

    public boolean shouldAdd(Item item) {
        return item instanceof BlockItem && this.shouldAdd(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.shouldAdd(((BucketFluidAccessor) item).getFluid());
    }

    public boolean shouldAdd(Block block) {
        return this.addBlocks.contains(block);
    }

    public boolean shouldAdd(Fluid fluid) {
        return this.addFluids.contains(fluid);
    }

    public boolean shouldRemove(ItemStack itemStack) {
        return this.shouldRemove(itemStack.getItem());
    }

    public boolean shouldRemove(Item item) {
        return item instanceof BlockItem && this.shouldRemove(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.shouldRemove(((BucketFluidAccessor) item).getFluid());
    }

    public boolean shouldRemove(Block block) {
        return this.removeBlocks.contains(block);
    }

    public boolean shouldRemove(Fluid fluid) {
        return this.removeFluids.contains(fluid);
    }

    public void toggleItems(ItemStack... stacks) {
        this.toggleItems(this.toItems(stacks));
    }

    public void toggleItems(List<Item> items) {
        this.toggleItems(items.toArray(new Item[0]));
    }

    public void toggleItems(Item... items) {
        for (Item item : items) {
            if (item instanceof BlockItem) {
                this.toggleBlocks(RegistryData.itemBlocks.get(item));
            } else if (item instanceof BucketItem) {
                this.toggleFluids(((BucketFluidAccessor) item).getFluid());
            }
        }
    }

    public void toggleBlocks(Collection<Block> blocks) {
        this.toggleBlocks(blocks.toArray(new Block[0]));
    }

    public void toggleBlocks(Block... blocks) {
        for (Block block : blocks) {
            if (this.shouldAdd(block)) {
                this.addBlocks.remove(block);
            } else if (this.shouldRemove(block)) {
                this.removeBlocks.remove(block);
            } else if (this.isAllowed(block)) {
                this.removeBlocks(block);
            } else {
                this.addBlocks(block);
            }
        }
    }

    public void toggleFluids(Collection<Fluid> fluids) {
        this.toggleFluids(fluids.toArray(new Fluid[0]));
    }

    public void toggleFluids(Fluid... fluids) {
        for (Fluid fluid : fluids) {
            if (this.shouldAdd(fluid)) {
                this.addFluids.remove(fluid);
            } else if (this.shouldRemove(fluid)) {
                this.removeFluids.remove(fluid);
            } else if (this.isAllowed(fluid)) {
                this.removeFluids(fluid);
            } else {
                this.addFluids(fluid);
            }
        }
    }

    public void addBlocks(Block... blocks) {
        this.addBlocks.addAll(Arrays.asList(blocks));
    }

    public void addFluids(Fluid... fluids) {
        for (Fluid fluid : fluids) {
            this.addFluids.addAll(RegistryData.fluids.get(fluid));
        }
    }

    public void removeBlocks(Block... blocks) {
        this.removeBlocks.addAll(Arrays.asList(blocks));
    }

    public void removeFluids(Fluid... fluids) {
        for (Fluid fluid : fluids) {
            this.removeFluids.addAll(RegistryData.fluids.get(fluid));
        }
    }

    private List<Item> toItems(ItemStack... stacks) {
        final List<Item> items = new ArrayList<>();

        for (ItemStack stack : stacks) {
            items.add(stack.getItem());
        }

        return items;
    }

    private List<Block> toBlocks(Item... items) {
        final List<Block> blocks = new ArrayList<>();

        for (Item item : items) {
            if (item instanceof BlockItem) {
                blocks.add(Block.getBlockFromItem(item));
            }
        }

        return blocks;
    }

    public boolean isDefault() {
        return this.allowedBlocks.equals(this.defaultBlocks) && this.allowedFluids.equals(this.defaultFluids);
    }

    public boolean canApply() {
        return !this.addBlocks.isEmpty() || !this.addFluids.isEmpty() || !this.removeBlocks.isEmpty() || !this.removeFluids.isEmpty();
    }

    public void apply() {
        this.allowedBlocks.removeAll(this.removeBlocks);
        this.allowedFluids.removeAll(this.removeFluids);
        this.allowedBlocks.addAll(this.addBlocks);
        this.allowedFluids.addAll(this.addFluids);
        this.removeBlocks.clear();
        this.removeFluids.clear();
        this.addBlocks.clear();
        this.addFluids.clear();

        if (this.enabled) {
            this.reload();
        }
    }

    public boolean isSaved() {
        return this.addBlocks.isEmpty() && this.removeBlocks.isEmpty() && this.addFluids.isEmpty() && this.removeFluids.isEmpty();
    }

    public void reload() {
        client.worldRenderer.reload();
    }

    public void discard() {
        this.addBlocks.clear();
        this.removeBlocks.clear();
        this.addFluids.clear();
        this.removeFluids.clear();
    }

    public void reset() {
        this.resetBlocks();
        this.resetFluids();
    }

    public void resetBlocks() {
        this.removeBlocks.clear();
        this.removeBlocks.addAll(this.allowedBlocks.parallelStream().filter((Block block) -> !this.defaultBlocks.contains(block)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
        this.addBlocks.clear();
        this.addBlocks.addAll(this.defaultBlocks.parallelStream().filter((Block block) -> !this.allowedBlocks.contains(block)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
    }

    public void resetFluids() {
        this.removeFluids.clear();
        this.removeFluids.addAll(this.allowedFluids.parallelStream().filter((Fluid fluid) -> !this.defaultFluids.contains(fluid)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
        this.addFluids.clear();
        this.addFluids.addAll(this.defaultFluids.parallelStream().filter((Fluid fluid) -> !this.allowedFluids.contains(fluid)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
    }

    public void toggle() {
        if (this.enabled = !this.enabled) {
            this.previousChunkCullingEnabled = client.chunkCullingEnabled;

            client.chunkCullingEnabled = false;
        } else {
            client.chunkCullingEnabled = this.previousChunkCullingEnabled;
        }
    }

    public static class Serializer implements JsonSerializer<Configuration> {
        private static JsonArray serializeFluids(Set<Fluid> fluids, JsonSerializationContext context) {
            final JsonArray array = new JsonArray();

            for (Fluid item : fluids) {
                array.add(context.serialize(Registry.FLUID.getId(item).toString()));
            }

            return array;
        }

        private static JsonArray serializeBlocks(Set<Block> blocks, JsonSerializationContext context) {
            final JsonArray array = new JsonArray();

            for (Block item : blocks) {
                array.add(context.serialize(Registry.BLOCK.getId(item).toString()));
            }

            return array;
        }

        @Override
        public JsonElement serialize(Configuration src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject object = new JsonObject();

            object.addProperty("enabled", src.enabled);
            object.addProperty("previousChunkCullingEnabled", src.previousChunkCullingEnabled);
            object.add("allowedBlocks", serializeBlocks(src.allowedBlocks, context));
            object.add("allowedFluids", serializeFluids(src.allowedFluids, context));
            object.add("addBlocks", serializeBlocks(src.addBlocks, context));
            object.add("addFluids", serializeFluids(src.addFluids, context));
            object.add("removeBlocks", serializeBlocks(src.removeBlocks, context));
            object.add("removeFluids", serializeFluids(src.removeFluids, context));

            return object;
        }
    }
}
