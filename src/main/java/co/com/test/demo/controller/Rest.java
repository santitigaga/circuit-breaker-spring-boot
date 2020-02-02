package co.com.test.demo.controller;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RestController
public class Rest {



    private TimeLimiterConfig timeLimiterConfiguration;
public Rest(){
    timeLimiterConfiguration = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(5500))
            .cancelRunningFuture(true)
            .build();
}
    @GetMapping
    public String test()  {
        TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfiguration);


        Supplier<CompletableFuture<String>> get = () -> {
            return CompletableFuture.supplyAsync(() -> {
                return this.hello();
            });
        };
        Callable<String> getLimiter = TimeLimiter.decorateFutureSupplier(timeLimiter, get);


        return Try.of(getLimiter::call).recover((throwable) -> fallback()).get();


    }

    private String hello(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello world";
    }
    private String fallback() {
        return "Request fails. It takes long time to response";
    }
}
