package nl.jaapcoomans.demo.mockserver.gameservice.remote;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.Code;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.ColoredPin;

public class CodeGuessBody {
    private CodeBody actual;
    private CodeBody guess;

    CodeGuessBody(Code actual, Code guess) {
        this.actual = new CodeBody(actual);
        this.guess = new CodeBody(guess);
    }

    public CodeBody getActual() {
        return actual;
    }

    public CodeBody getGuess() {
        return guess;
    }

    public static class CodeBody {
        private ColoredPin pin0;
        private ColoredPin pin1;
        private ColoredPin pin2;
        private ColoredPin pin3;

        private CodeBody(Code code) {
            this.pin0 = code.getPin(0);
            this.pin1 = code.getPin(1);
            this.pin2 = code.getPin(2);
            this.pin3 = code.getPin(3);
        }

        public ColoredPin getPin0() {
            return pin0;
        }

        public ColoredPin getPin1() {
            return pin1;
        }

        public ColoredPin getPin2() {
            return pin2;
        }

        public ColoredPin getPin3() {
            return pin3;
        }
    }
}
