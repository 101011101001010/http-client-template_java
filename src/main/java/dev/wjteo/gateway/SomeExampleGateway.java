package dev.wjteo.gateway;

import dev.wjteo.entity.SomeExample;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SomeExampleGateway extends ASpecificGateway<SomeExample> {
    private static SomeExampleGateway instance;

    protected SomeExampleGateway() {
        super(SomeExampleGateway.class, SomeExample.class, "/someexample");
    }

    public static SomeExampleGateway getInstance() {
        if (null == instance)
            instance = new SomeExampleGateway();
        return instance;
    }

    public void setEndpoint(@NonNull String endpoint) {
        endpoint = endpoint.trim();

        if (endpoint.endsWith("/"))
            endpoint = StringUtils.chop(endpoint);

        if (!endpoint.startsWith("/"))
            endpoint = "/" + endpoint;

        setEntityEndpoint(endpoint);
    }

    public List<SomeExample> getAll(final Instant startInstant, final Instant endInstant) throws RestException {
        final Map<String, String> queryParameters = new HashMap<>();
        if (null != startInstant) queryParameters.put("startInstant", String.valueOf(startInstant.toEpochMilli()));
        if (null != endInstant) queryParameters.put("endInstant", String.valueOf(endInstant.toEpochMilli()));
        return getAll(queryParameters);
    }

    public List<SomeExample> getSpecial(final Instant startInstant, final Instant endInstant) throws RestException {
        final Map<String, String> queryParameters = new HashMap<>();
        if (null != startInstant) queryParameters.put("startInstant", String.valueOf(startInstant.toEpochMilli()));
        if (null != endInstant) queryParameters.put("endInstant", String.valueOf(endInstant.toEpochMilli()));

        HttpUriRequest request = createGetRequest("/special", queryParameters);
        return executeWithListResponse(request);
    }

    public Optional<SomeExample> postSpecial(final SomeExample example, final Instant startInstant, final Instant endInstant) throws RestException {
        final Map<String, String> queryParameters = new HashMap<>();
        if (null != startInstant) queryParameters.put("startInstant", String.valueOf(startInstant.toEpochMilli()));
        if (null != endInstant) queryParameters.put("endInstant", String.valueOf(endInstant.toEpochMilli()));

        final HttpUriRequest request = createPostRequest("/specialpost", queryParameters, example);
        return executeWithObjectResponse(request);
    }
}
