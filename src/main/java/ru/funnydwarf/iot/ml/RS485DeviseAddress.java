package ru.funnydwarf.iot.ml;

import gnu.io.CommPortIdentifier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RS485DeviseAddress {

    private final CommPortIdentifier portIdentifier;
    private byte addressByte;
}
