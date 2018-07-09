package com.docker.storage.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaConsumerHandler {
    private KafkaConsumer<String,String> consumer;
    private KafkaConfCenter kafkaConfCenter;
    public KafkaConsumerHandler(KafkaConfCenter kafkaConfCenter){
        this.kafkaConfCenter = kafkaConfCenter;
    }

    public void send(String topic,String message){

    }

    public void connect(){
        Properties props = kafkaConfCenter.getConsumerConf();
        this.consumer = new KafkaConsumer<>(props);
    }
}
