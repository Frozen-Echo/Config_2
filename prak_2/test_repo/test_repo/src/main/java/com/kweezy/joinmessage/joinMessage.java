package com.kweezy.joinmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Random;

public class joinMessage extends JavaPlugin implements Listener {
    public static ConfigFile config;

    public static joinMessage instance;

    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            config = new ConfigFile(this.getConfig());

            this.getServer().getPluginManager().registerEvents(this, this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPlayedBefore()) {
            p.teleport(getRespawnLoc()); // random teleport
            if (!ConfigFile.getBookText().isEmpty()) { // TODO: Not working, fix that
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

                BookMeta meta = (BookMeta) book.getItemMeta();
                meta.setTitle("Приветствие");
                meta.setAuthor("SERVER");
                meta.setPages(ConfigFile.getBookText());

                book.setItemMeta(meta);
                p.getInventory().setItemInHand(book);
            }
        }

        if (!ConfigFile.getWelcomeMessage().equals("")) {
            p.sendMessage(ConfigFile.getWelcomeMessage());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (ConfigFile.isRandomRespawn()) {
            if (!e.isBedSpawn()) {
                e.setRespawnLocation(getRespawnLoc()); // random teleport
            }
        }


    }

    public int randInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    Location getRespawnLoc() {
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (int i = 0; i < 10; i++) {
            int newX = spawnLocation.getBlockX() + randInt(-ConfigFile.getRandomRespawnRadius(), ConfigFile.getRandomRespawnRadius());
            int newZ = spawnLocation.getBlockZ() + randInt(-ConfigFile.getRandomRespawnRadius(), ConfigFile.getRandomRespawnRadius());
            int newY = Bukkit.getWorlds().get(0).getHighestBlockAt(newX, newZ).getY() - 1;
            Block topblock = Bukkit.getWorlds().get(0).getBlockAt(newX, newY, newZ);

            if (newY == -1) {
                Bukkit.getWorlds().get(0).getBlockAt(newX, 64, newZ).setType(Material.GLASS);
                return new Location(Bukkit.getWorlds().get(0), newX + 0.5, 65, newZ + 0.5);
            }

            if (!topblock.getType().equals(Material.STATIONARY_WATER) && !topblock.getType().equals(Material.WATER) && !topblock.getType().equals(Material.STATIONARY_LAVA) && !topblock.getType().equals(Material.LAVA)) {
                return new Location(Bukkit.getWorlds().get(0), newX + 0.5, topblock.getY(), newZ + 0.5);
            }
            System.out.println("[joinMessage] Skipping water at respawn");
        }
        return spawnLocation;
    }
}
