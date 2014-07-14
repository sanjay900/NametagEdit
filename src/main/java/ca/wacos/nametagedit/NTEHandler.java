package ca.wacos.nametagedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class loads all group/player data, and applies the tags during
 * reloads/individually
 * 
 * @author sgtcaze
 */
public class NTEHandler {

	private NametagEdit plugin;

	public NTEHandler(NametagEdit plugin) {
		this.plugin = plugin;
	}

	private HashMap<String, List<String>> groupData = new HashMap<>();
	public HashMap<String, List<String>> playerData = new HashMap<>();
	private HashMap<String, String> permissions = new HashMap<>();

	public void softReload() {
		savePlayerData();
		applyTags();
	}

	public void hardReload() {
		savePlayerData();
		plugin.reloadConfig();
		plugin.getFileUtils().loadGroupsYaml();
		loadGroups();
		loadPlayers();
		applyTags();
	}

	public void savePlayerData() {
		for (String s : playerData.keySet()) {
			List<String> temp = playerData.get(s);
			plugin.players.set("Players." + s + ".Name", temp.get(0)
					.replaceAll("§", "&"));
			plugin.players.set("Players." + s + ".Prefix", temp.get(1)
					.replaceAll("§", "&"));
			plugin.players.set("Players." + s + ".Suffix", temp.get(2)
					.replaceAll("§", "&"));
		}

		plugin.getFileUtils().savePlayersFile();
	}

	public void loadGroups() {
		groupData.clear();

		for (String s : plugin.groups.getConfigurationSection("Groups")
				.getKeys(false)) {
			List<String> tempData = new ArrayList<>();
			String prefix = plugin.groups.getString("Groups." + s + ".Prefix");
			String suffix = plugin.groups.getString("Groups." + s + ".Suffix");
			String permission = plugin.groups.getString("Groups." + s
					+ ".Permission");

			tempData.add(format(prefix));
			tempData.add(format(suffix));
			tempData.add(permission);

			groupData.put(s, tempData);
			permissions.put(permission, s);
		}
	}

	public void loadPlayers() {
		playerData.clear();

		for (String s : plugin.players.getConfigurationSection("Players")
				.getKeys(false)) {
			List<String> tempData = new ArrayList<>();
			String name = plugin.players.getString("Players." + s + ".Name");
			String prefix = plugin.players
					.getString("Players." + s + ".Prefix");
			String suffix = plugin.players
					.getString("Players." + s + ".Suffix");

			tempData.add(name);
			tempData.add(format(prefix));
			tempData.add(format(suffix));

			playerData.put(s, tempData);
		}
	}

	// This is a workaround for the deprecated getOnlinePlayers(). Credit to
	// @Goblom for suggesting
	public List<Player> getOnline() {
		List<Player> list = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			list.addAll(world.getPlayers());
		}
		return Collections.unmodifiableList(list);
	}

	public void applyTags() {
		for (Player p : getOnline()) {
			if (p != null) {
				String uuid = p.getUniqueId().toString();

				if (playerData.containsKey(uuid)) {
					List<String> temp = playerData.get(uuid);
					NametagManager.overlap(p.getName(), temp.get(1),
							temp.get(2));
				} else {
					String permission = "";

					for (String s : groupData.keySet()) {
						List<String> temp = groupData.get(s);

						if (p.hasPermission(temp.get(2))) {
							permission = temp.get(2);
							break;
						}
					}

					String group = permissions.get(permission);
					List<String> temp = groupData.get(group);

					if (temp != null) {
						NametagCommand
								.setNametagSoft(
										p.getName(),
										temp.get(0),
										temp.get(1),
										NametagChangeEvent.NametagChangeReason.GROUP_NODE);
					}

					if (plugin.tabListDisabled) {
						String str = "§f" + p.getName();
						String tab = "";
						for (int t = 0; t < str.length() && t < 16; t++) {
							tab += str.charAt(t);
						}
						p.setPlayerListName(tab);
					}
				}
			}
		}
	}

	public void applyTagToPlayer(Player p) {
		String uuid = p.getUniqueId().toString();

		NametagManager.clear(p.getName());

		if (playerData.containsKey(uuid)) {
			List<String> temp = playerData.get(uuid);
			NametagManager.overlap(p.getName(), temp.get(1), temp.get(2));
		} else {
			String permission = "";

			Permission perm = null;

			for (String s : groupData.keySet()) {
				List<String> temp = groupData.get(s);
				perm = new Permission(temp.get(2), PermissionDefault.FALSE);
				if (p.hasPermission(perm)) {
					permission = temp.get(2);
				}
			}

			String group = permissions.get(permission);

			List<String> temp = groupData.get(group);

			if (temp != null) {
				NametagCommand.setNametagSoft(p.getName(), temp.get(0),
						temp.get(1),
						NametagChangeEvent.NametagChangeReason.GROUP_NODE);
			}
		}

		if (plugin.tabListDisabled) {
			String str = "§f" + p.getName();
			String tab = "";
			for (int t = 0; t < str.length() && t < 16; t++) {
				tab += str.charAt(t);
			}
			p.setPlayerListName(tab);
		}
	}

	private String format(String input) {
		return trim(ChatColor.translateAlternateColorCodes('&', input));
	}

	private String trim(String input) {
		if (input.length() > 16) {
			String temp = input;
			input = "";
			for (int t = 0; t < 16; t++) {
				input += temp.charAt(t);
			}
		}
		return input;
	}
}