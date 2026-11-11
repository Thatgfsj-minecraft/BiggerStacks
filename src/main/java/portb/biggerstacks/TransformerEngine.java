/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
 * NOTE: The Forge/NeoForge versions of BiggerStacks also run an XML based TransformerLib class transformer
 * for third-party mods. Fabric has no equivalent loader level class transformation hook, so the Fabric port
 * is mixin-only; compatibility modules therefore have to be implemented as regular mixins.
 */
public class TransformerEngine implements IMixinConfigPlugin
{
    private static final Map<String, List<String>> MODS_THAT_CONFLICT_WITH_PATCHES = Map.of(
            "portb.biggerstacks.mixin.vanilla.AnvilMenuMixin", List.of("tiered")
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerEngine.class);
    private static final Pattern MOD_ID_PACKAGE_TARGET_PATTERN = Pattern.compile(
            "mixin\\.compat\\.([^.]+)\\.[^.]+$");

    private static boolean isModLoaded(String modId)
    {
        return FabricLoader.getInstance().isModLoaded(modId);
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
            boolean loaded = isModLoaded(modId);

            if (loaded)
                LOGGER.info(modId + " is installed, applying patches");
            else
                LOGGER.debug(modId + " is NOT installed");

            return loaded;
        }
        else
        {
            if (!MODS_THAT_CONFLICT_WITH_PATCHES.containsKey(mixinClassName))
                return true;
            else
                return !isAnyModInstalled(MODS_THAT_CONFLICT_WITH_PATCHES.get(mixinClassName));
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
