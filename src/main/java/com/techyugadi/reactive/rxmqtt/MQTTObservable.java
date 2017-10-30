package com.techyugadi.reactive.rxmqtt;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class MQTTObservable {
	
	private static Logger LOG = 
					LoggerFactory.getLogger(MQTTObservable.class);
	
	private MqttClient client;
	private	String brokerURL;
	private String clientId;
	private String topic;
	private MqttClientPersistence persistence;
	private long maxMessages = Long.MAX_VALUE;
	private long receivedMessages = 0;
	
	public MQTTObservable(Properties mqttProperties) 
			throws ConfigurationException, MqttException {
		
		brokerURL = mqttProperties.getProperty("brokerURL");
		clientId = mqttProperties.getProperty("clientId");
		topic = mqttProperties.getProperty("mqttTopic");
		
		if (mqttProperties.getProperty("maxMessages") != null) {
			try {
				maxMessages = Long.parseLong(
								mqttProperties.getProperty("maxMessages"));
				if (maxMessages <= 0)
					maxMessages = Long.MAX_VALUE;
			
			} catch (NumberFormatException nfe) {
				throw new ConfigurationException(
						"maxMessages must be of type Long");
			}
		}
		
		if (brokerURL == null)
			throw new ConfigurationException("MQTT Broker URL not specified");
		if (topic == null)
			throw new ConfigurationException("MQTT Topic not specified");
		if (clientId == null)
			clientId = "mqttclient";
		
	}
	
	public MQTTObservable(Properties mqttProperties, 
								MqttClientPersistence persistence)
							throws ConfigurationException, MqttException {
		
		this(mqttProperties);
		this.persistence = persistence;
		
	}
	
	public Observable<MqttMessage> retrieveObservable() {
		
		Observable<MqttMessage> observable = 
			Observable.create(emitter -> {
						
				MqttCallback callback = new MqttCallback() {
							
					@Override
					public void connectionLost(Throwable e) {
						LOG.error("Lost Connection to MQTT Broker ");
						emitter.onError(e);
					}

					@Override
					public void messageArrived(String topic, 
							MqttMessage event) {
								
						LOG.debug("MQTT Message arrived from " + 
									topic + " message = " + event.toString());
						if (maxMessages != Long.MAX_VALUE)
							receivedMessages ++;
						
						if (receivedMessages <= maxMessages)
							emitter.onNext(event); 
						else {
							emitter.onComplete();
						}
							
					}

					@Override
					public void deliveryComplete(
									IMqttDeliveryToken token) {
						
						try {
							LOG.debug(
								"MQTT Message Delivery Complete for message: "
					    		 + token.getMessage().toString());
						} catch (MqttException me) {
							LOG.error(
								"MqttError while processing deliveryComplete:" 
										+ me.getMessage());
						}

					}
							
				};
						
				client = new MqttClient(brokerURL, clientId);
				client.connect();
				LOG.debug("Client: " + clientId + " "
						+ "connected to MQTT Broker: " + brokerURL);
				client.subscribe(topic);
				LOG.debug("Client: " + clientId + " "
						+ "subscribed to MQTT Topic: " + topic);
			    client.setCallback(callback);
			    LOG.debug("MQTT callback set");
				        
			});
		
		return observable;
		
	}
	
	public Flowable<MqttMessage> retrieveFlowable(
									BackpressureStrategy strategy) {
		
		return retrieveObservable().toFlowable(strategy);
		
	}
	
	public void cleanup() {
		
		try {
			
			client.disconnect();
			LOG.debug("MQTT Client disconnected: " + client.getClientId());
			client.close();
			LOG.debug("MQTT Client closed: " + client.getClientId());
			
		} catch (MqttException e) {
			LOG.error("MqttException while cleaning up client resources: " +
						e.getMessage());
		}
        
	}

}