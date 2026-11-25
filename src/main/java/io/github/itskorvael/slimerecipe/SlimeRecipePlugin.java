package io.github.itskorvael.slimerecipe;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Slimes no longer spawn on their own. Slime balls are instead crafted as "Alginate Gel":
 * dried kelp (alginate) + honey bottle (sticky base) + bone meal (calcium cross-linker) + lime dye (colour).
 */
public final class SlimeRecipePlugin extends JavaPlugin implements Listener {

    private NamespacedKey recipeKey;
    private final Set<SpawnReason> blockedReasons = EnumSet.noneOf(SpawnReason.class);
    private boolean unlockOnJoin;

    @Override
    public void onEnable() {
        recipeKey = new NamespacedKey(this, "alginate_gel");
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        applyConfig();
    }

    @Override
    public void onDisable() {
        Bukkit.removeRecipe(recipeKey);
    }

    private void applyConfig() {
        reloadConfig();

        blockedReasons.clear();
        for (String name : getConfig().getStringList("spawns.blocked-reasons")) {
            try {
                blockedReasons.add(SpawnReason.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Unknown spawn reason in spawns.blocked-reasons: " + name);
            }
        }

        unlockOnJoin = "join".equalsIgnoreCase(getConfig().getString("recipe.unlock", "ingredient"));

        Bukkit.removeRecipe(recipeKey);
        if (!getConfig().getBoolean("recipe.enabled", true)) {
            getLogger().info("Slime recipe disabled in config.");
            return;
        }

        int kelp = getConfig().getInt("recipe.dried-kelp", 2);
        int yield = getConfig().getInt("recipe.yield", 2);
        // A shapeless recipe holds at most 9 ingredients; 3 are fixed (honey, bone meal, dye).
        if (kelp < 1 || kelp > 6) {
            getLogger().warning("recipe.dried-kelp must be 1-6, using 2.");
            kelp = 2;
        }
        if (yield < 1 || yield > 64) {
            getLogger().warning("recipe.yield must be 1-64, using 2.");
            yield = 2;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, new ItemStack(Material.SLIME_BALL, yield));
        recipe.addIngredient(kelp, Material.DRIED_KELP);
        recipe.addIngredient(Material.HONEY_BOTTLE);
        recipe.addIngredient(Material.BONE_MEAL);
        recipe.addIngredient(Material.LIME_DYE);
        recipe.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        Bukkit.addRecipe(recipe);
        getLogger().info("Slime recipe registered: " + kelp + " dried kelp + honey bottle + bone meal + lime dye -> "
                + yield + " slime balls.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.SLIME && blockedReasons.contains(event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (unlockOnJoin) {
            player.discoverRecipe(recipeKey);
            return;
        }
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && isIngredientHint(stack.getType())) {
                player.discoverRecipe(recipeKey);
                return;
            }
        }
    }

    @EventHandler
    public void onSlotChange(PlayerInventorySlotChangeEvent event) {
        if (!unlockOnJoin && isIngredientHint(event.getNewItemStack().getType())) {
            event.getPlayer().discoverRecipe(recipeKey);
        }
    }

    private static boolean isIngredientHint(Material type) {
        return type == Material.HONEY_BOTTLE || type == Material.DRIED_KELP;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            applyConfig();
            sender.sendMessage("SlimeRecipe config reloaded.");
            return true;
        }
        sender.sendMessage("Usage: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase(java.util.Locale.ROOT))) {
            out.add("reload");
        }
        return out;
    }
}
