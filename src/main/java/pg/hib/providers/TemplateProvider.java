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

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProvider.class);

    private TemplateProvider() {}

    public static <T> Optional<T> singleObjectTemplate(Session openedSession, Callable<? extends T> operation, Class<T> clazz) {
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
                String errMsg = String.format("Result object is not type of %s", clazz.getSimpleName());
                LOGGER.error(errMsg);
                throw new ClassCastException(errMsg);
            }
        }
        return Optional.empty();
    }

    public static <T, C extends Collection<T>> C collectionTemplate(Session openedSession, Callable<C> operation) {
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
