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
import user11681.xray.registry.BlockItemData;
import user11681.xray.registry.FluidData;
import user11681.xray.mixin.duck.BucketFluidAccessor;

public class Configuration {
    public static final Configuration INSTANCE = new Configuration("config/xray.json");

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(Configuration.class, new Serializer())
            .create();
    private static final JsonParser PARSER = new JsonParser();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public final Set<Block> defaultBlocks;
    public final Set<Block> allowedBlocks;
    public final Set<Block> addBlocks;
    public final Set<Block> removeBlocks;
    public final Set<Fluid> defaultFluids;
    public final Set<Fluid> allowedFluids;
    public final Set<Fluid> addFluids;
    public final Set<Fluid> removeFluids;
    private final File file;
    public boolean enabled;
    private boolean previousChunkCullingEnabled;

    public Configuration(final String path) {
        this.file = new File(path);
        this.allowedBlocks = new ObjectLinkedOpenHashSet<>();
        this.allowedFluids = new ObjectLinkedOpenHashSet<>();
        this.addBlocks = new ObjectLinkedOpenHashSet<>();
        this.removeBlocks = new ObjectLinkedOpenHashSet<>();
        this.addFluids = new ObjectLinkedOpenHashSet<>();
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
        this.defaultFluids.addAll(FluidData.FLUIDS.get(Fluids.WATER));
        this.defaultFluids.addAll(FluidData.FLUIDS.get(Fluids.LAVA));
    }

    public static boolean isAcceptable(final ItemStack itemStack) {
        return isAcceptable(itemStack.getItem());
    }

    public static boolean isAcceptable(final Item item) {
        if (item instanceof BucketItem) {
            final Fluid fluid = ((BucketFluidAccessor) item).getFluid();

            return !(item instanceof FishBucketItem || fluid == Fluids.EMPTY);
        }

        return item instanceof BlockItem;
    }

    public final void read() {
        if (this.tryCreateFile()) {
            this.reset();
        } else {
            try {
                final JsonElement content = PARSER.parse(this.readFile());

                if (content instanceof JsonObject) {
                    final JsonObject configuration = (JsonObject) content;
                    final Predicate<Block> blockPredicate = (final Block block) -> !block.is(Blocks.AIR);
                    final Predicate<Fluid> fluidPredicate = (final Fluid fluid) -> !fluid.matchesType(Fluids.EMPTY);

                    this.readArrayAllowed("block", "allowedBlocks", configuration, this.allowedBlocks, this::resetBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayAllowed("fluid", "allowedFluids", configuration, this.allowedFluids, this::resetFluids, fluidPredicate, Registry.FLUID::get);
                    this.readArrayCache("block", "addBlocks", configuration, this.addBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayCache("fluid", "addFluids", configuration, this.addFluids, fluidPredicate, Registry.FLUID::get);
                    this.readArrayCache("block", "removeBlocks", configuration, this.removeBlocks, blockPredicate, Registry.BLOCK::get);
                    this.readArrayCache("fluid", "removeFluids", configuration, this.removeFluids, fluidPredicate, Registry.FLUID::get);

                    this.enabled = configuration.get("enabled").getAsBoolean();

                    Main.LOGGER.info("Successfully loaded X-ray configuration file {}.", this.file);
                } else {
                    Main.LOGGER.error("X-ray configuration file {} format is incorrect. It must contain an object with string \"enabled\" and arrays \"allowedBlocks\" and \"allowedFluids\"", this.file);
                }
            } catch (final JsonParseException exception) {
                Main.LOGGER.error("X-ray configuration file {} format is not a valid JSON format.", this.file);
            }
        }

        this.write(true);
    }

    private <T> void readArrayCache(final String type, final String name, final JsonObject configuration, final Collection<T> output, final Predicate<T> addPredicate, final Function<Identifier, T> fromString) {
        this.readArray(type, name, configuration, output, null, String.format("\"%s\" array in %s is not present; ignoring it.", name, this.file), addPredicate, fromString);
    }

    private <T> void readArrayAllowed(final String type, final String name, final JsonObject configuration, final Collection<T> output, final Runnable onFail, final Predicate<T> addPredicate, final Function<Identifier, T> fromString) {
        this.readArray(type, name, configuration, output, onFail, String.format("\"%s\" array in %s is not present; generating a default array instead.", name, this.file), addPredicate, fromString);
    }

    private <T> void readArray(final String type, final String name, final JsonObject configuration, final Collection<T> output, final Runnable onFail, final String message, final Predicate<T> addPredicate, final Function<Identifier, T> fromString) {
        final JsonArray array = configuration.getAsJsonArray(name);

        if (array == null) {
            Main.LOGGER.error("\"{}\" array in {} is not present; generating a default array instead.", name, this.file);

            if (onFail != null) {
                onFail.run();
            }
        } else for (final JsonElement identifier : array) {
            try {
                final T block = fromString.apply(new Identifier(identifier.getAsString()));

                if (addPredicate.test(block)) {
                    output.add(block);
                }
            } catch (final UnsupportedOperationException exception) {
                Main.LOGGER.warn("Skipping {} because it is not a valid {} identifier", identifier, type);
            }
        }
    }

    public final void write() {
        this.write(false);
    }

    public final void write(final boolean verbose) {
        final String configuration = GSON.toJson(this);

        if (this.file.exists() || this.tryCreateFile()) {
            try {
                final OutputStream output = new FileOutputStream(this.file);

                if (verbose) {
                    Main.LOGGER.info("Attempting to write to X-ray configuration file {}.", this.file);
                }

                output.write(configuration.getBytes());

                if (verbose) {
                    Main.LOGGER.info("Successfully wrote to X-ray configuration file {}.", this.file);
                }
            } catch (final FileNotFoundException exception) {
                Main.LOGGER.error("X-ray configuration file {} was not found despite existing.", this.file);
            } catch (final IOException exception) {
                Main.LOGGER.error("An error occurred while attempting to write to X-ray configuration file {}.", this.file);
            }
        }
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
            this.apply();

            return false;
        }
    }

    public final boolean shouldFilter(final BlockEntity entity) {
        return entity.hasWorld() && this.shouldFilter(entity.getCachedState().getBlock());
    }

    public final boolean shouldFilter(final BlockState state) {
        return this.shouldFilter(state.getBlock());
    }

    public final boolean shouldFilter(final Block block) {
        return this.enabled && !this.isAllowed(block);
    }

    public final boolean shouldFilter(final FluidState state) {
        return this.shouldFilter(state.getFluid());
    }

    public final boolean shouldFilter(final Fluid fluid) {
        return this.enabled && !this.isAllowed(fluid);
    }

    public final boolean isAllowed(final ItemStack itemStack) {
        return this.isAllowed(itemStack.getItem());
    }

    public final boolean isAllowed(final Item item) {
        return item instanceof BlockItem && this.isAllowed(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.isAllowed(((BucketFluidAccessor) item).getFluid());
    }

    public final boolean isAllowed(final BlockEntity entity) {
        return this.isAllowed(entity.getCachedState().getBlock());
    }

    public final boolean isAllowed(final BlockState state) {
        return this.isAllowed(state.getBlock());
    }

    public final boolean isAllowed(final Block block) {
        return this.allowedBlocks.contains(block);
    }

    public final boolean isAllowed(final Fluid fluid) {
        return this.allowedFluids.contains(fluid);
    }

    public final boolean shouldAdd(final ItemStack itemStack) {
        return this.shouldAdd(itemStack.getItem());
    }

    public final boolean shouldAdd(final Item item) {
        return item instanceof BlockItem && this.shouldAdd(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.shouldAdd(((BucketFluidAccessor) item).getFluid());
    }

    public final boolean shouldAdd(final Block block) {
        return this.addBlocks.contains(block);
    }

    public final boolean shouldAdd(final Fluid fluid) {
        return this.addFluids.contains(fluid);
    }

    public final boolean shouldRemove(final ItemStack itemStack) {
        return this.shouldRemove(itemStack.getItem());
    }

    public final boolean shouldRemove(final Item item) {
        return item instanceof BlockItem && this.shouldRemove(Block.getBlockFromItem(item)) || item instanceof BucketItem && this.shouldRemove(((BucketFluidAccessor) item).getFluid());
    }

    public final boolean shouldRemove(final Block block) {
        return this.removeBlocks.contains(block);
    }

    public final boolean shouldRemove(final Fluid fluid) {
        return this.removeFluids.contains(fluid);
    }

    public final void toggleItems(final ItemStack... stacks) {
        this.toggleItems(this.toItems(stacks));
    }

    public final void toggleItems(final List<Item> items) {
        this.toggleItems(items.toArray(new Item[0]));
    }

    public final void toggleItems(final Item... items) {
        for (final Item item : items) {
            if (item instanceof BlockItem) {
                this.toggleBlocks(BlockItemData.ITEM_BLOCKS.get(item));
            } else if (item instanceof BucketItem) {
                this.toggleFluids(((BucketFluidAccessor) item).getFluid());
            }
        }
    }

    public final void toggleBlocks(final Collection<Block> blocks) {
        this.toggleBlocks(blocks.toArray(new Block[0]));
    }

    public final void toggleBlocks(final Block... blocks) {
        for (final Block block : blocks) {
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

    public final void toggleFluids(final Collection<Fluid> fluids) {
        this.toggleFluids(fluids.toArray(new Fluid[0]));
    }

    public final void toggleFluids(final Fluid... fluids) {
        for (final Fluid fluid : fluids) {
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

    public final void addBlocks(final Block... blocks) {
        this.addBlocks.addAll(Arrays.asList(blocks));
    }

    public final void addFluids(final Fluid... fluids) {
        for (final Fluid fluid : fluids) {
            this.addFluids.addAll(FluidData.FLUIDS.get(fluid));
        }
    }

    public final void removeBlocks(final Block... blocks) {
        this.removeBlocks.addAll(Arrays.asList(blocks));
    }

    public final void removeFluids(final Fluid... fluids) {
        for (final Fluid fluid : fluids) {
            this.removeFluids.addAll(FluidData.FLUIDS.get(fluid));
        }
    }

    private List<Item> toItems(final ItemStack... stacks) {
        final List<Item> items = new ArrayList<>();

        for (final ItemStack stack : stacks) {
            items.add(stack.getItem());
        }

        return items;
    }

    private List<Block> toBlocks(final Item... items) {
        final List<Block> blocks = new ArrayList<>();

        for (final Item item : items) {
            if (item instanceof BlockItem) {
                blocks.add(Block.getBlockFromItem(item));
            }
        }

        return blocks;
    }

    public final boolean isDefault() {
        return this.allowedBlocks.equals(this.defaultBlocks) && this.allowedFluids.equals(this.defaultFluids);
    }

    public final boolean canApply() {
        return !this.addBlocks.isEmpty() || !this.addFluids.isEmpty() || !this.removeBlocks.isEmpty() || !this.removeFluids.isEmpty();
    }

    public final void apply() {
        this.allowedBlocks.removeAll(this.removeBlocks);
        this.allowedFluids.removeAll(this.removeFluids);
        this.allowedBlocks.addAll(this.addBlocks);
        this.allowedFluids.addAll(this.addFluids);
        this.removeBlocks.clear();
        this.removeFluids.clear();
        this.addBlocks.clear();
        this.addFluids.clear();

        this.write();

        if (this.enabled) {
            this.reload();
        }
    }

    public boolean isSaved() {
        return this.addBlocks.isEmpty() && this.removeBlocks.isEmpty() && this.addFluids.isEmpty() && this.removeFluids.isEmpty();
    }

    public final void reload() {
        client.worldRenderer.reload();
    }

    public final void discard() {
        this.addBlocks.clear();
        this.removeBlocks.clear();
        this.addFluids.clear();
        this.removeFluids.clear();
    }

    public final void reset() {
        this.resetBlocks();
        this.resetFluids();
    }

    public final void resetBlocks() {
        this.removeBlocks.clear();
        this.removeBlocks.addAll(this.allowedBlocks.parallelStream().filter((final Block block) -> !this.defaultBlocks.contains(block)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
        this.addBlocks.clear();
        this.addBlocks.addAll(this.defaultBlocks.parallelStream().filter((final Block block) -> !this.allowedBlocks.contains(block)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
    }

    public final void resetFluids() {
        this.removeFluids.clear();
        this.removeFluids.addAll(this.allowedFluids.parallelStream().filter((final Fluid fluid) -> !this.defaultFluids.contains(fluid)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
        this.addFluids.clear();
        this.addFluids.addAll(this.defaultFluids.parallelStream().filter((final Fluid fluid) -> !this.allowedFluids.contains(fluid)).collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new)));
    }

    public final void toggle() {
        if (this.enabled = !this.enabled) {
            this.previousChunkCullingEnabled = client.chunkCullingEnabled;

            client.chunkCullingEnabled = false;
        } else {
            client.chunkCullingEnabled = this.previousChunkCullingEnabled;
        }

        this.write();
    }

    public static class Serializer implements JsonSerializer<Configuration> {
        private static JsonArray serializeFluids(final Set<Fluid> fluids, final JsonSerializationContext context) {
            final JsonArray array = new JsonArray();

            for (final Fluid item : fluids) {
                array.add(context.serialize(Registry.FLUID.getId(item).toString()));
            }

            return array;
        }

        private static JsonArray serializeBlocks(final Set<Block> blocks, final JsonSerializationContext context) {
            final JsonArray array = new JsonArray();

            for (final Block item : blocks) {
                array.add(context.serialize(Registry.BLOCK.getId(item).toString()));
            }

            return array;
        }

        @Override
        public JsonElement serialize(final Configuration src, final Type typeOfSrc, final JsonSerializationContext context) {
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
