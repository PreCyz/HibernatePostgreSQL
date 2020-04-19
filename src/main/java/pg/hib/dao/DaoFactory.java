package pg.hib.dao;

import org.hibernate.SessionFactory;

public final class DaoFactory {

    private DaoFactory() { }

    public static TestEntityDao getTestBeanRepository(SessionFactory sessionFactory) {
        return new TestBeanRepository(sessionFactory);
    }

    public static CarDao getCarRepository(SessionFactory sessionFactory) {
        return new CarRepository(sessionFactory);
    }
}
