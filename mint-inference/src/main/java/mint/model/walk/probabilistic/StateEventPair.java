package mint.model.walk.probabilistic;

import java.util.Objects;

public class StateEventPair {
    private Integer state;
    private String event;

    public StateEventPair(Integer state, String event) {
        this.state = state;
        this.event = event;
    }

    public Integer getState() {
        return state;
    }

    public Object getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateEventPair that = (StateEventPair) o;
        return state.equals(that.state) && event.equals(that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state.toString(), event);
    }
}
