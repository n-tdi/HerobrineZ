package world.ntdi.herobrinez;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

public final class HerobrineZ extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Long cooldown = 30 * 60 * 20L;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                }
                World w = p.getWorld();
                int x = (int) (p.getLocation().getX() + 100);
                int z = (int) p.getLocation().getZ();
                int y = getHighestY(w, x, z);
                Location loc = new Location(w, x, y, z);
                Zombie zomb = (Zombie) w.spawnEntity(loc, EntityType.ZOMBIE);
                zomb.setCustomName("Herobrine");
                zomb.setCustomNameVisible(true);
                zomb.setBaby(false);

                zomb.getEquipment().setHelmet(getHead("gagaluc"));
            }
        }.runTaskTimer(this, cooldown, cooldown);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        p.getWorld().getNearbyEntities(p.getLocation(), 50, 0, 50, entity -> entity instanceof Zombie && entity.getCustomName().equals("Herobrine")).forEach(Entity::remove);
    }

    public int getHighestY(World world, int x, int z) {
        int y = 255;
        while(world.getBlockAt(x, y, z).getType() != Material.AIR) { y--; }
        return y;
    }

    static String getHeadValue(String name){
        try {
            String result = getURLContent("https://api.mojang.com/users/profiles/minecraft/" + name);
            Gson g = new Gson();
            JsonObject obj = g.fromJson(result, JsonObject.class);
            String uid = obj.get("id").toString().replace("\"","");
            String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);
            obj = g.fromJson(signature, JsonObject.class);
            String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            String decoded = new String(Base64.getDecoder().decode(value));
            obj = g.fromJson(decoded, JsonObject.class);
            String skinURL = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();
            return new String(Base64.getEncoder().encode(skinByte));
        } catch (Exception ignored){ }
        return null;
    }
    private static String getURLContent(String urlStr) {
        URL url;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try{
            url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8) );
            String str;
            while((str = in.readLine()) != null) {
                sb.append( str );
            }
        } catch (Exception ignored) { }
        finally{
            try{
                if(in!=null) {
                    in.close();
                }
            }catch(IOException ignored) { }
        }
        return sb.toString();
    }
    public ItemStack getHead(String p){
        AtomicReference<ItemStack> i = null;
        Bukkit.getScheduler().runTaskAsynchronously(this, (Runnable) () -> {
            String value;
            value = getHeadValue(p);
            if (value == null){
                value = "";
            }
            ItemStack item = getHead(value);

            i.set(item);
        });
        return i.get();
    }
}
