package com.hugosave.internprojectk.config;

import com.google.protobuf.util.JsonFormat;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

public class ProtoConverter {
    private final JsonFormat.TypeRegistry typeRegistry;
    private final JsonFormat.Parser parser;
    private final JsonFormat.Printer printer;
    private final ProtobufJsonFormatHttpMessageConverter httpConverter;

    public ProtoConverter(JsonFormat.TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;

        this.parser = JsonFormat.parser()
            .usingTypeRegistry(typeRegistry)
            .ignoringUnknownFields();

        this.printer = JsonFormat.printer()
            .usingTypeRegistry(typeRegistry)
            .preservingProtoFieldNames()
            .includingDefaultValueFields()
            .omittingInsignificantWhitespace();

        this.httpConverter = new ProtobufJsonFormatHttpMessageConverter(parser, this.printer);
    }

    public JsonFormat.Parser getParser() {
        return this.parser;
    }

    public JsonFormat.Printer getPrinter() {
        return this.printer;
    }

    public ProtobufJsonFormatHttpMessageConverter getHttpConverter() {
        return this.httpConverter;
    }

    public JsonFormat.TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
