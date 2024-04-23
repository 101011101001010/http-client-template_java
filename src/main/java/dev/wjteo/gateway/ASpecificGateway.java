package dev.wjteo.gateway;

import dev.wjteo.entity.AEntity;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ASpecificGateway<E extends AEntity> extends AGateway {
    protected final Class<E> entityClass;
    protected String entityEndpoint;

    protected ASpecificGateway(@NonNull final Class<? extends AGateway> gatewayClass, @NonNull final Class<E> entityClass, @NonNull String entityEndpoint) {
        super(gatewayClass);
        this.entityClass = entityClass;
        setEntityEndpoint(entityEndpoint);
    }

    protected void setEntityEndpoint(String entityEndpoint) {
        entityEndpoint = entityEndpoint.trim();

        if (entityEndpoint.endsWith("/"))
            entityEndpoint = StringUtils.chop(entityEndpoint);

        if (!entityEndpoint.startsWith("/"))
            entityEndpoint = "/" + entityEndpoint;

        this.entityEndpoint = entityEndpoint;
    }

    @NonNull
    public List<E> getAll(@NonNull final Map<String, String> queryParameters) throws RestException {
        final HttpUriRequest request = createGetRequest(entityEndpoint, queryParameters);
        return executeWithListResponse(request, entityClass);
    }

    @NonNull
    public Optional<E> getById(final long id, @NonNull final Map<String, String> queryParameters) throws RestException {
        final HttpUriRequest request = createGetRequest(entityEndpoint + "/" + id, queryParameters);
        return executeWithObjectResponse(request, entityClass);
    }

    @NonNull
    public List<E> getByIds(@NonNull final List<Long> ids, @NonNull final Map<String, String> queryParameters) throws RestException {
        final HttpUriRequest request = createPostRequest(entityEndpoint + "/ids", queryParameters, ids);
        return executeWithListResponse(request, entityClass);
    }

    /**
     * Test
     * @param entity test
     * @return test
     * @throws RestException test12345
     */
    @NonNull
    public Optional<E> create(@NonNull final E entity) throws RestException {
        final HttpUriRequest request = createPostRequest(entityEndpoint + "/create", new HashMap<>(), entity);
        return executeWithObjectResponse(request, entityClass);
    }

    @NonNull
    public Optional<E> update(@NonNull final E entity) throws RestException {
        final HttpUriRequest request = createPostRequest(entityEndpoint + "/update", new HashMap<>(), entity);
        return executeWithObjectResponse(request, entityClass);
    }

    public boolean delete(final long id) throws RestException {
        final HttpUriRequest request = createDeleteRequest(entityEndpoint + "/delete/" + id, new HashMap<>());
        return executeWithObjectResponse(request, entityClass).isPresent();
    }

    @NonNull
    protected Optional<E> executeWithObjectResponse(@NonNull final HttpUriRequest request) throws RestException {
        return super.executeWithObjectResponse(request, entityClass);
    }

    @NonNull
    protected List<E> executeWithListResponse(@NonNull final HttpUriRequest request) throws RestException {
        return super.executeWithListResponse(request, entityClass);
    }

    @Override
    @NonNull
    protected HttpUriRequest createGetRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters) throws RestException {
        return super.createGetRequest(processEndpoint(endpoint), requestParameters);
    }

    @Override
    @NonNull
    protected <T> HttpUriRequest createPostRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters, @NonNull final T requestObject) throws RestException {
        return super.createPostRequest(processEndpoint(endpoint), requestParameters, requestObject);
    }

    @Override
    @NonNull
    protected HttpUriRequest createDeleteRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters) throws RestException {
        return super.createDeleteRequest(processEndpoint(endpoint), requestParameters);
    }

    @NonNull
    private String processEndpoint(@NonNull String endpoint) {
        endpoint = endpoint.trim();

        if (!endpoint.startsWith(entityEndpoint))
            endpoint = entityEndpoint + endpoint;

        return endpoint;
    }
}
