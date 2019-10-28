package nl.jaapcoomans.demo.mockserver.gameservice.domain;

public interface CodeChecker {
    Result checkCode(Code code, Code guess);
}
