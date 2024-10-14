package burp;

import org.json.JSONObject;
import org.json.XML;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utilities {
    public static byte[] convertToURLENCODED(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) throws Exception {
        // GET to POST
        byte[] request = requestResponse.getRequest();
        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")) {
            request = helpers.toggleRequestMethod(request);
        }

        // get request info
        IRequestInfo requestInfo = helpers.analyzeRequest(request);
        byte content_type = requestInfo.getContentType();
        String body = new String(request, requestInfo.getBodyOffset(), request.length - requestInfo.getBodyOffset(), StandardCharsets.UTF_8);

        // ignore other content-type
        if (content_type != IRequestInfo.CONTENT_TYPE_XML && content_type != IRequestInfo.CONTENT_TYPE_JSON) {
            return null;
        }

        // convert xml to json
        if (content_type == IRequestInfo.CONTENT_TYPE_XML) {
            JSONObject jsonObject = XML.toJSONObject(body);
            if (jsonObject.length() == 1) {
                String firstKey = jsonObject.keys().next();
                jsonObject = jsonObject.getJSONObject(firstKey);
            }
            body = jsonObject.toString();
        }

        // convert json to urlencoded
        JSONObject jsonObject = new JSONObject(body);
        String urlencoded = json2urlencoded(jsonObject);

        // update content-type header
        List<String> headers;
        headers = helpers.analyzeRequest(request).getHeaders();
        Iterator<String> iter = headers.iterator();
        while (iter.hasNext()) {
            if (iter.next().contains("Content-Type"))
                iter.remove();
        }
        headers.add("Content-Type: application/x-www-form-urlencoded;charset=UTF-8");
        return helpers.buildHttpMessage(headers, urlencoded.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] convertToJSON(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) throws Exception {
        // GET to POST
        byte[] request = requestResponse.getRequest();
        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")) {
            request = helpers.toggleRequestMethod(request);
        }

        // get request info
        IRequestInfo requestInfo = helpers.analyzeRequest(request);
        byte content_type = requestInfo.getContentType();
        String body = new String(request, requestInfo.getBodyOffset(), request.length - requestInfo.getBodyOffset(), StandardCharsets.UTF_8);

        // ignore other content-type
        if (content_type != IRequestInfo.CONTENT_TYPE_NONE && content_type != IRequestInfo.CONTENT_TYPE_URL_ENCODED && content_type != IRequestInfo.CONTENT_TYPE_XML) {
            return null;
        }

        // convert xml to json
        JSONObject jsonObject = new JSONObject();
        if (content_type == IRequestInfo.CONTENT_TYPE_XML) {
            jsonObject = XML.toJSONObject(body);
            if (jsonObject.length() == 1) {
                String firstKey = jsonObject.keys().next();
                jsonObject = jsonObject.getJSONObject(firstKey);
            }
        }

        // convert urlencoded to json
        if (content_type == IRequestInfo.CONTENT_TYPE_NONE || content_type == IRequestInfo.CONTENT_TYPE_URL_ENCODED) {
            jsonObject = urlencoded2json(body);
        }

        // update content-type header
        List<String> headers;
        headers = helpers.analyzeRequest(request).getHeaders();
        Iterator<String> iter = headers.iterator();
        while (iter.hasNext()) {
            if (iter.next().contains("Content-Type"))
                iter.remove();
        }
        headers.add("Content-Type: application/json;charset=UTF-8");
        return helpers.buildHttpMessage(headers, jsonObject.toString().getBytes());
    }

    public static byte[] convertToXML(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) throws Exception {
        // GET to POST
        byte[] request = requestResponse.getRequest();
        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")) {
            request = helpers.toggleRequestMethod(request);
        }

        // get request info
        IRequestInfo requestInfo = helpers.analyzeRequest(request);
        byte content_type = requestInfo.getContentType();
        String body = new String(request, requestInfo.getBodyOffset(), request.length - requestInfo.getBodyOffset(), StandardCharsets.UTF_8);

        // ignore other content-type
        if (content_type != IRequestInfo.CONTENT_TYPE_NONE && content_type != IRequestInfo.CONTENT_TYPE_URL_ENCODED && content_type != IRequestInfo.CONTENT_TYPE_JSON) {
            return null;
        }

        // convert urlencoded to json
        if (content_type == IRequestInfo.CONTENT_TYPE_NONE || content_type == IRequestInfo.CONTENT_TYPE_URL_ENCODED) {
            body = urlencoded2json(body).toString();
        }

        // convert json to xml
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xml.append("<root>");
        JSONObject jsonObject = new JSONObject(body);
        xml.append(XML.toString(jsonObject));
        xml.append("</root>");

        // update content-type header
        List<String> headers;
        headers = helpers.analyzeRequest(request).getHeaders();
        Iterator<String> iter = headers.iterator();
        while (iter.hasNext()) {
            if (iter.next().contains("Content-Type"))
                iter.remove();
        }
        headers.add("Content-Type: application/xml;charset=UTF-8");
        return helpers.buildHttpMessage(headers, xml.toString().getBytes(StandardCharsets.UTF_8));
    }

    // convert urlencoded to json
    private static JSONObject urlencoded2json(String urlencoded) throws Exception {
        JSONObject jsonObject = new JSONObject();
        String[] pairs = urlencoded.split("&");
        for (String pair : pairs) {
            if (Objects.equals(pair, "")) {
                continue;
            }
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
            jsonObject.put(key, value.trim());
        }
        return jsonObject;
    }

    // convert json to urlencoded
    private static String json2urlencoded(JSONObject jsonObject) throws Exception {
        List<String> pairs = new ArrayList<>();
        Iterator<String> keysIterator = jsonObject.keys();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            Object value = jsonObject.get(key);
            final String pair = key + "=" + URLEncoder.encode(value.toString(), "UTF-8");
            pairs.add(pair);
        }
        return String.join("&", pairs);
    }
}