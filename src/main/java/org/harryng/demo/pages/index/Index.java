package org.harryng.demo.pages.index;

import io.smallrye.mutiny.Uni;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/index")
public class Index {
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Uni<String> getIndex() {
        return Uni.createFrom().item(() -> "Hello world: " + LocalDateTime.now());
    }
}
