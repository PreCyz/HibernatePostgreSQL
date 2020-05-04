package pg.hib.dao;

@FunctionalInterface
public interface EntityFieldMapper<EntityType> {
    EntityType map(Object[] fields);
}
