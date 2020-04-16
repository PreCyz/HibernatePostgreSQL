package pg.hib;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.TestBeanRepository;
import pg.hib.entities.TestBean;
import pg.hib.providers.HibernateSessionProvider;
import pg.hib.providers.TemplateProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HibernateSessionProvider hibSessionProvider = HibernateSessionProvider.getInstance();
        SessionFactory sessionFactory = hibSessionProvider.getSessionFactory();

        //simpleOpr(sessionFactory);

        TestBeanRepository testBeanRepository = new TestBeanRepository(sessionFactory);

        List<TestBean> all = testBeanRepository.findAll();
        LOGGER.info("This is what I got from db {}", all);

        all = testBeanRepository.findByIds(Stream.of(1L, 3L).collect(toSet()));
        LOGGER.info("This is what I got from db {}", all);

        Optional<TestBean> testBean = testBeanRepository.findById(4L);
        testBean.ifPresent(tb -> LOGGER.info("This is what I got from db {}", tb));

        sessionFactory.close();
    }

    private static void simpleOpr(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();

        Optional<TestBean> testBean = TemplateProvider.getTemplate(session, () -> {
            TestBean objInsert = new TestBean(true, LocalDateTime.now());
            session.save(objInsert);
            return objInsert;
        }, TestBean.class);

        testBean.ifPresent(bean -> LOGGER.info("This was saved {}.", bean.toString()));

        TemplateProvider.voidTemplate(session, () -> {
            TestBean tb = session.get(TestBean.class, 1L);
            tb.setActive(false);
            session.saveOrUpdate(tb);
        });

        testBean = TemplateProvider.getTemplate(session, () -> session.get(TestBean.class, 1L), TestBean.class);

        testBean.ifPresent(bean -> LOGGER.info("This was updated {}.", bean.toString()));

        session.close();
    }


}
