package dev.wjteo.gateway;

import dev.wjteo.utilities.Serialization;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AGateway {
    enum HttpRequestType {
        GET,
        POST,
        DELETE
    }

    private static String _hostAddress = "";
    protected final Class<? extends AGateway> gatewayClass;

    protected AGateway(@NonNull final Class<? extends AGateway> gatewayClass) {
        this.gatewayClass = gatewayClass;
    }

    public static void setHost(@NonNull String hostAddress) {
        hostAddress = hostAddress.trim();

        if (hostAddress.endsWith("/"))
            hostAddress = StringUtils.chop(hostAddress);

        if (!hostAddress.startsWith("http://"))
            hostAddress = "http://" + hostAddress;

        _hostAddress = hostAddress;
    }

    @NonNull
    private <T> HttpUriRequest createRequest(@NonNull final HttpRequestType requestType, @NonNull final String endpoint, @NonNull final Map<String, String> requestParameters, final T requestObject) throws RestException {
        final URI uri = createURI(endpoint, requestParameters);
        final HttpUriRequest httpRequest;

        // Initializing the HttpUriRequest based on requestType.
        switch (requestType) {
            case GET -> httpRequest = new HttpGet(uri);
            case DELETE -> httpRequest = new HttpDelete(uri);
            case POST -> httpRequest = new HttpPost(uri);
            default -> throw new RestException("Failed to create HTTP request - invalid request type.");
        }

        // Serialize the requestObject if httpRequest is HttpPost and include it in the request.
        // Probably should think of another way than to use null.
        if (httpRequest instanceof HttpPost httpPost && null != requestObject) {
            final Optional<String> json = Serialization.serialize(requestObject);

            if (json.isEmpty())
                throw new RestException("Failed to create HTTP request - object serialization failed.");

            final StringEntity stringEntity;

            try {
                stringEntity = new StringEntity(json.get());
            } catch (UnsupportedEncodingException e) {
                throw new RestException("Failed to create HTTP request - unsupported encoding for serialized object.");
            }

            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setEntity(stringEntity);
        }

        return httpRequest;
    }

    @NonNull
    private URI createURI(@NonNull String endpoint, @NonNull final Map<String, String> requestParameters) throws RestException {
        if (!endpoint.startsWith(_hostAddress))
            endpoint = _hostAddress + endpoint;

        final URIBuilder uriBuilder;

        try {
            LoggerFactory.getLogger(gatewayClass).info("Creating URI with address: " + endpoint);
            uriBuilder = new URIBuilder(endpoint);
        } catch (URISyntaxException e) {
            LoggerFactory.getLogger(gatewayClass).error("Failed to create HTTP request: " + e.getMessage());
            throw new RestException("Failed to create HTTP request - URI syntax exception when initializing builder.");
        }

        // Adding request parameters, if any, into the URI builder
        if (!requestParameters.isEmpty())
            requestParameters.forEach(uriBuilder::addParameter);

        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            LoggerFactory.getLogger(gatewayClass).error("Failed to create HTTP request: " + e.getMessage());
            throw new RestException("Failed to create HTTP request - URI syntax exception when building URI.");
        }
    }

    @NonNull
    protected String executeWithResponse(@NonNull final HttpUriRequest request) throws RestException {
        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(request)) {
            if (null == response)
                throw new RestException("Failed to execute HTTP request - no response.");

            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_NO_CONTENT)
                return "";

            final HttpEntity responseEntity = response.getEntity();

            if (null == responseEntity)
                throw new RestException("Failed to execute HTTP request - no response entity.");

            final String responseEntityString = IOUtils.toString(responseEntity.getContent(), StandardCharsets.UTF_8);

            if (null == responseEntityString)
                throw new RestException("Failed to execute HTTP request - unable to parse response entity content.");

            return switch (statusCode) {
                case HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED -> responseEntityString;
                default -> {
                    LoggerFactory.getLogger(gatewayClass).error("HTTP " + statusCode + ": " + responseEntityString);
                    throw new RestException(responseEntityString);
                }
            };
        } catch (IOException e) {
            throw new RestException("Failed to execute HTTP request - I/O.");
        }
    }

    @NonNull
    protected <T> Optional<T> executeWithObjectResponse(@NonNull final HttpUriRequest request, @NonNull final Class<T> objectClass) throws RestException {
        final String responseEntityString = executeWithResponse(request);

        if (responseEntityString.isBlank())
            return Optional.empty();

        Optional<T> object = Serialization.deserialize(responseEntityString, objectClass);

        if (object.isPresent())
            return object;

        throw new RestException("Failed to execute HTTP request - unable to deserialize response entity content.");
    }

    @NonNull
    protected <T> List<T> executeWithListResponse(@NonNull final HttpUriRequest request, @NonNull final Class<T> listElementClass) throws RestException {
        final String responseEntityString = executeWithResponse(request);

        if (responseEntityString.isBlank())
            return List.of();

        Optional<List<T>> object = Serialization.deserializeList(responseEntityString, listElementClass);

        if (object.isPresent())
            return object.get();

        throw new RestException("Failed to execute HTTP request - unable to deserialize response entity content.");
    }

    @NonNull
    protected HttpUriRequest createGetRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters) throws RestException {
        return createRequest(HttpRequestType.GET, endpoint, requestParameters, null);
    }

    @NonNull
    protected <T> HttpUriRequest createPostRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters, @NonNull final T requestObject) throws RestException {
        return createRequest(HttpRequestType.POST, endpoint, requestParameters, requestObject);
    }

    @NonNull
    protected HttpUriRequest createDeleteRequest(@NonNull final String endpoint, @NonNull final Map<String, String> requestParameters) throws RestException {
        return createRequest(HttpRequestType.DELETE, endpoint, requestParameters, null);
    }
}






























