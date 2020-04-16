package pg.hib.providers;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.Main;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class TemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private TemplateProvider() {}

    public static <T> Optional<T> getTemplate(Session openedSession, Callable<? extends T> operation, Class<T> clazz) {
        Object tmpResult;
        Transaction transaction = openedSession.beginTransaction();
        try {
            tmpResult = operation.call();
        } catch (Exception ex) {
            LOGGER.error("Something went wrong", ex);
            throw new HibernateException(ex);
        } finally {
            transaction.commit();
        }
        if (tmpResult != null) {
            if (clazz.isInstance(tmpResult)) {
                return Optional.of(clazz.cast(tmpResult));
            } else {
                LOGGER.error("Result object is not type of {}", clazz.getCanonicalName());
                throw new ClassCastException("Result object is not type of " + clazz.getCanonicalName());
            }
        }
        return Optional.empty();
    }

    public static <T, C extends Collection<T>> C getCollectionTemplate(Session openedSession, Callable<C> operation) {
        Transaction transaction = openedSession.beginTransaction();
        try {
            return operation.call();
        } catch (Exception ex) {
            LOGGER.error("Something went wrong", ex);
            throw new HibernateException(ex);
        } finally {
            transaction.commit();
        }
    }

    public static void voidTemplate(Session openedSession, Runnable operation) {
        Transaction transaction = openedSession.beginTransaction();
        try {
            operation.run();
        } catch (Exception ex) {
            LOGGER.error("Something went wrong", ex);
            throw new HibernateException(ex.getMessage());
        } finally {
            transaction.commit();
        }
    }
}
