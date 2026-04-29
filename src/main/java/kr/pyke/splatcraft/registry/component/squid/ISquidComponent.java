package kr.pyke.splatcraft.registry.component.squid;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface ISquidComponent extends ComponentV3, AutoSyncedComponent {
    boolean isSubmerged();
    void setSubmerged(boolean submerged);
}
