package pg.hib;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pg.hib.dao.CarDao;
import pg.hib.dao.TestEntityDao;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public final class MockProviderTest {

    private static CarDao carDao;
    private static TestEntityDao testEntityDao;

    private static final int NUMBER_OF_GENERATED_CAR_ENTITIES = 10;
    private static final int NUMBER_OF_GENERATED_TEST_ENTITIES = 5;

    @BeforeAll
    static void buildMock() {
        carDao = new BasicCRUDMockProvider<CarEntity, CarDao>()
                .addEntityClass(CarEntity.class)
                .addMockType(CarDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(EntitiesGenerator.generateCarEntitiesMap(NUMBER_OF_GENERATED_CAR_ENTITIES))
                .addSelectQueryResult(new ArrayList<>())
                .addUpdateAnswer(true)
                .buildMock();

        testEntityDao = new BasicCRUDMockProvider<TestEntity, TestEntityDao>()
                .addEntityClass(TestEntity.class)
                .addMockType(TestEntityDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(EntitiesGenerator.generateTestEntitiesMap(NUMBER_OF_GENERATED_TEST_ENTITIES))
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
        assertThat(actual).hasSize(NUMBER_OF_GENERATED_CAR_ENTITIES);
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
    void givenCarEntityUpdateQuery_whenExecuteUpdateQuery_thenReturnFalse() {
        final boolean result = carDao.executeUpdateQuery("update cars set active = NOT active");

        assertThat(result).isTrue();
    }

    @Test
    void givenTestBeanSelectQuery_whenExecuteUpdateQuery_thenThrowHibernateException() {
        final List<CarEntity> carEntities = carDao.executeSelectQuery("select * from cars");
        assertThat(carEntities).isEmpty();
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
    void givenTestExistingEntities_whenFindAll_thenReturnAllEntities() {
        final List<TestEntity> actual = testEntityDao.findAll();

        assertThat(actual).hasSize(NUMBER_OF_GENERATED_TEST_ENTITIES);
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

    @Test
    void givenTestEntityUpdateQuery_whenExecuteUpdateQuery_thenReturnFalse() {
        final boolean result = testEntityDao.executeUpdateQuery("update test_bean set active = NOT active");

        assertThat(result).isFalse();
    }

    @Test
    void givenTestEntitySelectQuery_whenExecuteUpdateQuery_thenThrowHibernateException() {
        try {
            testEntityDao.executeSelectQuery("select * from test_bean");
            fail("Should throw HibernateException.");
        } catch (HibernateException ex) {
            assertThat(ex.getMessage()).isEqualTo("In order to use native SQL select query this method has to be implemented.");
        }
    }
}
