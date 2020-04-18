package pg.hib;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pg.hib.dao.CarDao;
import pg.hib.entities.CarEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public final class MockProviderTest {

    private static CarDao carDao;

    @BeforeAll
    static void buildMock() {
        long id = 1;
        Map<Serializable, CarEntity> existingEntitiesMap = new HashMap<>();
        existingEntitiesMap.put(id, new CarEntity(id++, true, LocalDateTime.now(), LocalDateTime.now()));
        existingEntitiesMap.put(id, new CarEntity(id++, true, LocalDateTime.now(), LocalDateTime.now()));
        existingEntitiesMap.put(id, new CarEntity(id++, true, LocalDateTime.now(), LocalDateTime.now()));

        carDao = new BasicCRUDMockProvider<CarEntity, CarDao>()
                .addEntityClass(CarEntity.class)
                .addMockType(CarDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(existingEntitiesMap)
                .buildMock();
    }

    @Test
    void givenEntityAndEntityClass_whenSave_thenReturnEntityWithId() {
        final Optional<CarEntity> actual = carDao.save(new CarEntity(true, LocalDateTime.now(), LocalDateTime.now()));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isNotNull();
    }

    @Test
    void givenCollectionOfEntitiesAndEntityClass_whenSave_thenReturnEntitiesWithIds() {
        final List<CarEntity> entities = Stream.of(
                new CarEntity(true, LocalDateTime.now(), LocalDateTime.now()),
                new CarEntity(false, LocalDateTime.now(), LocalDateTime.now())
        ).collect(toList());

        final List<CarEntity> actual = carDao.saveAll(entities);

        assertThat(actual.isEmpty()).isFalse();
        actual.forEach(carEntity -> assertThat(carEntity.getId()).isPositive());
    }

    @Test
    void givenExisitngEntities_whenFindAll_thenReturnAllEntities() {
        final List<CarEntity> actual = carDao.findAll();

        //check @BeforeAll method
        assertThat(actual).hasSize(3);
    }

    @Test
    void givenExistingEntities_whenFindById_thenReturnEntityWithThatId() {
        final long id = 1L;
        final Optional<CarEntity> actual = carDao.findById(id);

        //check @BeforeAll method
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isEqualTo(id);
    }

    @Test
    void givenExistingEntities_whenFindByIds_thenReturnEntitiesWithTheseIds() {
        final List<Serializable> ids = Arrays.asList(1L, 2L);
        final List<CarEntity> actual = carDao.findByIds(ids);

        //check @BeforeAll method
        assertThat(actual).hasSize(ids.size());
        assertThat(actual.stream().map(CarEntity::getId).collect(toList()))
                .containsExactly(1L, 2L);
    }
}
