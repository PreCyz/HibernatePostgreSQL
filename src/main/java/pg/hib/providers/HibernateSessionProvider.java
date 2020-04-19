package pg.hib.providers;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;

import java.util.Properties;

public class HibernateSessionProvider {

    private static SessionFactory sessionFactory;

    private HibernateSessionProvider() { }

    private static class HibernateSessionProviderHolder {
        private static final HibernateSessionProvider INSTANCE = new HibernateSessionProvider();
    }

    public static HibernateSessionProvider getInstance() {
        return HibernateSessionProviderHolder.INSTANCE;
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Properties settings = new Properties();
            settings.put(Environment.DRIVER, "org.postgresql.Driver");
            settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/local-hib");
            settings.put(Environment.USER, "postgres");
            settings.put(Environment.PASS, "postgres");
            settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQL10Dialect");
            settings.put(Environment.SHOW_SQL, "true");
            settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            settings.put(Environment.HBM2DDL_AUTO, "update");

            Configuration cfg = new Configuration()
                    .setProperties(settings)
                    .addAnnotatedClass(TestEntity.class)
                    .addAnnotatedClass(CarEntity.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(cfg.getProperties())
                    .build();

            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        }
        return sessionFactory;
    }
}
