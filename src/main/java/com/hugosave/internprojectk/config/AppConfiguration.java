package com.hugosave.internprojectk.config;

import com.google.protobuf.util.JsonFormat;
import com.hugosave.intern.project.proto.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
public class AppConfiguration {

    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter(ProtoConverter protoConverter) {
        return protoConverter.getHttpConverter();
    }

    @Bean
    ProtoConverter getProtoConverter() {
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
            .add(UserOnboardDTO.getDescriptor())
            .add(UserAuthResponseDTO.getDescriptor())
            .add(UserAssetDTO.getDescriptor())
            .add(UserAssetAndBalanceDTO.getDescriptor())
            .add(UserTransactionRequestDTO.getDescriptor())
            .add(TransactionMessageDTO.getDescriptor())
            .add(TransactionHistoryDTO.getDescriptor())
            .add(AssetTransactionDTO.getDescriptor())
            .add(PriceDTO.getDescriptor())
            .build();


        return new ProtoConverter(typeRegistry);
    }

    @Bean
    public RestTemplate restTemplate(ProtoConverter protoConverter, RestTemplateBuilder builder){
        return builder.additionalMessageConverters(protoConverter.getHttpConverter()).build();
    }


}
