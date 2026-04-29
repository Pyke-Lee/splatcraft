package kr.pyke.splatcraft.registry.component.squid;

import kr.pyke.splatcraft.registry.component.ModComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SquidComponent implements ISquidComponent {
    private final Player owner;

    private boolean submerged = false;

    public SquidComponent(Player owner) {
        this.owner = owner;
    }

    @Override
    public void readData(ValueInput input) {
        this.submerged = input.getBooleanOr("submerged", false);
    }

    @Override
    public void writeData(ValueOutput output) {
        output.putBoolean("submerged", this.submerged);
    }

    @Override public boolean isSubmerged() { return submerged; }

    @Override
    public void setSubmerged(boolean submerged) {
        if (this.submerged == submerged) { return; }

        this.submerged = submerged;
        ModComponents.SQUID.sync(owner);
    }
}
