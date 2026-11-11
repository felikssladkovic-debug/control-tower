package io.feliks.pact.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    private final String version;

    public SystemInfoController(@Value("${pact.version}") String version) {
        this.version = version;
    }

    @GetMapping("/info")
    @Operation(summary = "Get PACT backend identity and version")
    @ApiResponse(responseCode = "200", description = "Backend identity and Maven project version")
    @ApiResponse(responseCode = "500", description = "Unexpected server error")
    public SystemInfo info() {
        return new SystemInfo("PACT", version);
    }
}
