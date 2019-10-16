package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Code {
    private final List<ColoredPin> code;

    Code(ColoredPin pin0, ColoredPin pin1, ColoredPin pin2, ColoredPin pin3) {
        this.code = List.of(pin0, pin1, pin2, pin3);
    }

    int numberOfPins() {
        return this.code.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != Code.class) {
            return false;
        }
        Code other = (Code) obj;
        return Objects.equals(this.code, other.code);
    }

    @Override
    public int hashCode() {
        return this.code.hashCode();
    }

    @Override
    public String toString() {
        var joiner = new StringJoiner(", ", "[Code: ", "]");

        this.code.stream()
                .map(Objects::toString)
                .forEach(joiner::add);

        return joiner.toString();
    }
}
