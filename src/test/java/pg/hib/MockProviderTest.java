package pg.hib;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pg.hib.dao.CarDao;
import pg.hib.dao.TestEntityDao;
import pg.hib.entities.CarEntity;
import pg.hib.entities.LocalDateTimeConverter;
import pg.hib.entities.TestEntity;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public final class MockProviderTest {

    private static CarDao carDao;
    private static TestEntityDao testEntityDao;

    private static final int DEFAULT_NUMBER_ENTITIES_TO_GENERATE = 10;

    @BeforeAll
    static void buildMock() {
        carDao = new BasicCRUDMockProvider<CarEntity, CarDao>()
                .addEntityClass(CarEntity.class)
                .addMockType(CarDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(EntitiesGenerator.generateCarEntitiesMap(DEFAULT_NUMBER_ENTITIES_TO_GENERATE))
                .addSelectQueryResult(new ArrayList<>())
                .addUpdateAnswer(true)
                .buildMock();

        testEntityDao = new BasicCRUDMockProvider<TestEntity, TestEntityDao>()
                .addEntityClass(TestEntity.class)
                .addMockType(TestEntityDao.class)
                .addDeleteAnswer(true)
                .addExistingEntities(EntitiesGenerator.generateTestEntitiesMap(DEFAULT_NUMBER_ENTITIES_TO_GENERATE))
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
        assertThat(actual).hasSize(DEFAULT_NUMBER_ENTITIES_TO_GENERATE);
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
        final List<CarEntity> carEntities = carDao.executeSelectQuery(
                "select * from cars",
                fields -> new CarEntity(
                        Long.valueOf(String.valueOf(fields[0])),
                        Boolean.parseBoolean(String.valueOf(fields[1])),
                        new LocalDateTimeConverter().convertToEntityAttribute(String.valueOf(fields[2])),
                        new LocalDateTimeConverter().convertToEntityAttribute(String.valueOf(fields[3]))
                ));
        assertThat(carEntities).isEmpty();
    }

    @Test
    void givenTestEntityAndEntityClass_whenSave_thenReturnEntityWithId() {
        final Optional<TestEntity> actual = testEntityDao.save(new TestEntity(true, LocalDateTime.now()));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getEntityId()).isNotNull();
    }

    @Test
    void givenCollectionOfTestEntitiesAndTestEntityClass_whenSave_thenReturnEntitiesWithIds() {
        final List<TestEntity> entities = Stream.of(
                new TestEntity(true, LocalDateTime.now()),
                new TestEntity(false, LocalDateTime.now())
        ).collect(toList());

        final List<TestEntity> actual = testEntityDao.saveAll(entities);

        assertThat(actual.isEmpty()).isFalse();
        actual.forEach(carEntity -> assertThat(carEntity.getEntityId()).isPositive());
    }

    @Test
    void givenTestExistingEntities_whenFindAll_thenReturnAllEntities() {
        final List<TestEntity> actual = testEntityDao.findAll();

        assertThat(actual).hasSize(DEFAULT_NUMBER_ENTITIES_TO_GENERATE);
    }

    @Test
    void givenExistingTestEntities_whenFindById_thenReturnEntityWithThatId() {
        final int id = 1;
        final Optional<TestEntity> actual = testEntityDao.findById(id);

        //check @BeforeAll method
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getEntityId()).isEqualTo(id);
    }

    @Test
    void givenExistingTestEntities_whenFindByIds_thenReturnEntitiesWithTheseIds() {
        final List<Serializable> ids = Arrays.asList(1, 2);
        final List<TestEntity> actual = testEntityDao.findByIds(ids);

        //check @BeforeAll method
        assertThat(actual).hasSize(ids.size());
        assertThat(actual.stream().map(TestEntity::getEntityId).collect(toList()))
                .containsExactly(1, 2);
    }

    @Test
    void givenTestEntityUpdateQuery_whenExecuteUpdateQuery_thenReturnFalse() {
        final boolean result = testEntityDao.executeUpdateQuery("update test_bean set active = NOT active");

        assertThat(result).isFalse();
    }

    @Test
    void givenTestEntitySelectQuery_whenExecuteUpdateQuery_thenThrowHibernateException() {
        try {
            testEntityDao.executeSelectQuery("select * from test_bean", fields -> new TestEntity());
            fail("Should throw HibernateException.");
        } catch (HibernateException ex) {
            assertThat(ex.getMessage()).isEqualTo("In order to use native SQL select query this method has to be implemented.");
        }
    }

    @Test
    void givenTestEntity_whenGetIdFiledTypeAndName_thenReturnIdNameAndItsType() {
        final LinkedHashMap<String, Class<?>> idAndType = getIdAndType();

        assertThat(idAndType.keySet()).containsExactly("entityId");
        assertThat(idAndType.values()).containsExactly(Integer.class);
    }

    private LinkedHashMap<String, Class<?>> getIdAndType() {
        LinkedHashMap<String, Class<?>> result = new LinkedHashMap<>(1);
        for (Field field : TestEntity.class.getDeclaredFields()) {
//            System.out.printf("name - %s, type - %s%n", field.getName(), field.getType());
            for (Annotation declaredAnnotation : field.getDeclaredAnnotations()) {
                if (declaredAnnotation.annotationType() == javax.persistence.Id.class) {
//                    System.out.printf("annotation type %s%n", declaredAnnotation.annotationType());
                    result.put(field.getName(), field.getType());
                }
//                System.out.printf("%n");
            }
        }
        return result;
    }
}
