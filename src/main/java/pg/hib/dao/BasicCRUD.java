package pg.hib.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BasicCRUD<EntityType extends Serializable> {
    List<EntityType> findAll();

    Optional<EntityType> findById(Serializable id);

    List<EntityType> findByIds(Collection<Serializable> ids);

    Optional<EntityType> save(EntityType entity);

    List<EntityType> save(List<EntityType> entities);

    boolean delete(EntityType entity);

    boolean deleteByIds(Collection<Serializable> entities);

    boolean deleteAll(Collection<EntityType> entities);
}