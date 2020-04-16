package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.Main;
import pg.hib.entities.TestBean;
import pg.hib.providers.TemplateProvider;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestBeanRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final SessionFactory sessionFactory;

    public TestBeanRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<TestBean> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.getCollectionTemplate(session, () -> {
                Query<List<TestBean>> query = session.createQuery("FROM TestBean t ORDER BY t.id");
                return castCollection(query);
            });
        } catch (Exception ex) {
            LOGGER.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public Optional<TestBean> findById(Serializable id) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.getTemplate(session, () -> session.get(TestBean.class, id, LockMode.READ), TestBean.class);
        } catch (Exception ex) {
            LOGGER.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public List<TestBean> findByIds(Set<Long> ids) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.getCollectionTemplate(session, () -> {
                Query<List<TestBean>> query = session.createQuery("FROM TestBean t WHERE t.id IN :ids");
                query.setParameter("ids", ids);
                return castCollection(query);
            });
        } catch (Exception ex) {
            LOGGER.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    private List<TestBean> castCollection(Query<List<TestBean>> query) {
        final List<TestBean> records = new LinkedList<>();
        for(final Object o : query.list()) {
            records.add((TestBean) o);
        }
        return records;
    }
}
