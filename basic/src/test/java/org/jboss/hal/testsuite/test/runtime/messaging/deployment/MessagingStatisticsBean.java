package org.jboss.hal.testsuite.test.runtime.messaging.deployment;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class MessagingStatisticsBean {

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/jms/queue/test")
    private Queue queue;

    @Resource
    private UserTransaction tx;

    public void commit() {
        try {
            tx.begin();
            sendMessage();
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            tx.begin();
            sendMessage();
            tx.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage() throws JMSException {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession();
            MessageProducer producer = session.createProducer(queue);
            connection.start();
            producer.send(session.createTextMessage("Sample message."));
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
