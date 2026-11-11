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
 * NOTE: The 1.20.1/1.21.1 versions of BiggerStacks also injected an ILaunchPluginService here to run
 * the XML based TransformerLib patches for third-party mods. That machinery is deliberately not part
 * of the 1.21.11 core port yet; when compatibility modules are restored the transformer engine has to
 * be re-implemented against the current ModLauncher API (or replaced by regular mixins).
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
