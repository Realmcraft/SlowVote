package com.swifteh;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Player;

public class Map {

	ArrayList<Pair> list = new ArrayList<Pair>();

	void add(String p, double i) {
		list.add(new Pair(p, i));
	}

	void remove(Player p) {
		Iterator<Pair> i = list.iterator();
		while (i.hasNext())
			if (i.next().p.equals(p.getName())) {
				i.remove();
				return;
			}
	}
	
	double get(String string){
		for(Pair pair : list){
			if (pair.p.equals(string))
					return pair.i;
		}
		return -1;
	}
	
	void replace(Player p, double i){
		for(Pair pair : list) if (pair.p.equals(p.getName())) pair.i = i;
	}
}
