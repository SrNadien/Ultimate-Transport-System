package nadiendev.ultimatetransport.cable;

import nadiendev.ultimatetransport.api.TransferType;
import nadiendev.ultimatetransport.filter.SideFilter;
import nadiendev.ultimatetransport.item.UpgradeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Everything one face of one cable remembers. */
public class SideConfig {

    private TransferType cargo = TransferType.ENERGY;
    private ConnectionMode mode = ConnectionMode.INSERT;
    private RedstoneMode redstone = RedstoneMode.IGNORED;
    private boolean retrieve = false;
    private boolean severed = false;
    private DistributionMode distribution = DistributionMode.ROUND_ROBIN;
    private int priority = 0;
    private ItemStack upgrade = ItemStack.EMPTY;
    private final SideFilter filter = new SideFilter();

    private int roundRobin = 0;

    public ConnectionMode mode() {
        return mode;
    }

    public void setMode(ConnectionMode mode) {
        this.mode = mode;
    }

    public TransferType cargo() {
        return cargo;
    }

    public void setCargo(TransferType cargo) {
        this.cargo = cargo;
    }

    public boolean severed() {
        return severed;
    }

    public void setSevered(boolean severed) {
        this.severed = severed;
    }

    public boolean retrieve() {
        return retrieve;
    }

    public void setRetrieve(boolean retrieve) {
        this.retrieve = retrieve;
    }

    public RedstoneMode redstone() {
        return redstone;
    }

    public void setRedstone(RedstoneMode redstone) {
        this.redstone = redstone;
    }

    public DistributionMode distribution() {
        return distribution;
    }

    public void setDistribution(DistributionMode distribution) {
        this.distribution = distribution;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ItemStack upgrade() {
        return upgrade;
    }

    public void setUpgrade(ItemStack upgrade) {
        this.upgrade = upgrade;
    }

    public SideFilter filter() {
        return filter;
    }

    public UpgradeTier tier() {
        return upgrade.getItem() instanceof UpgradeItem item ? item.tier() : UpgradeTier.NONE;
    }

    public boolean canFilter() {
        return tier().canFilter();
    }

    /** Advances and returns the round-robin cursor, kept per side so each extractor spreads on its own. */
    public int nextRoundRobin(int size) {
        if (size <= 0) {
            return 0;
        }
        roundRobin = (roundRobin + 1) % size;
        return roundRobin;
    }

    public int roundRobin(int size) {
        return size <= 0 ? 0 : Math.floorMod(roundRobin, size);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", mode.getSerializedName());
        tag.putBoolean("Retrieve", retrieve);
        tag.putBoolean("Severed", severed);
        tag.putString("Redstone", redstone.getSerializedName());
        tag.putString("Distribution", distribution.getSerializedName());
        tag.putInt("Priority", priority);
        tag.putInt("RoundRobin", roundRobin);
        if (!upgrade.isEmpty()) {
            tag.put("Upgrade", upgrade.save(registries));
        }
        tag.put("Filter", filter.save(registries));
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        mode = ConnectionMode.byName(tag.getString("Mode"));
        retrieve = tag.getBoolean("Retrieve");
        severed = tag.getBoolean("Severed");
        redstone = RedstoneMode.byName(tag.getString("Redstone"));
        distribution = DistributionMode.byName(tag.getString("Distribution"));
        priority = tag.getInt("Priority");
        roundRobin = tag.getInt("RoundRobin");
        upgrade = tag.contains("Upgrade")
                ? ItemStack.parse(registries, tag.getCompound("Upgrade")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        if (tag.contains("Filter")) {
            filter.load(tag.getCompound("Filter"), registries);
        }
    }
}
