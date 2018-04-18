package ldcr.BridgingSkin;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import ldcr.BridgingSkin.data.PlayerSkin;
import ldcr.BridgingSkin.data.SkinSet;
import ldcr.lib.com.google.gson.Gson;
import ldcr.lib.com.google.gson.GsonBuilder;

public class BridgingSkin extends JavaPlugin implements Listener{
    public static HashMap<String,PlayerSkin> skins;
    @SuppressWarnings("deprecation")
    public static ItemStack getItem(final Player p) {
	final SkinSet skin = getSkin(p.getUniqueId().toString()).currentSkin;
	Material material = Material.getMaterial(skin.material);
	if ((material==null) || SkinSelectCommand.isIllegal(material)) {
	    material = Material.SANDSTONE;
	}
	return new ItemStack(material,64,(short)0,skin.data);
    }
    public static PlayerSkin getSkin(final String uuid) {
	PlayerSkin skin;
	if (!skins.containsKey(uuid)) {
	    final File file = new File(rootDir,uuid+".json");
	    if (file.exists()) {
		try {
		    skin = gson.fromJson(new FileReader(file), PlayerSkin.class);
		} catch (final Exception e) {
		    skin = new PlayerSkin(uuid);
		}
	    } else {
		skin = new PlayerSkin(uuid);
	    }
	    skins.put(uuid, skin);
	    return skin;
	}
	skin = skins.get(uuid);
	return skin;
    }
    private static File rootDir;
    private static Gson gson;
    @Override
    public void onEnable() {
	rootDir = new File("plugins" + File.separator + "BridgingSkin"+File.separator+"skins");
	if (!rootDir.exists()) {
	    rootDir.mkdirs();
	}
	skins = new HashMap<String,PlayerSkin>();

	gson = new GsonBuilder().create();
	/*
	for (final File file : rootDir.listFiles(new FilenameFilter() {

	    @Override
	    public boolean accept(final File arg0, final String filename) {
		if (filename.endsWith(".json")) return true;

		return false;
	    }
	})) {
	    try {
		final PlayerSkin skin = gson.fromJson(new FileReader(file), PlayerSkin.class);
		getLogger().info("Loaded "+skin.allSkin.size()+" skins for uuid "+skin.uuid);
		skins.put(skin.uuid, skin);
	    } catch (final IOException e) { }
	}*/

	getCommand("bskin").setExecutor(new SkinSelectCommand());
	Bukkit.getPluginManager().registerEvents(this, this);

	Bukkit.getScheduler().runTaskTimer(this, new Runnable() {

	    @Override
	    public void run() {
		saveData();
	    }

	}, 5*60*20, 5*60*20);
    }
    @Override
    public void onDisable() {
	saveData();
	/*
	try {
	    final JsonArray array = new JsonArray();
	    for (final PlayerSkin skin : skins.values()) {
		array.add(skin.serialize());
	    }
	    final Gson gson = new GsonBuilder().setVersion(1.1).setPrettyPrinting().create();
	    final String json = gson.toJson(array);
	    final File file_config = new File("plugins" + File.separator + "BridgingSkin" + File.separator + "skins.json");
	    FileUtils.writeFile(file_config, json);
	} catch (final Exception e) {
	    e.printStackTrace();
	}*/
    }
    private void saveData() {
	rootDir = new File("plugins" + File.separator + "BridgingSkin"+File.separator+"skins");
	if (!rootDir.exists()) {
	    rootDir.mkdirs();
	}
	final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	for (final PlayerSkin skin : skins.values()) {
	    try {
		final String json = gson.toJson(skin);
		FileUtils.writeFile(new File(rootDir,skin.uuid+".json"), json);
	    } catch (final Exception e) {}
	}

    }
    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
	final ItemStack item = e.getItemDrop().getItemStack();
	if (!item.hasItemMeta()) return;
	if (!item.getItemMeta().hasLore()) return;
	if (!item.getItemMeta().getLore().contains("§6皮肤方块")) return;
	e.setCancelled(true);
    }
    @EventHandler
    public void onInventoryClick (final InventoryClickEvent e) {
	if (e.isCancelled()) return;
	if ((e.getInventory().getTitle()!=null) && e.getInventory().getTitle().contains("§6§l皮肤库存")) {
	    e.setCancelled(true);
	}
	if (e.getClickedInventory()==null) return;
	if ((e.getClickedInventory().getTitle()!=null) && e.getClickedInventory().getTitle().contains("§6§l皮肤库存")) {
	    e.setCancelled(true);
	    final ItemStack item = e.getCurrentItem();
	    if ((item==null)|| (item.getType()==Material.AIR)) return;
	    if (item.getType()==Material.BARRIER) return;
	    getSkin(e.getView().getPlayer().getUniqueId().toString()).currentSkin = new SkinSet(item.getType().name(),item.getData().getData());
	    e.getView().getPlayer().closeInventory();
	    e.getView().getPlayer().sendMessage("§6§l[BridgingAnalyzer] §a你的搭路皮肤已更换");
	}
    }
    @EventHandler
    public void onMove(final InventoryMoveItemEvent e) {
	if (e.isCancelled()) return;
	if (e.getSource().getTitle()==null) return;
	if (e.getSource().getTitle().contains("§6§l皮肤库存")) {
	    e.setCancelled(true);
	    return;
	}
	if (e.getDestination()==null) return;
	if (e.getDestination().getTitle().contains("§6§l皮肤库存")) {
	    e.setCancelled(true);
	    return;
	}
    }

    @EventHandler
    public void onSetSkin(final PlayerInteractEvent e) {
	if ((e.getPlayer().getItemInHand()==null) || (e.getPlayer().getItemInHand().getType()==Material.AIR))
	    return;
	else {
	    final ItemStack item = e.getPlayer().getItemInHand();
	    if (!item.hasItemMeta()) return;
	    if (!item.getItemMeta().hasLore()) return;
	    if (!item.getItemMeta().getLore().contains("§6皮肤方块")) return;
	    e.setCancelled(true);
	    BridgingSkin.getSkin(e.getPlayer().getUniqueId().toString()).allSkin.add(new SkinSet(item.getType().name(),item.getData().getData()));
	    e.getPlayer().setItemInHand(null);
	    e.getPlayer().sendMessage("§6§l[BridgingAnalyzer] §a此方块已添加到你的搭路皮肤库存! 输入/bskin切换皮肤");
	    return;
	}
    }
}