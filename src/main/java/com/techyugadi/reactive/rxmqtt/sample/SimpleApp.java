package com.techyugadi.reactive.rxmqtt.sample;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.techyugadi.reactive.rxmqtt.MQTTObservable;
import io.reactivex.Observable;

import java.util.Properties;

public class SimpleApp {
	
	public static void main(String[] args) throws Exception {
		
		Properties mqttProps = new Properties();
		mqttProps.setProperty("brokerURL", "tcp://localhost:1883");
		mqttProps.setProperty("mqttTopic", "sensors/temperature");
		
		if (args.length > 0) {
			mqttProps.setProperty("maxMessages", args[0]);
		}
		
		MQTTObservable mqttObservable = new MQTTObservable(mqttProps);
		
		Observable<MqttMessage> observable = 
							mqttObservable.retrieveObservable();
		
		observable.subscribe(msg -> {System.out.println(
										"RECEIVED MESSAGE:" + 
										msg.toString());},
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
