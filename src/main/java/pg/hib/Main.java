package pg.hib;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.AbstractRepository;
import pg.hib.dao.TestBeanRepository;
import pg.hib.entities.TestBean;
import pg.hib.providers.HibernateSessionProvider;
import pg.hib.providers.TemplateProvider;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HibernateSessionProvider hibSessionProvider = HibernateSessionProvider.getInstance();
        SessionFactory sessionFactory = hibSessionProvider.getSessionFactory();
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

        sessionFactory.close();
    }

    private static void simpleOpr(TestBeanRepository repository) {
        Optional<TestBean> testBean = repository.save(new TestBean(true, LocalDateTime.now()));
        testBean.ifPresent(bean -> LOGGER.info("This was saved {}.", bean.toString()));

        testBean = repository.findById(1L);
        testBean.ifPresent(bean -> LOGGER.info("This was updated {}.", bean.toString()));

        testBean.ifPresent(repository::delete);
    }

    private static void batchSave(TestBeanRepository repository) {
        Random random = new Random();
        random.nextBoolean();
        List<TestBean> beans = new LinkedList<>();
        for (int i = 0; i < 100; ++i) {
            beans.add(new TestBean(random.nextBoolean(), LocalDateTime.now()));
        }
        List<TestBean> testBean = repository.save(beans);
        testBean.forEach(System.out::println);
    }


}
