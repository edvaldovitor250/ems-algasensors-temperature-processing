package com.algaworks.algasensors.temperatureprocessing.api.controller;

import com.algaworks.algasensors.temperatureprocessing.api.model.TemperatureLogOutPut;
import com.algaworks.algasensors.temperatureprocessing.coomon.IdGenerator;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

import static com.algaworks.algasensors.temperatureprocessing.infrastructure.rabbitmq.RabbitMQConfig.FANOUT_EXCHANGE_NAME;

@RestController
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@Slf4j
@RequiredArgsConstructor
public class TemperatureProcessingController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public void data(@PathVariable TSID sensorId, @RequestBody String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        Double temperature;

        try {
            temperature = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Input is not a number");
        }

        TemperatureLogOutPut temperatureLog = TemperatureLogOutPut.builder()
                .id(IdGenerator.generateId())
                .sensorId(sensorId)
                .value(temperature)
                .registeredAt(OffsetDateTime.now())
                .build();

        log.info(temperatureLog.toString());

        String exchange = FANOUT_EXCHANGE_NAME;
        String routingKey = "";

        Object payload = temperatureLog;

        rabbitTemplate.convertAndSend(exchange,routingKey,payload);
    }
}
