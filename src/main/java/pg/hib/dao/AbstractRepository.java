package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.providers.TemplateProvider;

import java.io.Serializable;
import java.util.*;

public abstract class AbstractRepository<EntityType extends Serializable> {

    private static final int DEFAULT_BATCH_SIZE = 20;

    protected final SessionFactory sessionFactory;
    protected final Logger logger;
    private final Class<EntityType> entityClazz;
    private final int batchSize;

    protected AbstractRepository(SessionFactory sessionFactory, Class<EntityType> entityClazz, int batchSize) {
        this.sessionFactory = sessionFactory;
        this.entityClazz = entityClazz;
        logger = LoggerFactory.getLogger(getClass());
        this.batchSize = batchSize;
    }

    protected AbstractRepository(SessionFactory sessionFactory, Class<EntityType> entityClazz) {
        this (sessionFactory, entityClazz, DEFAULT_BATCH_SIZE);
    }

    public List<EntityType> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                Query<List<EntityType>> query = session.createQuery("FROM TestBean t ORDER BY t.id");
                return castCollection(query);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public Optional<EntityType> findById(Serializable id) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.singleObjectTemplate(session, () -> session.get(entityClazz, id, LockMode.READ), entityClazz);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public List<EntityType> findByIds(Set<Serializable> ids) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                Query<List<EntityType>> query = session.createQuery(
                        String.format("FROM %s t WHERE t.id IN :ids", entityClazz.getSimpleName())
                );
                query.setParameter("ids", ids);
                return castCollection(query);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public Optional<EntityType> save(EntityType entity) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.singleObjectTemplate(session, () -> {
                Serializable id = session.save(entity);
                return session.get(entityClazz, id);
            }, entityClazz);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public List<EntityType> save(List<EntityType> entities) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                Set<Serializable> ids = new LinkedHashSet<>();
                for (int i = 0; i < entities.size(); i++) {
                    Serializable id = session.save(entities.get(i));
                    ids.add(id);
                    if (i % batchSize == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                return findByIds(ids);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    public boolean delete(EntityType entity) {
        try (Session session = sessionFactory.openSession()) {
            TemplateProvider.voidTemplate(session, () -> session.delete(entity));
            return true;
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    protected List<EntityType> castCollection(Query<List<EntityType>> query) {
        final List<EntityType> records = new LinkedList<>();
        for (final Object o : query.list()) {
            records.add(entityClazz.cast(o));
        }
        return records;
    }

}
