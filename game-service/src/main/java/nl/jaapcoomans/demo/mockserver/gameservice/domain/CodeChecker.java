package nl.jaapcoomans.demo.mockserver.gameservice.domain;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.Code;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.Result;

public interface CodeChecker {
    Result checkCode(Code code, Code guess);
}
