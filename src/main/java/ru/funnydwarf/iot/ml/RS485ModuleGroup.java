package ru.funnydwarf.iot.ml;

import gnu.io.CommPortIdentifier;
import lombok.Getter;

@Getter
public class RS485ModuleGroup extends ModuleGroup {

    private final CommPortIdentifier portIdentifier;
    public RS485ModuleGroup(CommPortIdentifier portIdentifier) {
        super("RS485", "Recommended Standard 485");
        this.portIdentifier = portIdentifier;//CommPortIdentifier.getPortIdentifier(port);
    }

    @Override
    protected InitializationState initialize() throws Exception {
        return InitializationState.OK;
    }
}
