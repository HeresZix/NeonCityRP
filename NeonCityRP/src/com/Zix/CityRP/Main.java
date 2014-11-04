package com.Zix.CityRP;

import java.util.ArrayList;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
	
	ArrayList<Player> civ = new ArrayList<Player>();
	ArrayList<Player> police = new ArrayList<Player>();
	ArrayList<Player> isbanking = new ArrayList<Player>();
	
	public String ospre = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "MineOS" + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE;
	public int civsalary = 72800;
	public int policesalary = 82800;
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		String uuid = p.getUniqueId().toString();
		String job = getConfig().getString(uuid + ".job");
		
		doBarStuff(p);

		ParticleEffect.HUGE_EXPLOSION.display(1, 1, 1, 10, 10, p.getLocation(), 2);
		
		if(getConfig().contains(uuid)){
			p.sendMessage(ospre + "Welcome back, " + p.getName() + ".");
			if(job.equals("civ")){
				civ.add(p);
				p.sendMessage("added to civ array");
			} else if(job.equals("police")){
				if(!CustomItems.hasBaton(p)){
					p.sendMessage("recieved a baton!");
					CustomItems.giveBaton(p);
				}
				p.sendMessage("added to police array");
				police.add(p);
			}
		} else {
			civ.add(p);
			p.sendMessage(ospre + "Welcome, " + p.getName() + ".");
			p.sendMessage(ospre + "I am " + ChatColor.BLUE + "MineOS" + ChatColor.WHITE + ", and I am here to help you.");
			p.sendMessage(ospre + "Your details have been added to my database.");
			getConfig().set(uuid + ".job", "civ");
			getConfig().set(uuid + ".atm", 40000);
			getConfig().set(uuid + ".pocket", 500);
			saveConfig();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		Block blk = e.getClickedBlock();
		Player p = e.getPlayer();
		
		if(blk != null) {
			if(blk.getType() == Material.EMERALD_BLOCK){
				int bnkblnc = getConfig().getInt(p.getUniqueId().toString() + ".atm");
				int pktblnc = getConfig().getInt(p.getUniqueId().toString() + ".pocket");
				isbanking.add(p);
				p.sendMessage(ospre + "You are now in banking mode, type " + ChatColor.DARK_GREEN + "/stopbanking" + ChatColor.WHITE + " to cancel.");
				p.sendMessage(ospre + "Account balance: " + ChatColor.GREEN + bnkblnc);
				p.sendMessage(ospre + "Money in pocket: " + ChatColor.GREEN + pktblnc);
				p.sendMessage(ospre + "Use " + ChatColor.DARK_GREEN + "/deposit <amt>" + ChatColor.WHITE + " to deposit money.");
				p.sendMessage(ospre + "Use " + ChatColor.DARK_GREEN + "/withdraw <amt>" + ChatColor.WHITE + " to withdraw money.");
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		if(isbanking.contains(p)){
			e.setCancelled(true);
		} else {
			e.setCancelled(false);
		}
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		
		World world = Bukkit.getWorld("world");		
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
		    @Override
		    public void run()
		    {
		        if(world.getTime() == 0){
		        	for(Player p : Bukkit.getOnlinePlayers()){
		        		String uuid = p.getUniqueId().toString();
		        		if(getConfig().getString(uuid + ".job").equals("civ")){
		        			p.sendMessage(ospre + "Your paycheck of $200 has been deposited into your account");
		        			int curamt = getConfig().getInt(uuid + ".atm");
		        			int newamt = curamt + 200;
		        			getConfig().set(uuid + ".atm", newamt);
		        			saveConfig();
		        		} else if(getConfig().getString(uuid + ".job").equals("police")){
		        			p.sendMessage(ospre + "Your paycheck of $300 has been deposited into your account");
		        			int curamt = getConfig().getInt(uuid + ".atm");
		        			int newamt = curamt + 300;
		        			getConfig().set(uuid + ".atm", newamt);
		        			saveConfig();
		        		}
		        	}
		        }
		    }
		}, 0, 1);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		String uuid = p.getUniqueId().toString();
		
		if(label.equals("stopbanking")){
			stopBanking(p);
		} else if(label.equals("deposit")){
			if(isbanking.contains(p)){
				if(args.length == 1){
					if(isInt(args[0])){
						int pocketbal = getConfig().getInt(uuid + ".pocket");
						int bankbal = getConfig().getInt(uuid + ".atm");
						int amt = Integer.parseInt(args[0]);
						if(!(amt > pocketbal)){
							int newbankamt = bankbal + amt;
							int newpocketamt = pocketbal - amt;
							getConfig().set(uuid + ".pocket", newpocketamt);
							getConfig().set(uuid + ".atm", newbankamt);
							saveConfig();
							p.sendMessage(ospre + "You have deposited " + ChatColor.GREEN + "$" + amt + ChatColor.WHITE + " into your account.");
							p.sendMessage(ospre + "New balance: " + ChatColor.GREEN + "$" + newbankamt + ChatColor.WHITE + ".");
							stopBanking(p);
						} else {
							p.sendMessage(ospre + "You dont have sufficient funds.");
						}
					} else {
						p.sendMessage(ospre + "Argument must be a number.");
					}
				} else {
					p.sendMessage(ospre + "Unknown command.");
				}
			} else {
				p.sendMessage(ospre + "You must be at an atm.");
			}
		} else if(label.equals("withdraw")){
			if(isbanking.contains(p)){
				if(args.length == 1){
					if(isInt(args[0])){
						int pocketbal = getConfig().getInt(uuid + ".pocket");
						int bankbal = getConfig().getInt(uuid + ".atm");
						int amt = Integer.parseInt(args[0]);
						if(!(amt > bankbal)){
							int newbankamt = bankbal - amt;
							int newpocketamt = pocketbal + amt;
							getConfig().set(uuid + ".pocket", newpocketamt);
							getConfig().set(uuid + ".atm", newbankamt);
							saveConfig();
							p.sendMessage(ospre + "You have withdrawn " + ChatColor.GREEN + "$" + amt + ChatColor.WHITE + " from your account.");
							p.sendMessage(ospre + "New balance: " + ChatColor.GREEN + "$" + newbankamt + ChatColor.WHITE + ".");
							stopBanking(p);
						} else {
							p.sendMessage(ospre + "You dont have sufficient funds.");
						}
					} else {
						p.sendMessage(ospre + "Argument must be a number.");
					}
				} else {
					p.sendMessage(ospre + "Unknown command.");
				}
			} else {
				p.sendMessage(ospre + "You must be at an atm.");
			}
		}
		
		return true;
	}
	
	public static boolean isInt(String s) {
	    try {
	        Integer.parseInt(s);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public void stopBanking(Player p){
		if(isbanking.contains(p)){
			isbanking.remove(p);
			p.sendMessage(ospre + "You are no longer in banking mode.");
		}
	}
	
	public void doBarStuff(Player p){
		BarAPI.setMessage(p, ChatColor.WHITE + p.getName() + " | Pocket: " + ChatColor.GREEN + "$" + getConfig().getInt(p.getUniqueId().toString() + ".pocket"));
		
		new BukkitRunnable() {
			 
            @Override
            public void run() {
            	doBarStuffAgain(p);
            }
 
        }.runTaskLater(this, 100);
		
	}
	
	public void doBarStuffAgain(Player p){
		BarAPI.setMessage(p, ChatColor.WHITE + p.getName() + " | ATM: " + ChatColor.GREEN + "$" + getConfig().getInt(p.getUniqueId().toString() + ".atm"));
		
		new BukkitRunnable() {
			 
            @Override
            public void run() {
            	doBarStuff(p);
            }
 
        }.runTaskLater(this, 100);
	}
	
}


















