package pg.hib;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pg.hib.dao.CarDao;
import pg.hib.dao.TestEntityDao;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public final class MockProviderTest {

    private static CarDao carDao;
    private static TestEntityDao testEntityDao;

    @BeforeAll
    static void buildMock() {
        Random random = new Random();
        long id = 1;
        Map<Serializable, CarEntity> existingCarEntitiesMap = new HashMap<>();
        existingCarEntitiesMap.put(id, new CarEntity(id++, random.nextBoolean(), LocalDateTime.now(), LocalDateTime.now()));
        existingCarEntitiesMap.put(id, new CarEntity(id++, random.nextBoolean(), LocalDateTime.now(), LocalDateTime.now()));
        existingCarEntitiesMap.put(id, new CarEntity(id, random.nextBoolean(), LocalDateTime.now(), LocalDateTime.now()));

        carDao = new BasicCRUDMockProvider<CarEntity, CarDao>()
                .addEntityClass(CarEntity.class)
                .addMockType(CarDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(existingCarEntitiesMap)
                .buildMock();

        id = 1;
        Map<Serializable, TestEntity> existingTestEntitiesMap = new HashMap<>();
        existingTestEntitiesMap.put(id, new TestEntity(id++, random.nextBoolean(), LocalDateTime.now()));
        existingTestEntitiesMap.put(id, new TestEntity(id++, random.nextBoolean(), LocalDateTime.now()));
        existingTestEntitiesMap.put(id, new TestEntity(id, random.nextBoolean(), LocalDateTime.now()));

        testEntityDao = new BasicCRUDMockProvider<TestEntity, TestEntityDao>()
                .addEntityClass(TestEntity.class)
                .addMockType(TestEntityDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(existingTestEntitiesMap)
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

    @Test
    void givenTestEntityAndEntityClass_whenSave_thenReturnEntityWithId() {
        final Optional<TestEntity> actual = testEntityDao.save(new TestEntity(true, LocalDateTime.now()));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isNotNull();
    }

    @Test
    void givenCollectionOfTestEntitiesAndTestEntityClass_whenSave_thenReturnEntitiesWithIds() {
        final List<TestEntity> entities = Stream.of(
                new TestEntity(true, LocalDateTime.now()),
                new TestEntity(false, LocalDateTime.now())
        ).collect(toList());

        final List<TestEntity> actual = testEntityDao.saveAll(entities);

        assertThat(actual.isEmpty()).isFalse();
        actual.forEach(carEntity -> assertThat(carEntity.getId()).isPositive());
    }

    @Test
    void givenTestExisitngEntities_whenFindAll_thenReturnAllEntities() {
        final List<TestEntity> actual = testEntityDao.findAll();

        //check @BeforeAll method
        assertThat(actual).hasSize(3);
    }

    @Test
    void givenExistingTestEntities_whenFindById_thenReturnEntityWithThatId() {
        final long id = 1L;
        final Optional<TestEntity> actual = testEntityDao.findById(id);

        //check @BeforeAll method
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isEqualTo(id);
    }

    @Test
    void givenExistingTestEntities_whenFindByIds_thenReturnEntitiesWithTheseIds() {
        final List<Serializable> ids = Arrays.asList(1L, 2L);
        final List<TestEntity> actual = testEntityDao.findByIds(ids);

        //check @BeforeAll method
        assertThat(actual).hasSize(ids.size());
        assertThat(actual.stream().map(TestEntity::getId).collect(toList()))
                .containsExactly(1L, 2L);
    }
}
