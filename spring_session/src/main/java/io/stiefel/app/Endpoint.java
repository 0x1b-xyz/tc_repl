package io.stiefel.app;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@EnableAutoConfiguration
public class Endpoint {

    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_REQUEST = "lastRequest";
    public static final String KEY_NODES = "nodes";

    public static void main(String[] args) {
        SpringApplication.run(Endpoint.class);
    }

    @Autowired
    private RedisTemplate<String,String> redis;

    private String hostname;

    @PostConstruct
    void setup() throws UnknownHostException {
        this.hostname = InetAddress.getLocalHost().getHostName();
        redis.opsForList().leftPush(KEY_NODES, hostname);
    }

    @PreDestroy
    void shutdown() {
        redis.opsForList().remove(KEY_NODES, 1, hostname);
    }

    @RequestMapping("/")
    String index(@RequestParam(value = KEY_NAME, required = false) String name,
                 HttpSession session) {

        List<String> nodes = redis.opsForList().range(KEY_NODES, 0, -1);

        if (name != null && !name.equals(session.getAttribute(KEY_NAME))) {
            session.setAttribute(KEY_NAME, name);
        }

        if (session.getAttribute(KEY_LAST_REQUEST) == null)
            session.setAttribute(KEY_LAST_REQUEST, System.currentTimeMillis());
        Long lastRequest = (Long)session.getAttribute(KEY_LAST_REQUEST);
        session.setAttribute(KEY_LAST_REQUEST, System.currentTimeMillis());

        return String.format("Hello %s from %s. Your last request was %s ago. Here are the %s nodes: %s",
                session.getAttribute(KEY_NAME),
                hostname,
                DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - lastRequest, true, true),
                nodes.size(),
                nodes.stream().collect(Collectors.joining(", "))
        );
    }

    @RequestMapping("/hostname")
    String hostname()  throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

}
