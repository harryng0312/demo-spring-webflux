package org.harryng.demo.pages.index;

import org.harryng.demo.pages.dto.IndexRes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/index")
public class Index {
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Mono<IndexRes> getIndex() {
        return Mono.just(new IndexRes("" + LocalDateTime.now()));
    }
}
