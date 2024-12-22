package com.kweezy.joinmessage;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigFile {

    public static String getWelcomeMessage() {
        return welcomeMessage;
    }

    private static String welcomeMessage = "";

    public static List<String> getBookText() {
        return bookText;
    }

    private static List<String> bookText = new ArrayList<>();

    private static boolean randomRespawn;

    public static boolean isRandomRespawn() {
        return randomRespawn;
    }

    public static int getRandomRespawnRadius() {
        return randomRespawnRadius;
    }

    private static int randomRespawnRadius;

    FileConfiguration config;

    public ConfigFile(FileConfiguration config) throws IOException {
        this.config = config;

        config.addDefault("showWelcomeMessage", true);

        config.addDefault("giveBookOnFirstJoin", true);

        config.addDefault("randomRespawn", true);
        config.addDefault("randomRespawnRadius", 100);

        randomRespawn = config.getBoolean("randomRespawn");
        randomRespawnRadius = config.getInt("randomRespawnRadius");

        if (config.getBoolean("showWelcomeMessage")) {
            File welcomemessagefile = new File(joinMessage.instance.getDataFolder(), "welcome.txt");
            welcomeMessage = colorize(readFileStrings(welcomemessagefile));
        }

        if (config.getBoolean("giveBookOnFirstJoin")) {
            File bookfile = new File(joinMessage.instance.getDataFolder(), "book.txt");
            List<String> lines = readFileList(bookfile, "\n-----\n");
            for (String line : lines) {
                bookText.add(colorize(line));
            }
        }

        config.options().copyDefaults(true);
        joinMessage.instance.saveConfig();

    }

    private String readFileStrings(File file) throws IOException {
        file.createNewFile();
        return new String(Files.readAllBytes(file.toPath())).replaceAll("\\r", "");
    }

    private List<String> readFileList(File file, String delimeter) throws IOException {
        return Arrays.asList(readFileStrings(file).split(delimeter));
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
