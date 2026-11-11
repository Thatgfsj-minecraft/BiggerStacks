/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.coremod.TransformerLibBootstrap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mixin config plugin.
 * <p>
 * Vanilla patches are always applied. Compatibility patches under
 * {@code portb.biggerstacks.mixin.compat.<modid>.*} are only applied when the target mod is installed.
 * <p>
 * The 1.20.1 version of this class also injected an {@code ILaunchPluginService} into ModLauncher through reflection
 * to run the XML based TransformerLib patches for third party mods. ModLauncher is gone in NeoForge 21.11 /
 * FancyModLoader 10, so those patches are now applied by a {@code ClassProcessor} implemented in
 * {@link portb.biggerstacks.coremod}. The static initialiser below keeps the two parts that have to run in the game
 * layer: loading the XML definitions (they live in the mod jar) and teaching the transformer library how to query the
 * current maximum stack size.
 */
public class TransformerEngine implements IMixinConfigPlugin
{
    private static final Map<String, List<String>> MODS_THAT_CONFLICT_WITH_PATCHES = Map.of(
            "portb.biggerstacks.mixin.vanilla.AnvilMenuMixin", List.of("tiered")
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerEngine.class);
    private static final Pattern MOD_ID_PACKAGE_TARGET_PATTERN = Pattern.compile(
            "mixin\\.compat\\.([^.]+)\\.[^.]+$");

    static
    {
        // The mixin plugin is the earliest game layer entry point, so this is where the XML transformer definitions
        // are loaded. This is idempotent: when the class processor is already linked it will have done this before.
        TransformerLibBootstrap.initialize(TransformerEngine.class);

        // The library needs to know how to get the maximum stack size. This must happen from the game layer because
        // StackSizeRules reads the ruleset that is synced from the server / read from the config file.
        TransformerLibBootstrap.setGlobalStackSizeSupplier(StackSizeRules::getMaxStackSize);

        LOGGER.info("XML transformer engine {}", TransformerLibBootstrap.isReady()
                ? "is active"
                : "is NOT active, XML based compatibility patches are disabled");
    }

    private static boolean isModLoaded(String modId)
    {
        var loader = FMLLoader.getCurrentOrNull();
        return loader != null && loader.getLoadingModList().getModFileById(modId) != null;
    }

    private static boolean isAnyModInstalled(List<String> listOfMods)
    {
        for (var modId : listOfMods)
        {
            if (isModLoaded(modId))
                return true;
        }

        return false;
    }

    @Override
    public void onLoad(String mixinPackage)
    {
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        Matcher matcher = MOD_ID_PACKAGE_TARGET_PATTERN.matcher(mixinClassName);

        if (matcher.find())
        {
            String modId = matcher.group(1);
            boolean isModLoaded = isModLoaded(modId);

            if (isModLoaded)
                LOGGER.info(modId + " is installed, applying patches");
            else
                LOGGER.debug(modId + " is NOT installed");

            return isModLoaded;
        }
        else
        {
            if (!MODS_THAT_CONFLICT_WITH_PATCHES.containsKey(mixinClassName))
            {
                return true;
            }
            else
            {
                //disable the patch if a mod that conflicts with it is installed
                return !isAnyModInstalled(MODS_THAT_CONFLICT_WITH_PATCHES.get(mixinClassName));
            }
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }
}
