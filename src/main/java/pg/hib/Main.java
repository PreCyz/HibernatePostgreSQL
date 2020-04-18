package pg.hib;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.CarRepository;
import pg.hib.dao.TestBeanRepository;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestBean;
import pg.hib.providers.HibernateSessionProvider;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
        TestBeanRepository repository = new TestBeanRepository(sessionFactory);

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

    private static void simpleOpr(TestBeanRepository repository) {
        Optional<TestBean> testBean = repository.save(new TestBean(true, LocalDateTime.now()));
        testBean.ifPresent(bean -> LOGGER.info("This was saved {}.", bean.toString()));

        testBean = repository.findById(1L);
        testBean.ifPresent(bean -> LOGGER.info("This was updated {}.", bean.toString()));

        testBean.ifPresent(repository::delete);
    }

    private static void batchTestBeanSave(TestBeanRepository repository) {
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
        CarRepository carRepository = new CarRepository(sessionFactory);

        batchCarSave(carRepository);

        List<CarEntity> carByIds = carRepository.findByIds(Stream.of(1L, 3L).collect(toUnmodifiableSet()));
        carByIds.forEach(System.out::println);
    }

    private static void batchCarSave(CarRepository repository) {
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
