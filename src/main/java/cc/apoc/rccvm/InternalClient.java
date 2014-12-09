package cc.apoc.rccvm;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalClient {
    private static final Logger logger = LoggerFactory.getLogger("rccvm.core");

    HttpClient client;

    int port;

    public InternalClient(Config config) {
        this.port = config.vm.daemon_host_port;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.client_timeout).setSocketTimeout(config.client_timeout)
                .build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public String get(String path) {
        return post(path, null);
    }

    public String post(String path, String postData) {
        String url = String.format("http://127.0.0.1:%d%s", port, path);
        logger.info("internal request: " + url);
        try {
            HttpRequestBase request;
            if (postData == null) {
                request = new HttpGet(url);
            } else {
                HttpPost post = new HttpPost(url);
                post.setEntity(new StringEntity(postData));
                request = post;
            }
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
