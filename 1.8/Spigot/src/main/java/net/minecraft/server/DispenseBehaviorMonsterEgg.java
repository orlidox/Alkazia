package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
// CraftBukkit end

final class DispenseBehaviorMonsterEgg extends DispenseBehaviorItem {

    DispenseBehaviorMonsterEgg() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumDirection enumdirection = BlockDispenser.b(isourceblock.f());
        double d0 = isourceblock.getX() + (double) enumdirection.getAdjacentX();
        double d1 = (double) ((float) isourceblock.getBlockPosition().getY() + 0.2F);
        double d2 = isourceblock.getZ() + (double) enumdirection.getAdjacentZ();
        // Entity entity = ItemMonsterEgg.a(isourceblock.i(), itemstack.getData(), d0, d1, d2);
        
        // CraftBukkit start
        World world = isourceblock.i();
        ItemStack itemstack1 = itemstack.a(1);
        org.bukkit.block.Block block = world.getWorld().getBlockAt(isourceblock.getBlockPosition().getX(), isourceblock.getBlockPosition().getY(), isourceblock.getBlockPosition().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d0, d1, d2));
        if (!BlockDispenser.eventFired) {
            world.getServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            itemstack.count++;
            return itemstack;
        }

        if (!event.getItem().equals(craftItem)) {
            itemstack.count++;
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            IDispenseBehavior idispensebehavior = (IDispenseBehavior) BlockDispenser.M.get(eventStack.getItem());
            if (idispensebehavior != IDispenseBehavior.a && idispensebehavior != this) {
                idispensebehavior.a(isourceblock, eventStack);
                return itemstack;
            }
        }

        itemstack1 = CraftItemStack.asNMSCopy(event.getItem());

        Entity entity = ItemMonsterEgg.spawnCreature(isourceblock.i(), itemstack.getData(), event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DISPENSE_EGG);

        if (entity instanceof EntityLiving && itemstack.hasName()) {
            ((EntityInsentient) entity).setCustomName(itemstack.getName());
        }

        // itemstack.a(1); // Handled during event processing
        // CraftBukkit end
        return itemstack;
    }
}
