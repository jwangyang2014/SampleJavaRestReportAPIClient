package com.backstopsolutions.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/rest")
public class ReportController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${report.base_user}")
    private String baseUrl;

    @RequestMapping("/runActivityReport")
    public Map runFundsReport() {
        ReportQuery reportQuery = getReportQuery();
        ResponseEntity<Map> objectResponseEntity = restTemplate
                .postForEntity(baseUrl + "/crm/runActivityReport", getHttpEntity(reportQuery), Map.class);
        return objectResponseEntity.getBody();
    }

    /**
     * build report query parameters
     *
     * @return
     */
    private ReportQuery getReportQuery() {
        JsonNode node = null;
        try {
            node = getReportDefinitionFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReportQuery.builder()
                .queryDefinition(node.toString())
                .asOf("2019-09-17T13:33:24.750507Z")
                .restrictionExpression("${(report.field5 > date(\"30/10/2019\")) && (!in(report.field3, \"[\\\"Document\\\",\\\"Email\\\"]\"))}")
                .build();
    }

    /**
     * setting request header
     *
     * @param reportQuery
     * @return
     */
    private HttpEntity<MultiValueMap> getHttpEntity(ReportQuery reportQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authorization = generateAuthorization("usernam", "password");
        headers.add("Authorization", authorization);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("queryDefinition", reportQuery.getQueryDefinition());
        params.add("asOf", reportQuery.getAsOf());
        params.add("restrictionExpression", reportQuery.getRestrictionExpression());
        return new HttpEntity<>(params, headers);
    }


    private String generateAuthorization(String username, String pwd) {
        String authorization = username + ":" + pwd;
        return "Basic " + Base64Utils.encodeToString(authorization.getBytes());
    }

    private JsonNode getReportDefinitionFromFile() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ReportDefinition.json").getFile());
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = new FileInputStream(file);
        JsonNode node = mapper.readValue(input, JsonNode.class);
        return node;
    }
}
