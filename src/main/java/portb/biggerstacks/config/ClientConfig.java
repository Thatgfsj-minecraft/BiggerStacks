/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import portb.biggerstacks.BiggerStacks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Client side config. Kept as a plain JSON file because Fabric has no built-in config API.
 */
public class ClientConfig
{
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("biggerstacks-client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean enableNumberShortening = true;
    public static String  numberColour           = ChatFormatting.DARK_AQUA.getName();

    public static void load()
    {
        if (Files.exists(FILE))
        {
            try
            {
                JsonObject json = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
                if (json.has("enableNumberShortening"))
                    enableNumberShortening = json.get("enableNumberShortening").getAsBoolean();
                if (json.has("numberColour"))
                    numberColour = json.get("numberColour").getAsString();
            }
            catch (IOException | RuntimeException e)
            {
                BiggerStacks.LOGGER.warn("Could not read {}", FILE, e);
            }
        }
        else
        {
            save();
        }
    }

    public static void save()
    {
        JsonObject json = new JsonObject();
        json.addProperty("enableNumberShortening", enableNumberShortening);
        json.addProperty("numberColour", numberColour);

        try
        {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(json), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            BiggerStacks.LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    public static ChatFormatting getNumberColour()
    {
        try
        {
            return Objects.requireNonNull(ChatFormatting.getByName(numberColour.toLowerCase(Locale.ROOT)));
        }
        catch (RuntimeException e)
        {
            return ChatFormatting.DARK_AQUA;
        }
    }
}
