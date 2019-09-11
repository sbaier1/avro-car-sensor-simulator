package com.hivemq.util;


import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class IgnoreSchemaProperty
{
    @JsonIgnore
    abstract void getSchema();
    @JsonIgnore
    abstract org.apache.avro.specific.SpecificData getSpecificData();
}