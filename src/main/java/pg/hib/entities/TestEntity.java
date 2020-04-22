package pg.hib.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_bean")
public class TestEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entity_id")
    private Integer entityId;

    @Column
    private boolean active;

    @Column
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime created;

    public TestEntity() { }

    public TestEntity(boolean active, LocalDateTime created) {
        this(null, active, created);
    }

    public TestEntity(Integer entityId, boolean active, LocalDateTime created) {
        this.entityId = entityId;
        this.active = active;
        this.created = created;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "entityId=" + entityId +
                ", active=" + active +
                ", created=" + created +
                '}';
    }
}
