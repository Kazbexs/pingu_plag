package pgdp.net;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {
	// TODO
    private HttpMethod method;
    private String path;
    private Map<String, String> parameters;


    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public HttpRequest(String firstLine, String body) {
        this.parameters = new HashMap<>();
        String[] strings = firstLine.split(" ");
        if (strings.length < 2) {
            throw new InvalidRequestException("Invalid number of argument in first line!");
        }

        String method = strings[0];

        switch (method) {
            case "GET" : this.method = HttpMethod.GET; break;
            case "POST" : this.method = HttpMethod.POST; break;
            default : throw new InvalidRequestException("Invalid HttpMethod!");
        }
        String[] pathParam = strings[1].split("\\?");
        this.path = pathParam[0];

        if (pathParam.length < 1 || pathParam.length > 2) {
            throw new InvalidRequestException("Invalid path!");
        }
        if (pathParam.length == 2) {
            String par[] = pathParam[1].split("&");
            this.parse(par);
        }
        if (body != null) {
            String[] splittedBody = body.split("&");
            this.parse(splittedBody);
        }
    }

    private void parse(String[] toParse) {
        for (String str : toParse) {
            String[] s = str.split("=");
            if (s.length != 2) {
                throw new InvalidRequestException("Invalid parameter");
            }
            this.parameters.put(s[0], URLDecoder.decode(s[1], StandardCharsets.UTF_8));
        }
    }

}

