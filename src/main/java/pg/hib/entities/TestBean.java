package pg.hib.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_bean")
public class TestBean implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private boolean active;

    @Column
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime created;

    public TestBean() { }

    public TestBean(boolean active, LocalDateTime created) {
        this.active = active;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "TestBean{" +
                "id=" + id +
                ", active=" + active +
                ", created=" + created +
                '}';
    }
}
