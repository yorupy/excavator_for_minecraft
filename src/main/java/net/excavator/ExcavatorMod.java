package net.excavator;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ExcavatorMod.MODID)
public class ExcavatorMod {
    public static final String MODID = "excavator";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExcavatorMod() {
        // Правильный современный вызов шины событий для 1.20.1
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Мод Excavator успешно загружен!");
    }

     private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(ModItems.SUPER_PICKAXE.get());
            event.accept(ModItems.SUPER_HOE.get());
            event.accept(ModItems.PICKME_PICKAXE.get()); // Добавили Pickme кирку
            event.accept(ModItems.PICKME_HOE.get());     // Добавили Pickme мотыгу
        }
    }
}