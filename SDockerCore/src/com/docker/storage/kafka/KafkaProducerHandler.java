package com.docker.storage.kafka;

import chat.logs.LoggerEx;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import java.util.Properties;

public class KafkaProducerHandler {
       public static final String TAG = KafkaConsumerHandler.class.getSimpleName();
       private KafkaProducer<String,String> producer;
       private KafkaConfCenter kafkaConfCenter;
       public KafkaProducerHandler(KafkaConfCenter kafkaConfCenter){
              this.kafkaConfCenter = kafkaConfCenter;
       }

       public void send(String topic,String message){
              producer.send(new ProducerRecord<String,String>(topic, message));
       }

       public void connect(){
              Properties props = kafkaConfCenter.getProducerConf();
              this.producer = new KafkaProducer<>(props);
       }

       public void disconnect(){
           if(producer != null) {
               producer.close();
               LoggerEx.info(TAG, "producer is close");
           }
       }
}
