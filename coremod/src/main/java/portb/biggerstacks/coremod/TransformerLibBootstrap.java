/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.coremod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.slw.MyLoggerFactory;
import portb.transformerlib.TransformerLib;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Bootstraps {@link TransformerLib} (the XML driven runtime ASM transformer engine) on FancyModLoader 10.
 * <p>
 * This class is deliberately free of any reference to game layer classes, because on FML the
 * {@code ClassProcessorProvider} that registers our {@link net.neoforged.neoforgespi.transformation.ClassProcessor}
 * is loaded from the FML plugin layer. The only game layer dependent piece, the global stack size supplier, is handed
 * in by {@link portb.biggerstacks.TransformerEngine} through {@link #setGlobalStackSizeSupplier(Supplier)}.
 */
public final class TransformerLibBootstrap
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerLibBootstrap.class);

    private static final String TRANSFORMER_DIR = "/transformers";
    /**
     * Matches the {@code <transformers>} root element and captures everything inside it.
     */
    private static final Pattern TRANSFORMER_ROOT = Pattern.compile("(?s)<transformers[^>]*>(.*)</transformers>");

    private static final String TRANSFORMER_FIELD = "transformers";

    private static volatile boolean ready;
    private static List<String> loadedFiles = List.of();

    private TransformerLibBootstrap()
    {
    }

    /**
     * Loads the XML transformer definitions with the given class as the resource anchor.
     * <p>
     * Must be called from the game layer (or from a class loader that can see the mod resources); the XML files live
     * next to the mod's classes while the class processors themselves are loaded from the FML plugin layer.
     *
     * @return {@code true} if the engine is usable
     */
    public static synchronized boolean initialize(Class<?> resourceContext)
    {
        if (ready)
            return true;

        // The XML definitions may have been loaded by another copy of this class (FML can expose the coremod from
        // both the plugin layer and the game layer). TransformerLib itself is shared, so its state is authoritative.
        if (isTransformerListLoaded())
        {
            ready = true;
            return true;
        }

        // TransformerLib logs long before the mod constructor runs, so give it a working slf4j adapter first.
        try
        {
            TransformerLib.LOGGER = MyLoggerFactory.createMyLogger(LoggerFactory.getLogger(TransformerLib.class));
        }
        catch (Throwable t)
        {
            // A logger is nice to have, not required for correct behaviour.
            LOGGER.debug("Could not install the TransformerLib logger adapter", t);
        }

        List<String> files;
        try
        {
            files = listTransformerFiles(resourceContext);
        }
        catch (Throwable t)
        {
            // Expected when this is called from the FML plugin layer, which cannot see the mod jar that contains the
            // XML files. The game layer caller will retry with a class loader that can see them.
            LOGGER.debug("Could not enumerate " + TRANSFORMER_DIR + " resources through " + resourceContext.getName(), t);
            return false;
        }

        if (files.isEmpty())
        {
            LOGGER.debug("No transformer definitions visible through " + resourceContext.getName());
            return false;
        }

        try
        {
            TransformerLib.loadTransformers(resourceContext);
        }
        catch (Throwable primaryFailure)
        {
            // TransformerLib.loadTransformers resolves "<root>/" + <absolute path> through getResourceAsStream, which
            // never resolves for both file:// (dev) and jar:// (prod) class loader layouts, so it reliably throws.
            // Load the very same files through TransformerLib's public XML entry point instead.
            LOGGER.debug("TransformerLib.loadTransformers() failed ({}), using the merged XML loader", primaryFailure.toString());
            try
            {
                loadMergedTransformers(resourceContext, files);
            }
            catch (Throwable t)
            {
                LOGGER.error("Could not load the XML transformer definitions through " + resourceContext.getName(), t);
                return false;
            }
        }

        loadedFiles = files;
        ready = true;

        int targetCount = TransformerTargetIndex.refresh();
        LOGGER.info("XML transformer engine ready: {} file(s) loaded, {} class target(s)", files.size(), targetCount);
        return true;
    }

    /**
     * @return {@code true} once the transformer definitions have been loaded, no matter which copy of this class
     *         loaded them
     */
    public static boolean isReady()
    {
        if (ready)
            return true;

        if (isTransformerListLoaded())
        {
            ready = true;
            return true;
        }

        return false;
    }

    private static boolean isTransformerListLoaded()
    {
        try
        {
            Field field = TransformerLib.class.getDeclaredField(TRANSFORMER_FIELD);
            field.setAccessible(true);
            return field.get(null) != null;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    /**
     * @return the transformer definition files that were loaded, for diagnostics
     */
    public static List<String> getLoadedFiles()
    {
        return loadedFiles;
    }

    /**
     * Forwards the global stack size supplier to the transformer library. The supplier itself must be provided by a
     * game layer class because it reads the synced ruleset.
     */
    public static void setGlobalStackSizeSupplier(Supplier<Integer> supplier)
    {
        TransformerLib.setGlobalStackLimitSupplier(supplier);
    }

    private static List<String> listTransformerFiles(Class<?> resourceContext) throws Exception
    {
        URL directory = resourceContext.getResource(TRANSFORMER_DIR);
        if (directory == null)
            return List.of();

        List<String> names = new ArrayList<>();
        String protocol = directory.getProtocol();

        if ("file".equals(protocol))
        {
            Path path = Path.of(directory.toURI());
            try (Stream<Path> stream = Files.list(path))
            {
                stream.filter(Files::isRegularFile).map(file -> file.getFileName().toString()).forEach(names::add);
            }
        }
        else if ("jar".equals(protocol))
        {
            JarURLConnection connection = (JarURLConnection) directory.openConnection();
            connection.setUseCaches(false);
            try (JarFile jarFile = connection.getJarFile())
            {
                String prefix = connection.getEntryName();
                java.util.Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(prefix + "/") && !entry.isDirectory())
                        names.add(name.substring(prefix.length() + 1));
                }
            }
        }
        else
        {
            throw new IllegalStateException("Unsupported class loader layout for " + TRANSFORMER_DIR + ": " + protocol);
        }

        names.sort(Comparator.naturalOrder());
        return names;
    }

    /**
     * Parses every individual XML file and hands the concatenated content to
     * {@link TransformerLib#loadTransformersFromXML(String)} in one go, because that method replaces the loaded
     * transformer list on every call.
     */
    private static void loadMergedTransformers(Class<?> resourceContext, List<String> files) throws Exception
    {
        StringBuilder merged = new StringBuilder(1024).append("<transformers>");

        for (String file : files)
        {
            String xml = readResource(resourceContext, TRANSFORMER_DIR + "/" + file);
            Matcher matcher = TRANSFORMER_ROOT.matcher(xml);
            if (!matcher.find())
                throw new IllegalStateException("Transformer file " + file + " has no <transformers> root element");

            merged.append('\n').append(matcher.group(1));
        }

        merged.append('\n').append("</transformers>");
        TransformerLib.loadTransformersFromXML(merged.toString());
    }

    private static String readResource(Class<?> resourceContext, String path) throws Exception
    {
        try (InputStream stream = resourceContext.getResourceAsStream(path))
        {
            if (stream == null)
                throw new IllegalStateException("Missing resource " + path);

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
