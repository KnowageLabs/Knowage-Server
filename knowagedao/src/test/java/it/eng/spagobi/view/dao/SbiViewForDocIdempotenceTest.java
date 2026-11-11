package it.eng.spagobi.view.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.view.metadata.SbiViewForDoc;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

public class SbiViewForDocIdempotenceTest {

    @Test
    public void repeatedCreationReturnsTheSameLinkWithoutASecondInsert() throws Exception {
        Fixture fixture = new Fixture();
        SbiViewForDoc first = fixture.dao.create(fixture.link());
        SbiViewForDoc second = fixture.dao.create(fixture.link());

        assertSame(first, second);
        assertEquals("link-id", second.getId());
        assertEquals(1, fixture.saved.size());
        assertEquals(2, fixture.commits);
        assertEquals(2, fixture.locks);
    }

    @Test
    public void rejectsUnownedFolderBeforeReadingOrSavingLinks() throws Exception {
        Fixture fixture = new Fixture();
        fixture.folderVisible = false;
        try {
            fixture.dao.create(fixture.link());
            fail("Expected an inaccessible folder to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(fixture.saved.isEmpty());
            assertEquals(1, fixture.rollbacks);
        }
    }

    private static class Fixture {
        private final SbiViewHierarchy folder = new SbiViewHierarchy();
        private final List<SbiViewForDoc> saved = new ArrayList<>();
        private final SbiViewForDocHIBDAOImpl dao;
        private boolean folderVisible = true;
        private boolean locked;
        private boolean active;
        private int locks;
        private int commits;
        private int rollbacks;

        private Fixture() throws Exception {
            setId(folder, "folder-id");
            Transaction transaction = (Transaction) Proxy.newProxyInstance(Transaction.class.getClassLoader(),
                    new Class<?>[] { Transaction.class }, (proxy, method, args) -> {
                        if ("commit".equals(method.getName())) {
                            commits++;
                            active = false;
                            return null;
                        }
                        if ("rollback".equals(method.getName())) {
                            rollbacks++;
                            active = false;
                            return null;
                        }
                        if ("isActive".equals(method.getName())) {
                            return active;
                        }
                        throw new AssertionError("Unexpected transaction call: " + method.getName());
                    });
            Filter filter = (Filter) Proxy.newProxyInstance(Filter.class.getClassLoader(),
                    new Class<?>[] { Filter.class }, (proxy, method, args) -> {
                        assertEquals("setParameter", method.getName());
                        assertEquals("alice", args[1]);
                        return proxy;
                    });
            Session session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(),
                    new Class<?>[] { Session.class }, (proxy, method, args) -> {
                        switch (method.getName()) {
                        case "beginTransaction":
                            active = true;
                            locked = false;
                            return transaction;
                        case "enableFilter":
                            return filter;
                        case "createCriteria":
                            return criteria(args[0] == SbiViewHierarchy.class);
                        case "save":
                            assertTrue("Folder must be locked before inserting a link", locked);
                            SbiViewForDoc link = (SbiViewForDoc) args[0];
                            setId(link, "link-id");
                            saved.add(link);
                            return link.getId();
                        case "isOpen":
                            return true;
                        case "close":
                            return null;
                        default:
                            throw new AssertionError("Unexpected session call: " + method.getName());
                        }
                    });
            dao = new SbiViewForDocHIBDAOImpl() {
                @Override
                public Session getSession() {
                    return session;
                }

                @Override
                protected SbiHibernateModel updateSbiCommonInfo4Insert(SbiHibernateModel model) {
                    return model;
                }
            };
            dao.setUserProfile(new UserProfile("alice", "tenant"));
        }

        private Criteria criteria(boolean parent) {
            return (Criteria) Proxy.newProxyInstance(Criteria.class.getClassLoader(),
                    new Class<?>[] { Criteria.class }, (proxy, method, args) -> {
                        switch (method.getName()) {
                        case "setLockMode":
                            assertTrue(parent);
                            assertEquals(LockMode.PESSIMISTIC_WRITE, args[0]);
                            locked = true;
                            locks++;
                            return proxy;
                        case "uniqueResult":
                            assertTrue(locked);
                            return folderVisible ? folder : null;
                        case "list":
                            assertTrue("Duplicate lookup must follow the folder lock", locked);
                            return new ArrayList<>(saved);
                        case "add":
                        case "addOrder":
                        case "setMaxResults":
                            return proxy;
                        default:
                            throw new AssertionError("Unexpected criteria call: " + method.getName());
                        }
                    });
        }

        private SbiViewForDoc link() {
            SbiViewForDoc link = new SbiViewForDoc();
            link.setBiObjId(12);
            link.setParent(folder);
            return link;
        }

        private static void setId(Object entity, String id) throws Exception {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        }
    }
}
