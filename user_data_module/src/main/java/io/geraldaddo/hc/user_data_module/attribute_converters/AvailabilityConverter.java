package io.geraldaddo.hc.user_data_module.attribute_converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.geraldaddo.hc.user_data_module.entities.Availability;
import io.geraldaddo.hc.user_data_module.exceptions.JsonConvertException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Converter
public class AvailabilityConverter implements AttributeConverter<List<Availability>, String> {
    private final Logger logger = LogManager.getLogger(AvailabilityConverter.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<Availability> availability) {
        try {
            return mapper.writeValueAsString(availability);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert availability to json. Availability: " + availability, e);
            throw new JsonConvertException("Failed to convert Availability to json", e);
        }
    }

    @Override
    public List<Availability> convertToEntityAttribute(String s) {
        try {
            return mapper.readValue(s, new TypeReference<List<Availability>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert json to Availability. json: " + s, e);
            throw new JsonConvertException("Failed to convert json to Availability", e);
        }
    }
}
