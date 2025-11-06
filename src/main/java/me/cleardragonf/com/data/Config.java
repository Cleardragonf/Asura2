package me.cleardragonf.com.data;

import me.cleardragonf.com.Asura;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Asura.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    // Vitals settings
    public static final ForgeConfigSpec.IntValue TEMP_HYPO_THRESHOLD = BUILDER.comment("Temperature hypothermia threshold (-100..100)").defineInRange("vitals.tempHypoThreshold", -75, -100, 0);
    public static final ForgeConfigSpec.IntValue TEMP_HYPER_THRESHOLD = BUILDER.comment("Temperature hyperthermia threshold (-100..100)").defineInRange("vitals.tempHyperThreshold", 75, 0, 100);
    public static final ForgeConfigSpec.IntValue THIRST_DRAIN_INTERVAL = BUILDER.comment("Ticks between passive thirst drain").defineInRange("vitals.thirstDrainInterval", 80, 1, 1200);
    public static final ForgeConfigSpec.IntValue THIRST_DRAIN_IDLE = BUILDER.comment("Passive thirst drain per interval when idle").defineInRange("vitals.thirstDrainIdle", 1, 0, 10);
    public static final ForgeConfigSpec.IntValue THIRST_DRAIN_SPRINTING = BUILDER.comment("Passive thirst drain per interval when sprinting").defineInRange("vitals.thirstDrainSprinting", 2, 0, 10);
    public static final ForgeConfigSpec.IntValue THIRST_GAIN_WATER = BUILDER.comment("Thirst gained from drinking a water bottle").defineInRange("vitals.thirstGainWater", 30, 0, 100);
    public static final ForgeConfigSpec.IntValue THIRST_GAIN_MILK = BUILDER.comment("Thirst gained from drinking milk").defineInRange("vitals.thirstGainMilk", 15, 0, 100);
    public static final ForgeConfigSpec.IntValue THIRST_GAIN_SOUP = BUILDER.comment("Thirst gained from eating soups/stews").defineInRange("vitals.thirstGainSoup", 12, 0, 100);
    public static final ForgeConfigSpec.IntValue THIRST_GAIN_CANTEEN = BUILDER.comment("Thirst gained from drinking a canteen").defineInRange("vitals.thirstGainCanteen", 35, 0, 100);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    // cached vitals config
    public static int tempHypoThreshold;
    public static int tempHyperThreshold;
    public static int thirstDrainInterval;
    public static int thirstDrainIdle;
    public static int thirstDrainSprinting;
    public static int thirstGainWater;
    public static int thirstGainMilk;
    public static int thirstGainSoup;
    public static int thirstGainCanteen;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream().map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName))).collect(Collectors.toSet());

        tempHypoThreshold = TEMP_HYPO_THRESHOLD.get();
        tempHyperThreshold = TEMP_HYPER_THRESHOLD.get();
        thirstDrainInterval = THIRST_DRAIN_INTERVAL.get();
        thirstDrainIdle = THIRST_DRAIN_IDLE.get();
        thirstDrainSprinting = THIRST_DRAIN_SPRINTING.get();
        thirstGainWater = THIRST_GAIN_WATER.get();
        thirstGainMilk = THIRST_GAIN_MILK.get();
        thirstGainSoup = THIRST_GAIN_SOUP.get();
        thirstGainCanteen = THIRST_GAIN_CANTEEN.get();
    }
}
