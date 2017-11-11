package com.techyugadi.reactive.rxmqtt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techyugadi.reactive.rxmqtt.ConfigurationException;
import com.techyugadi.reactive.rxmqtt.MQTTObservable;

import io.reactivex.Observable;
import io.reactivex.Flowable;
import io.reactivex.BackpressureStrategy;

import junit.framework.TestCase;
import io.moquette.BrokerConstants;
import io.moquette.server.Server;

public class TestMQTTObservable extends TestCase {
	
	private static Logger LOG = 
			LoggerFactory.getLogger(TestMQTTObservable.class);
	
	public void testObservable() throws IOException, InterruptedException, 
									MqttException, ConfigurationException {
		
		LOG.info("Unit Test MQTTObservable");
		
		Properties moqprops = new Properties();
		moqprops.put(BrokerConstants.PORT_PROPERTY_NAME, "1883");
		moqprops.put(BrokerConstants.HOST_PROPERTY_NAME, "0.0.0.0");
		moqprops.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, true);
		
		Server mqttBroker = new Server();
		mqttBroker.startServer(moqprops);
		
		Thread.sleep(3000);
		
		String broker = "tcp://0.0.0.0:1883";
		String clientId = "mqtt-test-client";
		
		String topic = "test-topic";
		String msg = "test message";
		int qos = 1;
		
		MqttClient mqttClient = new MqttClient(broker, clientId, 
												new MemoryPersistence());
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		
		mqttClient.connect(connOpts);
		
		Properties mqttProps = new Properties();
		mqttProps.setProperty("brokerURL", "tcp://localhost:1883");
		mqttProps.setProperty("mqttTopic", topic);
		
		MQTTObservable mqttObservable = new MQTTObservable(mqttProps);
		
		Observable<MqttMessage> observable = 
							mqttObservable.retrieveObservable();
		
		List<MqttMessage> observed = new ArrayList<MqttMessage>();
		observable.subscribe(observed::add);
		
		for (int i=0; i<6; i++) {
			MqttMessage message = new MqttMessage(msg.getBytes());
			message.setQos(qos);
			mqttClient.publish(topic, message);
		}
		
		mqttClient.disconnect();
		Thread.sleep(3000);
		mqttObservable.cleanup();
		mqttBroker.stopServer();
		
		assertEquals(observed.size(), 6);
		
	}

	public void testFlowable() throws IOException, InterruptedException, 
									MqttException, ConfigurationException {

		LOG.info("Unit Test MQTTFlowable");

		Properties moqprops = new Properties();
		moqprops.put(BrokerConstants.PORT_PROPERTY_NAME, "1883");
		moqprops.put(BrokerConstants.HOST_PROPERTY_NAME, "0.0.0.0");
		moqprops.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, true);

		Server mqttBroker = new Server();
		mqttBroker.startServer(moqprops);

		Thread.sleep(3000);

		String broker = "tcp://0.0.0.0:1883";
		String clientId = "mqtt-test-client";

		String topic = "test-topic";
		String msg = "test message";
		int qos = 1;

		MqttClient mqttClient = new MqttClient(broker, clientId, 
					new MemoryPersistence());
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);

		mqttClient.connect(connOpts);

		Properties mqttProps = new Properties();
		mqttProps.setProperty("brokerURL", "tcp://localhost:1883");
		mqttProps.setProperty("mqttTopic", topic);

		MQTTObservable mqttObservable = new MQTTObservable(mqttProps);

		Flowable<MqttMessage> flowable = 
				mqttObservable.retrieveFlowable(BackpressureStrategy.BUFFER);

		List<MqttMessage> observed = new ArrayList<MqttMessage>();
		flowable.subscribe(observed::add);

		for (int i=0; i<6; i++) {
			MqttMessage message = new MqttMessage(msg.getBytes());
			message.setQos(qos);
			mqttClient.publish(topic, message);
		}

		mqttClient.disconnect();
		Thread.sleep(3000);
		mqttObservable.cleanup();
		mqttBroker.stopServer();

		assertEquals(observed.size(), 6);

	}

}
