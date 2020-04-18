package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pg.hib.entities.TestBean;
import pg.hib.providers.TemplateProvider;

import java.util.List;

class TestBeanRepository extends AbstractRepository<TestBean> implements TestBeanDao {

    public TestBeanRepository(SessionFactory sessionFactory) {
        super(sessionFactory, TestBean.class);
    }

    @Override
    public List<TestBean> findByActive(boolean active) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                Query<List<TestBean>> query = session.createQuery("FROM TestBean t WHERE t.active = :active ORDER BY t.id");
                query.setParameter("active", active);
                return castCollection(query);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

}
