package pg.hib.dao;

import org.hibernate.SessionFactory;
import pg.hib.entities.CarEntity;

public class CarRepository extends AbstractRepository<CarEntity> {

    public CarRepository(SessionFactory sessionFactory) {
        super(sessionFactory, CarEntity.class);
    }

    public CarRepository(SessionFactory sessionFactory, int batchSize) {
        super(sessionFactory, CarEntity.class, batchSize);
    }
}
