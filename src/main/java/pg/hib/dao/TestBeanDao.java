package pg.hib.dao;

import pg.hib.entities.TestBean;

import java.util.List;

public interface TestBeanDao extends BasicCRUD<TestBean> {
    List<TestBean> findByActive(boolean active);
}
