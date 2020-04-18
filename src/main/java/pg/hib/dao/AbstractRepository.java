package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.providers.TemplateProvider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.*;

abstract class AbstractRepository<EntityType extends Serializable> implements BasicCRUD<EntityType> {

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
        this(sessionFactory, entityClazz, DEFAULT_BATCH_SIZE);
    }

    @Override
    public List<EntityType> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                @SuppressWarnings("unchecked")
                Query<List<EntityType>> query = session.createQuery(
                        String.format("FROM %s t ORDER BY t.id", entityClazz.getSimpleName()));
                return castCollection(query);
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    @Override
    public Optional<EntityType> findById(Serializable id) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.singleObjectTemplate(session, () -> session.get(entityClazz, id, LockMode.READ), entityClazz);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    @Override
    public List<EntityType> findByIds(Collection<Serializable> ids) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                @SuppressWarnings("unchecked")
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

    protected List<EntityType> castCollection(Query<List<EntityType>> query) {
        final List<EntityType> records = new LinkedList<>();
        for (final Object o : query.list()) {
            records.add(entityClazz.cast(o));
        }
        return records;
    }

    @Override
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

    @Override
    public List<EntityType> saveAll(List<EntityType> entities) {
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

    @Override
    public boolean delete(EntityType entity) {
        try (Session session = sessionFactory.openSession()) {
            TemplateProvider.voidTemplate(session, () -> session.delete(entity));
            return true;
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    @Override
    public boolean deleteByIds(Collection<Serializable> entitieIds) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Boolean> deleted = TemplateProvider.singleObjectTemplate(session, () -> {
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaDelete<EntityType> delete = cb.createCriteriaDelete(entityClazz);
                Root<EntityType> from = delete.from(entityClazz);
                delete.where(cb.in(from.get("id")).value(entitieIds));
                int result = session.createQuery(delete).executeUpdate();
                return result == entitieIds.size();
            }, Boolean.class);
            return deleted.orElse(false);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    @Override
    public boolean deleteAll(Collection<EntityType> entities) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Boolean> deleted = TemplateProvider.singleObjectTemplate(session, () -> {
                int i = 0;
                for (EntityType entity : entities) {
                    session.delete(entity);
                    i++;
                    if (i % batchSize == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                return true;
            }, Boolean.class);
            return deleted.orElse(false);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

}
