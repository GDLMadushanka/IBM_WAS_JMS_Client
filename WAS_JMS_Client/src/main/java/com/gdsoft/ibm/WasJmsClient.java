package com.gdsoft.ibm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.*;

import com.ibm.websphere.sib.api.jms.*;

/**
 * This class is used to send and receive JMS messages to IBM WAS
 */
public class WasJmsClient {

    private static String QUEUE_NAME = "queue1";
    private static String BUS_NAME = "bus1";
    private static String JMS_ENDPOINT = "172.17.0.2:7276";


    public static void main(String[] args) throws JMSException, IOException {


        // Obtain the factory factory
        JmsFactoryFactory jmsFact = JmsFactoryFactory.getInstance();

        // Create a JMS destination
        Destination dest;

        // Create JMS queue
        JmsQueue queue = jmsFact.createQueue(QUEUE_NAME);
        dest = queue;

        // Create a unified JMS connection factory
        JmsConnectionFactory connFact = jmsFact.createConnectionFactory();

        // Configure the connection factory
        connFact.setBusName(BUS_NAME);
        connFact.setProviderEndpoints(JMS_ENDPOINT);

        // Create the connection
        Connection conn = connFact.createConnection();

        Session session = null;
        MessageProducer producer = null;
        MessageConsumer consumer = null;
        try {

            // Create a session
            session = conn.createSession(false, // Not transactional
                    Session.AUTO_ACKNOWLEDGE);

            // Create a message producer
            producer = session.createProducer(dest);
            consumer = session.createConsumer(dest);

            // Loop reading lines of text from the console to send
            System.out.println("Ready to send to " + dest + " on bus " + BUS_NAME);
            BufferedReader lineInput = new BufferedReader(new InputStreamReader(System.in));
            String line = lineInput.readLine();

            TextMessage message = session.createTextMessage();
            message.setText(line);

            // Send the message
            producer.send(message,
                    Message.DEFAULT_DELIVERY_MODE,
                    Message.DEFAULT_PRIORITY,
                    Message.DEFAULT_TIME_TO_LIVE);

            // should start the connection to receive messages
            conn.start();
            Message msg = consumer.receiveNoWait();
            System.out.println("Consumed a message from queue");
            if (msg != null) {
                msg.acknowledge();
                System.out.println(msg.toString());
            } else System.out.println("null msg");

        }
        // Finally block to ensure we close our JMS objects
        finally {

            // Close the message producer
            try {
                if (producer != null) producer.close();
                if (consumer != null) consumer.close();
            } catch (JMSException e) {
                System.err.println("Failed to close message producer: " + e);
            }

            // Close the session
            try {
                if (session != null) session.close();
            } catch (JMSException e) {
                System.err.println("Failed to close session: " + e);
            }

            // Close the connection
            try {
                conn.close();
            } catch (JMSException e) {
                System.err.println("Failed to close connection: " + e);
            }

        }
    }
}
