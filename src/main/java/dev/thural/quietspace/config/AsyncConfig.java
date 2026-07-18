package dev.thural.quietspace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        return Executors.newVirtualThreadPerTaskFactory();
    }

    @Override
    public Executor getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskFactory();
    }
}
