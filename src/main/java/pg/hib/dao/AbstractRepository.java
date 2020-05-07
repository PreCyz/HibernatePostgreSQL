package pg.hib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.providers.TemplateProvider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static java.util.stream.Collectors.toList;

abstract class AbstractRepository<EntityType extends Serializable> implements BasicCRUD<EntityType> {

    private static final int DEFAULT_BATCH_SIZE = 20;

    protected final SessionFactory sessionFactory;
    protected final Logger logger;
    protected final String entityName;
    private final Class<EntityType> entityClazz;
    private final int batchSize;

    protected AbstractRepository(SessionFactory sessionFactory, Class<EntityType> entityClazz, int batchSize) {
        this.sessionFactory = sessionFactory;
        this.entityClazz = entityClazz;
        this.entityName = entityClazz.getSimpleName();
        this.batchSize = batchSize;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected AbstractRepository(SessionFactory sessionFactory, Class<EntityType> entityClazz) {
        this(sessionFactory, entityClazz, DEFAULT_BATCH_SIZE);
    }

    @Override
    public List<EntityType> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                final LinkedList<String> idNames = new LinkedList<>(getIdNameAndType().keySet());
                @SuppressWarnings("unchecked")
                Query<List<EntityType>> query = session.createQuery(
                        String.format("FROM %s t ORDER BY t.%s", entityClazz.getSimpleName(), idNames.getFirst()));
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
            return TemplateProvider.singleObjectTemplate(session, () -> session.get(entityClazz, id), entityClazz);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    @Override
    public List<EntityType> findByIds(Collection<Serializable> ids) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> getEntitiesByIds(ids, session));
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    private List<EntityType> getEntitiesByIds(Collection<Serializable> ids, Session session) {
        LinkedList<String> idNames = new LinkedList<>(getIdNameAndType().keySet());
        @SuppressWarnings("unchecked")
        Query<List<EntityType>> query = session.createQuery(
                String.format("FROM %s t WHERE t.%s IN :ids", entityClazz.getSimpleName(), idNames.getFirst())
        );
        query.setParameter("ids", ids);
        return castCollection(query);
    }

    private LinkedHashMap<String, Class<?>> getIdNameAndType() {
        LinkedHashMap<String, Class<?>> result = new LinkedHashMap<>(1);
        for (Field field : entityClazz.getDeclaredFields()) {
            for (Annotation declaredAnnotation : field.getDeclaredAnnotations()) {
                if (declaredAnnotation.annotationType() == javax.persistence.Id.class) {
                    result.put(field.getName(), field.getType());
                }
            }
        }
        return result;
    }

    protected List<EntityType> castCollection(Query<List<EntityType>> query) {
        final List<EntityType> records = new LinkedList<>();
        for (final Object o : query.list()) {
            records.add(entityClazz.cast(o));
        }
        return records;
    }
    
    protected List<EntityType> castCollection(Collection<Serializable> entities) {
        final List<EntityType> records = new LinkedList<>();
        for (final Serializable o : entities) {
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

    public List<EntityType> saveAll(List<EntityType> entities) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                Set<Serializable> ids = new LinkedHashSet<>();
                boolean batchExecuted = false;
                for (int i = 0; i < entities.size(); i++) {
                    Serializable id = session.save(entities.get(i));
                    ids.add(id);
                    batchExecuted = false;
                    if (i > 0 && i % batchSize == 0) {
                        session.flush();
                        session.clear();
                        batchExecuted = true;
                    }
                }
                if (!batchExecuted) {
                    session.flush();
                    session.clear();
                }

                if (ids.stream().allMatch(item -> item.getClass() == entityClazz)) {
                    return castCollection(ids);
                } else {
                    LinkedList<String> idNames = new LinkedList<>(getIdNameAndType().keySet());
                    @SuppressWarnings("unchecked")
                    Query<List<EntityType>> query = session.createQuery(
                            String.format("FROM %s t WHERE t.%s IN :ids", entityClazz.getSimpleName(), idNames.getFirst())
                    );
                    query.setParameter("ids", ids);
                    return castCollection(query);
                }
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
    public boolean deleteByIds(Collection<Serializable> entityIds) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Boolean> deleted = TemplateProvider.singleObjectTemplate(session, () -> {
                final LinkedList<String> idNames = new LinkedList<>(getIdNameAndType().keySet());
                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaDelete<EntityType> delete = cb.createCriteriaDelete(entityClazz);
                Root<EntityType> from = delete.from(entityClazz);
                delete.where(cb.in(from.get(idNames.getFirst())).value(entityIds));
                //query to execute: delete from EntityType where id in :ids
                int result = session.createQuery(delete).executeUpdate();
                return result == entityIds.size();
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

    @Override
    public boolean executeUpdateQuery(final String updateQuery, final Map<String, ?> paramMap) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Boolean> queryExec = TemplateProvider.singleObjectTemplate(session, () -> {
                @SuppressWarnings("rawtypes")
                NativeQuery sql = session.createSQLQuery(updateQuery);
                setQueryParameters(sql, paramMap);
                final int result = sql.executeUpdate();
                logger.info("The result of the query {}.", result);
                return true;
            }, Boolean.class);
            return queryExec.orElse(false);
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

    private void setQueryParameters(Query<?> sql, Map<String, ?> paramMap) {
        if (paramMap != null && !paramMap.isEmpty()) {
            for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
                if (entry.getValue() instanceof Collection) {
                    sql.setParameterList(entry.getKey(), Collection.class.cast(entry.getValue()));
                } else if (entry.getValue() instanceof Map) {
                    throw new HibernateException("You need to have a good reason to set map as parameter. " +
                                    "I do not know that reasons so please feel free and implement it");
                } else {
                    sql.setParameter(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public List<EntityType> executeSelectQuery(
            final String selectQuery, final Map<String, Object> paramMap, final EntityFieldMapper<EntityType> mapper
    ) {
        try (Session session = sessionFactory.openSession()) {
            return TemplateProvider.collectionTemplate(session, () -> {
                @SuppressWarnings("unchecked")
                NativeQuery<Object[]> sql = session.createSQLQuery(selectQuery);
                setQueryParameters(sql, paramMap);
                return sql.getResultStream().map(mapper::map).collect(toList());
            });
        } catch (Exception ex) {
            logger.error("Something went wrong.", ex);
            throw new HibernateException(ex);
        }
    }

}
