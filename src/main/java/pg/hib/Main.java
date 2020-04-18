package pg.hib;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.CarDao;
import pg.hib.dao.DaoFactory;
import pg.hib.dao.TestBeanDao;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestBean;
import pg.hib.providers.HibernateSessionProvider;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HibernateSessionProvider hibSessionProvider = HibernateSessionProvider.getInstance();
        SessionFactory sessionFactory = hibSessionProvider.getSessionFactory();

        //playingWithTestBean(sessionFactory);
        playingWithCarEntity(sessionFactory);

        sessionFactory.close();
    }

    private static void playingWithTestBean(SessionFactory sessionFactory) {
        TestBeanDao repository = DaoFactory.getTestBeanRepository(sessionFactory);

        simpleOpr(repository);

        List<TestBean> all = repository.findAll();
        LOGGER.info("This is what I got from db {}", all);

        all = repository.findByIds(Stream.of(1L, 3L).collect(toSet()));
        LOGGER.info("This is what I got from db {}", all);

        Optional<TestBean> testBean = repository.findById(4L);
        testBean.ifPresent(tb -> LOGGER.info("This is what I got from db {}", tb));

        List<TestBean> activeEntities = repository.findByActive(true);
        LOGGER.info("Only active entities {}", activeEntities);

        repository.save(new TestBean(false, LocalDateTime.now()));
        List<TestBean> inactiveEntities = repository.findByActive(false);
        LOGGER.info("Only inactive entities {}", inactiveEntities);

//        batchSave(repository);
    }

    private static void simpleOpr(TestBeanDao repository) {
        Optional<TestBean> testBean = repository.save(new TestBean(true, LocalDateTime.now()));
        testBean.ifPresent(bean -> LOGGER.info("This was saved {}.", bean.toString()));

        testBean = repository.findById(1L);
        testBean.ifPresent(bean -> LOGGER.info("This was updated {}.", bean.toString()));

        testBean.ifPresent(bean -> {
            boolean delete = repository.delete(bean);
            LOGGER.info("Record with id {} deleted [{}].", bean.getId(), delete);
        });
    }

    private static void batchTestBeanSave(TestBeanDao repository) {
        Random random = new Random();
        random.nextBoolean();
        List<TestBean> beans = new LinkedList<>();
        for (int i = 0; i < 100; ++i) {
            beans.add(new TestBean(random.nextBoolean(), LocalDateTime.now()));
        }
        List<TestBean> testBean = repository.save(beans);
        testBean.forEach(System.out::println);
    }

    private static void playingWithCarEntity(SessionFactory sessionFactory) {
        CarDao repository = DaoFactory.getCarRepository(sessionFactory);

        LOGGER.info("Creating cars with batch############.%n");
        batchCarSave(repository);

        Random random = new Random();
        Set<Serializable> ids = Stream.of((long) random.nextInt(300), (long) random.nextInt(300)).collect(toUnmodifiableSet());
        LOGGER.info("Getting cars by ids {}", ids);
        List<CarEntity> carByIds = repository.findByIds(ids);
        carByIds.forEach(System.out::println);

        LOGGER.info("Getting cars by firstRegistrationDate.");
        LinkedList<CarEntity> carsByDate = new LinkedList<>(
                repository.findAllByFirstRegistrationDateAfter(LocalDateTime.now().minusWeeks(50))
        );
        carsByDate.forEach(System.out::println);

        LOGGER.info("Deleting cars from previous query.");
        boolean deleteAll = repository.deleteAll(carsByDate);
        LOGGER.info("Cars from previous query deleted [{}}].", deleteAll);

        LOGGER.info("Deleting by Ids [{}] cars.", carByIds.size());
        boolean deleteByIds = repository.deleteByIds(carByIds.stream().map(CarEntity::getId).collect(toSet()));
        LOGGER.info("Cars from previous query deleted [{}].", deleteByIds);

        carByIds = repository.findAll();
        LOGGER.info("There are: {} cars.", carByIds.size());
    }

    private static void batchCarSave(CarDao repository) {
        Random random = new Random();
        List<CarEntity> cars = new LinkedList<>();
        for (int i = 0; i < 100; ++i) {
            cars.add(new CarEntity(
                    random.nextBoolean(),
                    LocalDateTime.now(),
                    LocalDateTime.now().minusMonths(random.nextInt(250))
            ));
        }
        List<CarEntity> savedCars = repository.save(cars);
        savedCars.forEach(System.out::println);
    }

}
