package ru.funnydwarf.iot.ml.sensor.reader;

import gnu.io.SerialPort;
import ru.funnydwarf.iot.ml.RS485DeviseAddress;
import ru.funnydwarf.iot.ml.utils.ModbusWorker;
import ru.funnydwarf.iot.ml.utils.SerialPortParam;

public class CWTSoilSensor5PINProbeReader implements Reader<RS485DeviseAddress> {

    private static final short registerAddress = 0x0000;
    private static final short numberOfRegisterToRead = 0x0004;
    private static final int numberOfAttempts = 5;
    private static final SerialPortParam sensorSerialPortParam = new SerialPortParam(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);// TODO: 04.04.2023 Зависит от конфигурации сенсора


    // TODO: 04.04.2023 Сделать более гибкую работу с модулем, поскольку у него могут быть различные конфигурации
    private ModbusWorker.ModbusResponse readSensorRegisters(RS485DeviseAddress address) throws RuntimeException {
        byte[] requestByteArray = ModbusWorker.createModbusRequestByteArray(address.getAddressByte(), ModbusWorker.FunctionalCodes.READ,
                registerAddress, numberOfRegisterToRead);
        for (int i = 0; i < numberOfAttempts; i++) {
            ModbusWorker.ModbusResponse response = ModbusWorker.doTransaction(address.getPortIdentifier(), sensorSerialPortParam, requestByteArray);
            if (response.isDataValid()){
                return response;
            }
        }
        throw new RuntimeException("Can't read valid data in %d attempts!".formatted(numberOfAttempts));
    }

    @Override
    public double[] read(RS485DeviseAddress address, Object... args) {
        short[] values = readSensorRegisters(address).getValues();
        double humidity = values[0] * 0.1;
        double temperature = values[1] * 0.1;
        double conductivity = values[2];
        double ph = values[3] * 0.1;
        return new double[] { humidity, temperature, conductivity, ph };
    }
}
