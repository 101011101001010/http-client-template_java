package dev.wjteo;

import dev.wjteo.entity.SomeExample;
import dev.wjteo.gateway.AGateway;
import dev.wjteo.gateway.RestException;
import dev.wjteo.gateway.SomeExampleGateway;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        AGateway.setHost("localhost:8080/api/ ");

        try {
            SomeExampleGateway.getInstance().postSpecial(new SomeExample(), Instant.now(), Instant.now());
        } catch (RestException e) {
            LoggerFactory.getLogger(Main.class).error(e.getMessage());
        }

        try {
            SomeExampleGateway.getInstance().getAll(Instant.now(), Instant.now());
        } catch (RestException e) {
            LoggerFactory.getLogger(Main.class).error(e.getMessage());
        }

        try {
            SomeExampleGateway.getInstance().create(new SomeExample());
        } catch (RestException e) {
            LoggerFactory.getLogger(Main.class).error(e.getMessage());
        }
    }
}
