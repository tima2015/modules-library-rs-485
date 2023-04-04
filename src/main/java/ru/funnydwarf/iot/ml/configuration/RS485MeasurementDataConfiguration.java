package ru.funnydwarf.iot.ml.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.funnydwarf.iot.ml.sensor.MeasurementDescription;
import ru.funnydwarf.iot.ml.sensor.MeasurementDescriptionRepository;

@Configuration
public class RS485MeasurementDataConfiguration {

    @Bean("CWTSoilSensor5PINProbeHumidityMeasurementDescription")
    @Lazy
    @Autowired
    public MeasurementDescription getCWTSoilSensor5PINProbeHumidityMeasurementDescription(MeasurementDescriptionRepository mdr) {
        return MeasurementDescriptionRepository.findOrCreate(mdr, "%", "Soil Humidity", "CWTSoilSensor5PINProbe soil humidity");
    }

    @Bean("CWTSoilSensor5PINProbeTemperatureMeasurementDescription")
    @Lazy
    @Autowired
    public MeasurementDescription getCWTSoilSensor5PINProbeTemperatureMeasurementDescription(MeasurementDescriptionRepository mdr) {
        return MeasurementDescriptionRepository.findOrCreate(mdr, "°C", "Temperature", "CWTSoilSensor5PINProbe soil temperature");
    }

    @Bean("CWTSoilSensor5PINProbeConductivityMeasurementDescription")
    @Lazy
    @Autowired
    public MeasurementDescription getCWTSoilSensor5PINProbeConductivityMeasurementDescription(MeasurementDescriptionRepository mdr) {
        return MeasurementDescriptionRepository.findOrCreate(mdr, "us/cm", "Conductivity", "CWTSoilSensor5PINProbe soil conductivity");
    }

    @Bean("CWTSoilSensor5PINProbePhMeasurementDescription")
    @Lazy
    @Autowired
    public MeasurementDescription getCWTSoilSensor5PINProbePhMeasurementDescription(MeasurementDescriptionRepository mdr) {
        return MeasurementDescriptionRepository.findOrCreate(mdr, "ph", "Acidity", "CWTSoilSensor5PINProbe soil acidity");
    }

    @Bean("CWTSoilSensor5PINProbeMeasurementDescription")
    @Lazy
    @Autowired
    public MeasurementDescription[] getTSL2561MeasurementDescription(@Qualifier("CWTSoilSensor5PINProbeHumidityMeasurementDescription") MeasurementDescription humidity,
                                                                     @Qualifier("CWTSoilSensor5PINProbeTemperatureMeasurementDescription") MeasurementDescription temperature,
                                                                     @Qualifier("CWTSoilSensor5PINProbeConductivityMeasurementDescription") MeasurementDescription conductivity,
                                                                     @Qualifier("CWTSoilSensor5PINProbePhMeasurementDescription") MeasurementDescription ph) {
        return new MeasurementDescription[]{ humidity, temperature, conductivity, ph };
    }
}
