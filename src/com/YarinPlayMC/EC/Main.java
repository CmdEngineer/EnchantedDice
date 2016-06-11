package com.YarinPlayMC.EC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import io.netty.util.internal.ThreadLocalRandom;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("gamble").setPermission("ed.gamble");
		getCommand("roll").setPermission("ed.roll");
		getCommand("enchanteddice").setPermission("ed.command");
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		saveConfig();
	}
	@Override
	public void onDisable() {
	
	}
	public ChatColor Red = ChatColor.RED;
	public ChatColor Blue = ChatColor.BLUE;
	public ChatColor Aqua = ChatColor.AQUA;
	public ChatColor Bold = ChatColor.BOLD;
	public ChatColor Gray = ChatColor.GRAY;
	public ChatColor Green = ChatColor.GREEN;
	public ChatColor White = ChatColor.WHITE;
	public ChatColor Gold = ChatColor.GOLD;

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		//Command: /ed set
		if((label.equalsIgnoreCase("EnchantedDice") || label.equalsIgnoreCase("ed") || label.equalsIgnoreCase("edice")) && p.hasPermission("ed.command")){
			if(args.length == 0){
				//Command: /ed
				msg(p, "/ed {set/get}");
			}else if(args.length == 1){
				//Command: /ed set
				msg(p, "/ed {set/get} {DefaultMin/DefaultMax/DefaultValues}");
			}else if(args.length == 2){
				//Command: /ed set min/max value
				if(checkArg(args[0], "set")){
					msg(p, "/ed set {DefaultMin/DefaultMax/DefaultValues} {value}");
				}else if(checkArg(args[0], "get") && p.hasPermission("ed.command.get") || p.hasPermission("ed.command.*")){
					reloadConfig();
					if(args[1].equalsIgnoreCase("DefaultMin"))
						msg(p, "Dice Rolls Default Minimum Value: " + getDefaultMin());
					else if(args[1].equalsIgnoreCase("DefaultMax"))
						msg(p, "Dice Rolls Default Maximum Value: " + getDefaultMax());
					else if(args[1].equalsIgnoreCase("DefaultValues"))
						msg(p, "Dice Rolls Default Values: " + getDefaultMin() + "-" + getDefaultMax());
					else sendError(p);
				}else sendError(p);
			}else if(args.length >= 2){
				if(checkArg(args[0], "set") && p.hasPermission("ed.command.set") || p.hasPermission("ed.command.*")){
					reloadConfig();
					if(checkArg(args[1], "DefaultMin")){
						if(NumberUtils.isNumber(args[2])){
							getConfig().set("Values.DefaultMin", Integer.parseInt(args[2]));
							msg(p, "Dice Rolls Default Minimum Value has set to: " + Integer.parseInt(args[2]));
							saveConfig();
						}else sendError(p);
					}else if(checkArg(args[1], "DefaultMax")){
						if(NumberUtils.isNumber(args[2])){
							getConfig().set("Values.DefaultMax", Integer.parseInt(args[2]));
							msg(p, "Dice Rolls Default Maximum Value has set to: " + Integer.parseInt(args[2]));
							saveConfig();
						}else sendError(p);
					}else if(checkArg(args[1], "DefaultValues")){
						try {
							if(args[2].equalsIgnoreCase("dice")){
								getConfig().set("Values.DefaultMin", 1);
								getConfig().set("Values.DefaultMax", 6);
								msg(p, "Dice Rolls Default Values has set to: " + 1 + "-" + 6 + Gray + "  (Normal Dice Mode)");
								saveConfig();
							}else{
								reloadConfig();
								String[] Values = args[2].split("-", 2);
								int newMin = getDefaultMin();
								int newMax = getDefaultMax();
								for(String v : Values){
									if(NumberUtils.isNumber(v))
										if(v == Values[0]) newMin = Integer.parseInt(v);
										if(v == Values[1]) newMax = Integer.parseInt(v);
								}
								getConfig().set("Values.DefaultMin", newMin);
								getConfig().set("Values.DefaultMax", newMax);
								msg(p, "Dice Rolls Default Values has set to: " + newMin + "-" + newMax);
								saveConfig();
							}
						} catch (Exception e) {
							sendError(p);
						}
					}else sendError(p);
				}else sendError(p);
			}else sendError(p);
		}
		//Command: /roll {min-max}
		if(label.equalsIgnoreCase("roll") && (p.hasPermission("ed.roll"))){
			if(args.length == 0){
				//Command: /roll ----
				reloadConfig();
				sendRoll(p, getDefaultMin(), getDefaultMax(), IntRoll(getDefaultMin(), getDefaultMax()));
			}else if((args.length == 1) && (p.hasPermission("ed.roll.edit") || p.hasPermission("ed.roll.*"))){
				//Commnad: /roll min-max
				String[] Values = args[0].split("-", 2);
				int newMin = getDefaultMin();
				int newMax = getDefaultMax();
				for(String v : Values){
					if(!(Pattern.matches("[a-zA-Z]+", v))){
						if(v == Values[0]) newMin = Integer.parseInt(v);
						if(v == Values[1]) newMax = Integer.parseInt(v);
					}
				}
				if(newMin != Integer.MIN_VALUE && newMax != Integer.MAX_VALUE)
					sendRoll(p, newMin, newMax, IntRoll(newMin, newMax));
			}
			
		}
		if(label.equalsIgnoreCase("gamble")){
			//Command: /gamble {}
			if(args.length == 0){
				msg(p, "Command: /gamble {dice}");
			}else if(args.length >= 1){
				if(checkArg(args[0], "dice") && (p.hasPermission("ed.gamble.dice") || p.hasPermission("ed.gamble.*"))){
					if(args.length != 2) msg(p, "Command: /gamble dice 0-100%");
					if(args.length == 2){
						if(args[1].endsWith("%")){
							String value = args[1].replaceAll("%", "");
							if(NumberUtils.isNumber(value)){
								sendDiceGamble(p, Integer.parseInt(value));
							}
						}else sendError(p);
					}
				}else if(checkArg(args[0], "coinflip") && (p.hasPermission("ed.gamble.coinflip") || p.hasPermission("ed.gamble.*"))){
					if(args.length == 1){
						sendCoinFlip(p);
					}
				}else{
					sendError(p);
				}
			}
		}
		return false;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if(cmd.getName().equalsIgnoreCase("gamble")){
			if(args.length == 1){
				ArrayList<String> gambles = new ArrayList<String>();
				ArrayList<String> validGambles = new ArrayList<String>();
				validGambles.add("dice");
				validGambles.add("coinflip");
				for(String str : validGambles){
					if(str.toLowerCase().startsWith(args[0].toLowerCase())){
						gambles.add(str);
					}
				}
                Collections.sort(gambles);
                return gambles;
			}
		}else if(cmd.getName().equalsIgnoreCase("EnchantedDice") || cmd.getName().equalsIgnoreCase("ed") || cmd.getName().equalsIgnoreCase("edice")){
			if(args.length == 1){
				ArrayList<String> SetOrGet = new ArrayList<String>();
				ArrayList<String> validSetOrGet = new ArrayList<String>();
				validSetOrGet.add("set");
				validSetOrGet.add("get");
				for(String str : validSetOrGet){
					if(str.toLowerCase().startsWith(args[0].toLowerCase())){
						SetOrGet.add(str);
					}
				}
				Collections.sort(SetOrGet);
				return SetOrGet;
			}else if(args.length == 2){
				if(checkArg(args[0], "set")){
					ArrayList<String> setValues = new ArrayList<String>();
					ArrayList<String> validSetValues = new ArrayList<String>();
					validSetValues.add("DefaultMin");
					validSetValues.add("DefaultMax");
					validSetValues.add("DefaultValues");
					for(String str : validSetValues){
						if(str.toLowerCase().startsWith(args[1].toLowerCase())){
							setValues.add(str);
						}
					}
					Collections.sort(setValues);
					return setValues;
				}else if(checkArg(args[0], "get")){
					ArrayList<String> getValues = new ArrayList<String>();
					ArrayList<String> validGetValues = new ArrayList<String>();
					validGetValues.add("DefaultMin");
					validGetValues.add("DefaultMax");
					validGetValues.add("DefaultValues");
					for(String str : validGetValues){
						if(str.toLowerCase().startsWith(args[1].toLowerCase())){
							getValues.add(str);
						}
					}
					Collections.sort(getValues);
					return getValues;
				}
			}
		}
		return null;
	}
	public int IntRoll(int min, int max){
		if(!(min > max)){
			int result = ThreadLocalRandom.current().nextInt(min, max + 1);
			return result;
		}else return 0;
	}
	public double DoubleRoll(double min, double max){
		if(!(min > max)){
			double result = ThreadLocalRandom.current().nextDouble(min, max + 1);
			return result;
		}else return 0;
	}
	public int getIntValue(String arg, String value){
		if(value == "min" || value == "max"){
			try {
				String[] Values = arg.split("-", 2);
				for(String v : Values){
					if(!(Pattern.matches("[a-zA-Z]+", v))){
						if(v == Values[0]){
							if(value == "min")
								return Integer.parseInt(Values[0]);
						}else{
							if(value == "max")
								return Integer.parseInt(Values[1]);
						}
					}
				}
			} catch (Exception e) {
				if(value == "min")
					return getDefaultMin();
				else if(value == "max")
					return getDefaultMax();
				else return 0;
			}
		}
		if(value == "min")
			return getDefaultMin();
		else if(value == "max")
			return getDefaultMax();
		else return 0;
	}
	public void sendRoll(Player player, int min, int max, int value){
 		Bukkit.getConsoleSender().sendMessage(player.getDisplayName() + " has Rolled: " + value + "( " + min + "-" + max + ")");
		for(Player p : Bukkit.getServer().getOnlinePlayers())
			p.sendMessage(Red + "Dice Roll" + Blue + " > " + player.getDisplayName() + Blue + " has Rolled: " + ChatColor.WHITE + Bold + value + Gray +" (" + min +"-"+ max + ")");
	}
	public void sendDiceGamble(Player player, int percentage){
		int log = ThreadLocalRandom.current().nextInt(0, 100 + 1);
		if(log < percentage){
			Bukkit.getConsoleSender().sendMessage(player.getDisplayName() + " Won a Gamble of: " + percentage + "% (" + log + ")");
			for(Player p : Bukkit.getServer().getOnlinePlayers())
				p.sendMessage(Red + "Gamble Dice" + Blue + " > " + player.getDisplayName() + Green + " Won a Gamble of: " + ChatColor.WHITE + Bold + percentage + "% " + Gray + "(" + log + ")");
		}else{
			Bukkit.getConsoleSender().sendMessage(player.getDisplayName() + " Lost a Gamble of: " + percentage + "% (" + log + ")");
			for(Player p : Bukkit.getServer().getOnlinePlayers())
				p.sendMessage(Red + "Gamble Dice" + Blue + " > " + player.getDisplayName() + Red + " Lost a Gamble of: " + ChatColor.WHITE + Bold + percentage + "% " + Gray + "(" + log + ")");
		}
	}
	public void sendCoinFlip(Player p){
		int log = ThreadLocalRandom.current().nextInt(0, 1 + 1);
		if(log == 1){ 
			p.sendMessage(Red + "Gamble Coin Flip" + Blue + " >" + Gold + " You" + " Flipped a coin to: " + ChatColor.WHITE + Bold + " Heads!");
			Bukkit.getConsoleSender().sendMessage(p.getDisplayName() + " has flipped a coin to: Heads!");
		}else if(log == 0){
			p.sendMessage(Red + "Gamble Coin Flip" + Blue + " >" + Gold + " You" + " Flipped a coin to: " + ChatColor.WHITE + Bold + " Tails!");
	 		Bukkit.getConsoleSender().sendMessage(p.getDisplayName() + " has flipped a coin to: Tails!");
		}else{
	 		Bukkit.getConsoleSender().sendMessage("" + log);
		}
	}
	public void sendError(Player p){
		p.sendMessage(Aqua + "["+ Red + "Enchanted Dice" + Aqua + "] " + Gray + "An Error has Occured");
	}
	public void msg(Player p, String msg){
		p.sendMessage(Red + "Enchanted Dice" + Blue + " > " + Gray + msg);
	}
	public boolean checkArg(String arg, String check){
		if(arg.equalsIgnoreCase(check)) return true;
			else return false;
	}
	public int getDefaultMin(){
		reloadConfig();
		return getConfig().getInt("Values.DefaultMin");
	}
	public int getDefaultMax(){
		reloadConfig();
		return getConfig().getInt("Values.DefaultMax");
	}
}