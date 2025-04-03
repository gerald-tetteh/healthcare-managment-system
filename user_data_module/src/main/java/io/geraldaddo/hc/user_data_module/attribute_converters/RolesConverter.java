package io.geraldaddo.hc.user_data_module.attribute_converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.geraldaddo.hc.user_data_module.enums.Role;
import io.geraldaddo.hc.user_data_module.exceptions.JsonConvertException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

@Converter
public class RolesConverter implements AttributeConverter<Set<Role>, String> {
    private final Logger logger = LogManager.getLogger(AvailabilityConverter.class);
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(Set<Role> roles) {
        try {
            return mapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert roles to json. Roles: " + roles, e);
            throw new JsonConvertException("Failed to convert Roles to json", e);
        }
    }

    @Override
    public Set<Role> convertToEntityAttribute(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Set<Role>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert json to Roles. json: " + json, e);
            throw new JsonConvertException("Failed to convert json to Roles", e);
        }
    }
}
