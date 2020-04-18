package pg.hib;

import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.BasicCRUD;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class BasicCRUDMockProvider<EntityType extends Serializable, MockType extends BasicCRUD<EntityType>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(BasicCRUDMockProvider.class);

    private Map<Serializable, EntityType> existingEntitiesMap;
    private Class<EntityType> entityClass;
    private Class<MockType> returnTypeClass;
    private boolean deleteAnswer;

    public BasicCRUDMockProvider() {
    }

    public BasicCRUDMockProvider<EntityType, MockType> addExistingEntities(Map<Serializable, EntityType> existingEntitiesMap) {
        this.existingEntitiesMap = new HashMap<>(existingEntitiesMap);
        return this;
    }

    public BasicCRUDMockProvider<EntityType, MockType> addEntityClass(Class<EntityType> entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public BasicCRUDMockProvider<EntityType, MockType> addMockType(Class<MockType> returnTypeClass) {
        this.returnTypeClass = returnTypeClass;
        return this;
    }

    public BasicCRUDMockProvider<EntityType, MockType> addDeleteAnswer(boolean deleteAnswer) {
        this.deleteAnswer = deleteAnswer;
        return this;
    }

    public MockType buildMock() {

        final Map<Serializable, EntityType> workingCopyMap =
                Optional.ofNullable(existingEntitiesMap).orElseGet(HashMap::new);

        final MockType mockObj = mock(returnTypeClass);

        if (!workingCopyMap.isEmpty()) {
            mockReadMethods(workingCopyMap, mockObj);
        }

        mockWriteMethods(entityClass, mockObj);

        mockDeleteMethods(entityClass, mockObj);

        return mockObj;
    }

    private void mockReadMethods(Map<Serializable, EntityType> existingEntitiesMap, MockType mockObj) {

        when(mockObj.findAll()).thenReturn(new ArrayList<>(existingEntitiesMap.values()));

        when(mockObj.findById(any(Serializable.class))).then((Answer<Optional<EntityType>>) invocationOnMock -> {
            final Serializable entityId = invocationOnMock.getArgument(0);
            return Optional.of(existingEntitiesMap.get(entityId));
        });

        when(mockObj.findByIds(anyCollection())).then((Answer<List<EntityType>>) invocationOnMock -> {
            final Collection<Serializable> idsCollection = invocationOnMock.getArgument(0);
            return existingEntitiesMap.entrySet()
                    .stream()
                    .filter(entry -> idsCollection.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        });
    }

    private void mockWriteMethods(Class<EntityType> entityClass, MockType mockObj) {
        when(mockObj.save(any(entityClass))).then((Answer<Optional<EntityType>>) invocationOnMock -> {
            final EntityType entity = invocationOnMock.getArgument(0);
            final Field id = entity.getClass().getDeclaredField("id");
            id.setAccessible(true);
            id.set(entity, Long.valueOf(new Random().nextInt(1000) + ""));
            LOGGER.info("Entity of type {} was saved. {} id was given.", entity.getClass(), id.get(entity));
            return Optional.of(entity);
        });

        when(mockObj.saveAll(anyList())).then((Answer<List<EntityType>>) invocationOnMock -> {
            final List<EntityType> entities = invocationOnMock.getArgument(0);
            long idNumber = 0;
            for (EntityType entity : entities) {
                idNumber++;
                try {
                    final Field id = entity.getClass().getDeclaredField("id");
                    id.setAccessible(true);
                    id.set(entity, Long.valueOf(idNumber + ""));
                    LOGGER.info("Entity of type {} was saved. {} id was given.", entity.getClass(), idNumber);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    LOGGER.error("Could not provide id for the entity {}.", entity, e);
                }
            }
            return entities;
        });
    }

    private void mockDeleteMethods(Class<EntityType> entityClass, MockType mockObj) {
        when(mockObj.delete(any(entityClass))).thenReturn(deleteAnswer);
        when(mockObj.deleteByIds(anyCollection())).thenReturn(deleteAnswer);
        when(mockObj.deleteAll(anyCollection())).thenReturn(deleteAnswer);
    }
}
