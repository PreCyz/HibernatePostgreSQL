package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pg.hib.entities.TestEntity;
import pg.hib.providers.TemplateProvider;

import java.util.List;

class TestBeanRepository extends AbstractRepository<TestEntity> implements TestEntityDao {

    public TestBeanRepository(SessionFactory sessionFactory) {
        super(sessionFactory, TestEntity.class);
    }

    @Override
    public List<TestEntity> findByActive(boolean active) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                final String hql = String.format("FROM %s t WHERE t.active = :active ORDER BY t.id", this.entityName);
                @SuppressWarnings("unchecked")
                Query<List<TestEntity>> query = session.createQuery(hql);
                query.setParameter("active", active);
                return castCollection(query);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

}
