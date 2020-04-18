package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pg.hib.entities.CarEntity;
import pg.hib.providers.TemplateProvider;

import java.time.LocalDateTime;
import java.util.List;

class CarRepository extends AbstractRepository<CarEntity> implements CarDao {

    public CarRepository(SessionFactory sessionFactory) {
        super(sessionFactory, CarEntity.class);
    }

    @Override
    public List<CarEntity> findAllByFirstRegistrationDateAfter(LocalDateTime localDateTime) {
            try (Session session = sessionFactory.openSession()) {
                return TemplateProvider.collectionTemplate(session, () -> {
                    @SuppressWarnings("unchecked")
                    Query<List<CarEntity>> query = session.createQuery(
                            "FROM CarEntity t WHERE t.firstRegistrationDate >= :firstRegistrationDate ORDER BY t.id"
                    );
                    query.setParameter("firstRegistrationDate", localDateTime);
                    return castCollection(query);
                });
            } catch (Exception ex) {
                logger.error("Something went wrong.", ex);
                throw new HibernateException(ex);
            }
    }
}
