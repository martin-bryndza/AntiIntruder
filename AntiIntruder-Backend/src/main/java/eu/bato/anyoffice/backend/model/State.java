package eu.bato.anyoffice.backend.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Entity prepared for use with as a state of a resource.
 *
 * @author Bato
 */
@Entity
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    private long maxDuration;
    private long minDuration;

    @ManyToOne(optional = true)
    private State defaultSuccessor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return Maximum state duration in milliseconds or 0 for not specified
     */
    public long getMaxDuration() {
        return maxDuration;
    }

    /**
     *
     * @param maxDuration Maximum state duration in milliseconds or 0 for not
     * specified
     */
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
     *
     * @return Minimum state duration in milliseconds or 0 for not specified
     */
    public long getMinDuration() {
        return minDuration;
    }

    /**
     *
     * @param minDuration Minimum state duration in milliseconds or 0 for not
     * specified
     */
    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    /**
     * Gets the default successor of this state. If the given successor is null,
     * this object will returned as its successor.
     *
     * @return Default successor state of this state.
     */
    public State getDefaultSuccessor() {
        return defaultSuccessor == null ? this : defaultSuccessor;
    }

    public void setDefaultSuccessor(State defaultSuccessor) {
        this.defaultSuccessor = defaultSuccessor;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "State{" + "id=" + id + ", name=" + name + ", maxDuration=" + maxDuration + ", minDuration=" + minDuration + '}';
    }

}
