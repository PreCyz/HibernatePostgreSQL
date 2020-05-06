package pg.hib.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BasicCRUD<EntityType extends Serializable> {
    List<EntityType> findAll();

    Optional<EntityType> findById(Serializable id);

    List<EntityType> findByIds(Collection<Serializable> ids);

    Optional<EntityType> save(EntityType entity);

    List<EntityType> saveAll(List<EntityType> entities);

    boolean delete(EntityType entity);

    boolean deleteByIds(Collection<Serializable> entities);

    boolean deleteAll(Collection<EntityType> entities);

    List<EntityType> executeSelectQuery(
            String selectQuery, final Map<String, Object> paramMap, final EntityFieldMapper<EntityType> mapper
    );

    boolean executeUpdateQuery(final String sqlQuery, final Map<String, Object> paramMap);
}
