package com.techyugadi.reactive.rxmqtt.sample;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.techyugadi.reactive.rxmqtt.MQTTObservable;
import io.reactivex.Observable;

import java.util.Properties;

/*
 * To run this program, it is recommended that you first set up
 * 'mosquitto' - a MQTT client server package on your machine.
 * On Ubuntu, it can be installed using the command:
 * sudo apt-get install mosquitto
 * sudo apt-get install mosquitto-clients
 * This starts a MQTT server on the machine on default port 1883
 * Then run this program from one console window.
 * From another console window run:
 * mosquitto_pub -t sensors/temperature -m 32 -q 1
 * To run the unit tests, you will have to stop the mosquitto server, to
 * avoid port conflict. This can be done by running: 
 * sudo service mosquitto stop
 * To start mosquitto server again, run:
 * sudo service mosquitto start 
 */

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
