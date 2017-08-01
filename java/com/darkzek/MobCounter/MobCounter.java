package com.darkzek.MobCounter;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by darkzek on 24/05/17.
 */

/*
   TODO: Separate into more classes, add custom enchantments
 */
public class MobCounter extends JavaPlugin implements Listener, CommandExecutor {

    FileConfiguration config;

    HashMap<String, Integer> players = new HashMap();
    ArrayList<String> names = new ArrayList<String>();

    @Override
    public void onEnable() {
        config = getConfig();

        //Check if config exists
        if (!new File(getDataFolder().toString()).exists()) {
            saveConfig();
        }
        this.getServer().getPluginManager().registerEvents(this, this);
        loadPoints();
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
        //Hope no player called UUID joins..
        String[] playerNames = toStringArray(config.getStringList("UUID").toArray());


        //Loop for every player
        for (int i = 0; i < playerNames.length; i++) {
            String name = playerNames[i];
            //Get score and put it into the list of players
            players.put(name, config.getInt(name));
            if (!names.contains(name)) {
                names.add(name);
            }

        }
    }

    public int getPoints(String UUID) {

        //If that player has never killed a mob before
        if (!players.containsKey(UUID)) {
            //Add him to the system
            setPoints(UUID, 0);
        }

        int points = players.get(UUID);

        return points;
    }

    public void setPoints(String UUID, int points) {
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

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {


        Player player = event.getPlayer();

        if (!allowedBlock(event.getBlockPlaced())) {
            return;
        }

        String UUID = player.getUniqueId().toString();

        //Add point to player
        int points = getPoints(UUID);
        points++;

        //Announce it if it gets high
        if (points == 50) {
            getServer().broadcastMessage(player.getDisplayName() + ChatColor.GREEN + " is on 50 blocks placed!");
        } else {
            //Then every 50 do it.
            //Check if its a multiple of 50
            if (isMultiple(points, 200)) {
                //Announce it
                getServer().broadcastMessage(player.getDisplayName() + ChatColor.GREEN + " is on " + points + " blocks placed");
                //And discord
                getServer().dispatchCommand(getServer().getConsoleSender(), "discord bcast **" + player.getDisplayName() + "** is on **" + points + "** blocks placed");
            }
        }
        //Save player points
        setPoints(UUID, points);
    }

    public boolean isMultiple(int number, int multiple) {
        if (number % multiple == 0) {
            return true;
        }
        return false;
    }
}
