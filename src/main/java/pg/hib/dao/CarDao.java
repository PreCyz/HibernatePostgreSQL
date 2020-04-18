package pg.hib.dao;

import pg.hib.entities.CarEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface CarDao extends BasicCRUD<CarEntity> {
    List<CarEntity> findAllByFirstRegistrationDateAfter(LocalDateTime localDateTime);
}
