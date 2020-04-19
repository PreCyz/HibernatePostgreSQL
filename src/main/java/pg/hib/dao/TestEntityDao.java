package pg.hib.dao;

import pg.hib.entities.TestEntity;

import java.util.List;

public interface TestEntityDao extends BasicCRUD<TestEntity> {
    List<TestEntity> findByActive(boolean active);
}
