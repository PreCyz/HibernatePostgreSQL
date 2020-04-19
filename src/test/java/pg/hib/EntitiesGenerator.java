package pg.hib;

import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class EntitiesGenerator {

    private EntitiesGenerator() { }

    public static Map<Serializable, CarEntity> generateCarEntitiesMap(int numberOfEntities) {
        Random random = new Random();
        Map<Serializable, CarEntity> entitiesMap = new HashMap<>(numberOfEntities);
        for (long i = 1; i <= numberOfEntities; ++i) {
            entitiesMap.put(i, new CarEntity(i, random.nextBoolean(), LocalDateTime.now(), LocalDateTime.now()));
        }
        return entitiesMap;
    }

    public static Map<Serializable, TestEntity> generateTestEntitiesMap(int numberOfEntities) {
        Random random = new Random();
        Map<Serializable, TestEntity> entitiesMap = new HashMap<>(numberOfEntities);
        for (long i = 1; i <= numberOfEntities; ++i) {
            entitiesMap.put(i, new TestEntity(i, random.nextBoolean(), LocalDateTime.now()));
        }
        return entitiesMap;
    }
}
