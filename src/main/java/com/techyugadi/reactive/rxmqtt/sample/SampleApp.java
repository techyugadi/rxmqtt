package com.techyugadi.reactive.rxmqtt.sample;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.techyugadi.reactive.rxmqtt.MQTTObservable;
import io.reactivex.Observable;

import java.util.Properties;

public class SampleApp {
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Operator Exmaple");
		
		Properties mqttProps = new Properties();
		mqttProps.setProperty("brokerURL", "tcp://localhost:1883");
		mqttProps.setProperty("mqttTopic", "sensors/temperature");
		Integer maxMessages = 24;
		mqttProps.setProperty("maxMessages", maxMessages.toString());
		
		MQTTObservable mqttObservable = new MQTTObservable(mqttProps);
		
		Observable<MqttMessage> observable = 
							mqttObservable.retrieveObservable();
		
		Observable<String> newObservable = observable
								   .map(m->m.toString())
								   .zipWith(
									    Observable.range(1, maxMessages),
										(str,seq) -> "Seq #" + seq + ":" + str
									);
					                         
		newObservable.subscribe(System.out::println,
							 	err -> {System.out.println(
									 "RECEIVED ERROR:" + 
										err.toString()); 
							 			err.printStackTrace();}
							);
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				System.out.println("Cleaning up MQTT Client");
				mqttObservable.cleanup();
			}
	    });
		
	}

}
