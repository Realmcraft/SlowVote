package com.swifteh;

import java.util.ArrayList;

import org.bukkit.configuration.ConfigurationSection;

public class SVCommand {

	public SVCommand(String commandName, int int1, ConfigurationSection cs) {
		name = commandName;
		time = int1;
		for (Object o : cs.getValues(false).values()) {
			commands.add((String) o);
		}
	}

	public ArrayList<String> commands = new ArrayList<String>();
	public String name;
	public int time;
}
