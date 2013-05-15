package com.swifteh;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Player;

public class Map {

	ArrayList<Pair> list = new ArrayList<Pair>();

	void add(Player p, double i) {
		list.add(new Pair(p, i));
	}

	void remove(Player p) {
		Iterator<Pair> i = list.iterator();
		while (i.hasNext())
			if (i.next().p.getName().equals(p.getName())) {
				i.remove();
				return;
			}
	}
	
	double get(Player p){
		for(Pair pair : list){
			if (pair.p.getName().equals(p.getName()))
					return pair.i;
		}
		return -1;
	}
}
