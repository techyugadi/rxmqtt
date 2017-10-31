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
