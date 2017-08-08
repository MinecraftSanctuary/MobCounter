package com.darkzek.MobCounter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by darkzek on 24/05/17.
 */

/*
   TODO: Separate into more classes, add custom enchantments
 */
public class MobCounter extends JavaPlugin implements Listener, CommandExecutor {

    FileConfiguration config;

    HashMap<String, Double> players = new HashMap();
    ArrayList<String> names = new ArrayList<String>();
    HashMap<Player, Location> locations = new HashMap<Player, Location>();

    @Override
    public void onEnable() {
        config = getConfig();

        //Check if config exists
        if (!new File(getDataFolder().toString()).exists()) {
            saveConfig();
        }

        this.getServer().getPluginManager().registerEvents(this, this);
        loadPoints();

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                ArrayList<Player> players = new ArrayList<Player>();
                //Put all online players into the list
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player);
                }
                for (Player player : players) {
                    double points = getPoints(player.getUniqueId().toString());
                    if (locations.get(player) != null) {
                        //Make sure they're in the same world
                        if (locations.get(player).getWorld().getName() == player.getWorld().getName()) {
                            double difference = locations.get(player).distance(player.getLocation());
                            points += difference;

                            setPoints(player.getUniqueId().toString(), points);
                        }
                    }
                }
                //Set new points locations
                locations = new HashMap<Player, Location>();
                for (Player player : players) {
                    locations.put(player, player.getLocation());
                }
            }
        }, 60, 100);
    }

    @Override
    public void onDisable() {
        savePoints();
    }

    public String[] toStringArray(Object[] obj) {
        String[] array = new String[obj.length];
        for (int i = 0; i < obj.length; i++) {
            array[i] = (String) obj[i];
        }
        return array;
    }

    public void savePoints() {
        //Get all playernames
        String[] playerNames = toStringArray(names.toArray());

        for (int i = 0; i < playerNames.length; i++) {
            String name = playerNames[i];
            config.set(name, players.get(name));
        }
        config.set("UUID", names);
        //Had troubles saving, this will work fine
        config.saveToString();
        saveConfig();
        reloadConfig();
        config.saveToString();
        saveConfig();
    }

    public void loadPoints() {
        String[] playerNames = toStringArray(config.getStringList("UUID").toArray());

        //Loop for every player
        for (int i = 0; i < playerNames.length; i++) {
            String name = playerNames[i];
            //Get score and put it into the list of players
            players.put(name, config.getDouble(name));
            if (!names.contains(name)) {
                names.add(name);
            }
        }
    }

    public double getPoints(String UUID) {

        //If that player has never killed a mob before
        if (!players.containsKey(UUID)) {
            //Add him to the system
            setPoints(UUID, 0);
        }

        double points = players.get(UUID);

        return points;
    }

    public void setPoints(String UUID, double points) {
        players.put(UUID, points);
        if (!names.contains(UUID)) {
            names.add(UUID);
        }
    }

    boolean allowedBlock(Block block) {
        if (block.getType() == Material.SEEDS || block.getType() == Material.BEETROOT_SEEDS || block.getType() == Material.MELON_SEEDS || block.getType() == Material.PUMPKIN_SEEDS || block.getType() == Material.POTATO) {
            return false;
        }
        return true;
    }


    public boolean isMultiple(double number, int multiple) {
        if (number % multiple == 0) {
            return true;
        }
        return false;
    }
}
