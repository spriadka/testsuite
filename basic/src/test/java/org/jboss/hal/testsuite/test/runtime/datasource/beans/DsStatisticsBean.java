package org.jboss.hal.testsuite.test.runtime.datasource.beans;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.hal.testsuite.test.runtime.datasource.TestEntity;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DsStatisticsBean implements DsStatisticsRemote {

    @PersistenceContext(unitName = "testxapu")
    private EntityManager em;

    @Resource(name = "java:jboss/TransactionManager")
    private TransactionManager tm;

    @Resource
    private UserTransaction tx;

    @Override
    public void commit() throws Exception {
        tx.begin();
        em.persist(new TestEntity());
        tx.commit();
    }

    @Override
    public void rollback() throws Exception {
        tx.begin();
        em.persist(new TestEntity());
        tx.rollback();
}
}
