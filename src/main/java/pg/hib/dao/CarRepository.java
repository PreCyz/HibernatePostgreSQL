package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import pg.hib.entities.CarEntity;
import pg.hib.entities.LocalDateTimeConverter;
import pg.hib.providers.TemplateProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class CarRepository extends AbstractRepository<CarEntity> implements CarDao {

    public CarRepository(SessionFactory sessionFactory) {
        super(sessionFactory, CarEntity.class);
    }

    @Override
    public List<CarEntity> findAllByFirstRegistrationDateAfter(LocalDateTime localDateTime) {
            try (Session session = sessionFactory.openSession()) {
                return TemplateProvider.collectionTemplate(session, () -> {
                    final String hql = String.format(
                            "FROM %s t WHERE t.firstRegistrationDate >= :firstRegistrationDate ORDER BY t.id",
                            this.entityName
                    );
                    @SuppressWarnings("unchecked")
                    Query<List<CarEntity>> query = session.createQuery(hql);
                    query.setParameter("firstRegistrationDate", localDateTime);
                    return castCollection(query);
                });
            } catch (Exception ex) {
                logger.error("Something went wrong.", ex);
                throw new HibernateException(ex);
            }
    }

    protected CarEntity castObject(Object[] fields) {
        try {
            final Class<?>[] parameterTypes = Arrays.stream(CarEntity.class.getDeclaredFields())
                    .map(Field::getType)
                    .toArray(Class<?>[]::new);
            final Constructor<CarEntity> constructor = CarEntity.class.getConstructor(parameterTypes);
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] instanceof BigInteger) {
                    fields[i] = ((BigInteger)fields[i]).longValue();
                } else if (fields[i] instanceof String) {
                    fields[i] = new LocalDateTimeConverter().convertToEntityAttribute(String.valueOf(fields[i]));
                }
            }
            return constructor.newInstance(fields);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error("Problem with casting object to {}.", entityName, e);
        }
        return null;
    }
}
