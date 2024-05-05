package me.defender.itemrotation.items;

import me.defender.itemrotation.ItemRotation;
import me.defender.itemrotation.api.RotationItem;
import me.defender.itemrotation.api.utils.ConfigUtils;
import me.defender.itemrotation.api.utils.xseries.XSound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IceBridgeRotationItem extends RotationItem {

    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.ICE);
    }

    @Override
    public String defaultName() {
        return "Ice Bridge";
    }

    @Override
    public List<String> defaultLore() {
        return Arrays.asList("&7Cost: &6%price% Gold", "", "&7Spawns a bridge of ice blocks,", "&7in front of you", "", "%BuyStatus%");
    }

    @Override
    public int getPrice() {
        // Set the price of the ice bridge item
        return 5;
    }

    @Override
    public Material getCurrency() {
        // Set the currency used to purchase the ice bridge item
        return Material.GOLD_INGOT;
    }

    @Override
    public boolean execute(Player player, Block block2) {
        ConfigUtils config = new ConfigUtils();
        int length = config.getInt("Items." + defaultName() + ".length");
        double width = (double) config.getInt("Items." + defaultName() + ".width") /2;
        int delay = config.getInt("Items." + defaultName() + ".delay-before-removal");
        List<Block> blocks = new ArrayList<>();
        // Get the player's current position and direction
        Location pos = player.getLocation();
        Vector direction = pos.getDirection();

        // Calculate the position of the first block in the ice path
        // based on the player's position and direction
        double x = pos.getX() + direction.getX();
        double y = pos.getY();
        double z = pos.getZ() + direction.getZ();
        World world = pos.getWorld();

        // Adjust the width of the bridge based on the player's direction
        if (direction.getX() != 0 && direction.getZ() != 0) {
            width *= Math.sqrt(2);
        }

        // Set the blocks in a straight line in front of the player
        for (int i = 0; i < length; i++) {
            double finalX = x;
            double finalZ = z;
            double finalWidth = width;
            Bukkit.getScheduler().runTaskLater(ItemRotation.getInstance(), () -> {
                for (int j = (int) -Math.round(finalWidth); j <= Math.round(finalWidth); j++) {
                    Vector perp = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                    double offsetX = j * perp.getX();
                    double offsetZ = j * perp.getZ();
                    Location location = new Location(world, Math.round(finalX + offsetX), y-1, Math.round(finalZ + offsetZ));
                    Block block = world.getBlockAt(location);
                    if (!block.getType().isSolid()) {
                        XSound.BLOCK_GLASS_BREAK.play(location, 1.0f, 0.5f);
                        world.playEffect(location.clone().add(0, 1.3, 0), Effect.HAPPY_VILLAGER, 0);
                        block.setType(Material.ICE);
                        blocks.add(block);
                    }
                    Bukkit.getScheduler().runTaskLater(ItemRotation.getInstance(), () -> {
                        if (blocks.contains(block)) {
                            World w = block.getWorld();
                            XSound.ENTITY_CHICKEN_EGG.play(block.getLocation(), 1.0f, 0.5f);
                            w.playEffect(block.getLocation().clone().add(0, 1.3, 0), Effect.CLOUD, 0);
                            block.setType(Material.AIR);
                            blocks.remove(block);
                        }
                    }, delay * 20L);
                }
            }, i * 2L);
            x += direction.getX();
            z += direction.getZ();
        }
        return true;
    }

    @Override
    public boolean isBlockRequired() {
        return false;
    }


}

