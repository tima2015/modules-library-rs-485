package ru.funnydwarf.iot.ml.utils;

import gnu.io.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.funnydwarf.iot.ml.RS485DeviseAddress;
import ru.funnydwarf.iot.ml.RS485ModuleGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModbusWorker {

    private static final int open_port_timeout = 2000;
    private static final int read_timeout = 2000;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FunctionalCodes {
        public static final byte READ = 0x3;
        public static final byte WRITE = 0x6;
    }

    private static SerialPort begin(CommPortIdentifier portIdentifier, SerialPortParam param) {
        try {
            CommPort commPort = portIdentifier.open(ModbusWorker.class.getName(), open_port_timeout);
            if (commPort instanceof SerialPort port) {
                port.setSerialPortParams(param.bytePerSecond(), param.dataBits(), param.stopBits(), param.parity());
                return port;
            }
            commPort.close();
            throw new RuntimeException("Port = [%s] not a serial port!".formatted(portIdentifier.getName()));
        } catch (PortInUseException | UnsupportedCommOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void end(SerialPort port) {
        if (port != null) {
            port.close();
        }
    }

    public static int crcModbus(byte[] bytes) {
        int errorWord = 0xFFFF;
        int dataByteCount = bytes.length - 2;

        for (int i = 0; i < dataByteCount; i++) {
            errorWord ^= ((int) bytes[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((errorWord & 1) == 1) {
                    errorWord = (errorWord >> 1) ^ 0xA001;
                } else {
                    errorWord >>= 1;
                }
            }
        }
        return errorWord;
    }

    private static byte[] splitCRCModbusBytes(int errorWord) {
        return new byte[] {(byte) (errorWord & 0xFF), (byte) ((errorWord >> 8) & 0xFF)};
    }

    public static byte[] createModbusRequestByteArray(byte deviceAddress, byte functionCode, short registerAddress, short requestValue) {
        byte[] byteArray = new byte[] {
                deviceAddress,
                functionCode,
                (byte) ((registerAddress >> 8) & 0xFF),
                (byte) (registerAddress & 0xFF),
                (byte) ((requestValue >> 8) & 0xFF),
                (byte) (requestValue & 0xFF),
                0,
                0
        };
        byte[] crcModbusBytes = splitCRCModbusBytes(crcModbus(byteArray));
        byteArray[6] = crcModbusBytes[0];
        byteArray[7] = crcModbusBytes[1];

        return byteArray;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class ModbusResponse {
        private final byte slaveAddress;
        private final byte functionalCode;
        private final short[] values;
        private final boolean dataValid;
    }

    private static ModbusResponse parseResponse(byte[] responseByteArray) {
        byte numOfBytes = responseByteArray[2];
        short[] values = new short[numOfBytes/2];

        int valueStartIndex = 3;
        for (int i = 0; i < values.length; i++) {
            values[i] = (short) (responseByteArray[valueStartIndex + i*2] << 8);
            values[i] |= ((short)responseByteArray[valueStartIndex + i*2 + 1] & 0xFF);
        }
        byte[] bytes = splitCRCModbusBytes(crcModbus(responseByteArray));
        boolean dataValid = bytes[0] == responseByteArray[responseByteArray.length - 2] &&
                bytes[1] == responseByteArray[responseByteArray.length - 1];

        return new ModbusResponse(responseByteArray[0], responseByteArray[1], values, dataValid);
    }

    public static ModbusResponse doTransaction(CommPortIdentifier portIdentifier, SerialPortParam param, byte[] requestByteArray) {
        SerialPort port = null;
        byte[] responseByteArray;
        try {
            port = begin(portIdentifier, param);
            InputStream in = port.getInputStream();
            OutputStream out = port.getOutputStream();

            out.write(requestByteArray);
            out.flush();

            for (int i = 0; i < read_timeout; i++) {
                Thread.sleep(1);
                if (in.available() != 0){
                    break;
                }
            }
            if (in.available() == 0) {
                throw new RuntimeException("Sensor read timeout!");
            }

            int slaveAddress = in.read();
            int functionalCode = in.read();
            int dataByteCount = in.read();
            responseByteArray = new byte[dataByteCount + 5];

            responseByteArray[0] = (byte) slaveAddress;
            responseByteArray[1] = (byte) functionalCode;
            responseByteArray[2] = (byte) dataByteCount;

            for (int i = 3; i < responseByteArray.length; i++) {
                responseByteArray[i] = (byte) in.read();
            }


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            end(port);
        }
        return parseResponse(responseByteArray);
    }

}
