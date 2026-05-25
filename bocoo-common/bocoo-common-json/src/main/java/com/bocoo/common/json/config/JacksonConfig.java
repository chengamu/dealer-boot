package com.bocoo.common.json.config;

import com.bocoo.common.core.utils.TimeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.bocoo.common.json.handler.BigNumberSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * jackson 配置
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 全局配置序列化返回 JSON 处理
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
            javaTimeModule.addSerializer(LocalDateTime.class, UtcLocalDateTimeSerializer.INSTANCE);
            javaTimeModule.addDeserializer(LocalDateTime.class, UtcLocalDateTimeDeserializer.INSTANCE);
            builder.modules(javaTimeModule);
            builder.timeZone(TimeZone.getTimeZone("UTC"));
            log.info("初始化 jackson 配置");
        };
    }

    private static final class UtcLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        private static final UtcLocalDateTimeSerializer INSTANCE = new UtcLocalDateTimeSerializer();

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeString(TimeUtils.formatUtcIso(value));
        }
    }

    private static final class UtcLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final UtcLocalDateTimeDeserializer INSTANCE = new UtcLocalDateTimeDeserializer();

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return TimeUtils.parseUtcIso(p.getValueAsString());
        }
    }

}
