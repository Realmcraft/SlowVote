package com.swifteh;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisRunnable implements Runnable{

	@Override
	public void run() {
		if (SlowVote.spawn == null) return;
		for(Player p : SlowVote.spawn.getWorld().getPlayers()){
			try{
			if (p.getLocation().distance(SlowVote.spawn) < 3){
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 59, 1));
			}
			}catch(Exception e){
				System.out.println("Error applying invis to " + p.getName());
			}
		}
	}

}
