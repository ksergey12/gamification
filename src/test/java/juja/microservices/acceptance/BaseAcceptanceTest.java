package juja.microservices.acceptance;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import juja.microservices.gamification.Gamification;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.io.Reader;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder.mongoDb;
import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Danil Kuznetsov
 */

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {Gamification.class})
@DirtiesContext
public class BaseAcceptanceTest {

    static final String ACHIEVE_DAILY_URL = "/achieve/daily";
    static final String ACHIEVE_THANKS_URL = "/achieve/thanks";
    static final String ACHIEVE_CODENJOY_URL = "/achieve/codenjoy";
    static final String ACHIEVE_INTERVIEW_URL = "/achieve/interview";
    static final String USER_POINT_SUM_URL = "/user/pointSum";
    static final String USER_ACHIEVE_DETAILS_URL = "/user/achieveDetails";
    static final String EMPTY_JSON_CONTENT_REQUEST = "";


    @LocalServerPort
    int localPort;

    @Rule
    public MongoDbRule mongoDbRule = new MongoDbRule(
            mongoDb()
                    .databaseName("gamification")
                    .host("127.0.0.1")
                    .port(27017)
                    .build()
    );

    @Before
    public void setup() {
        RestAssured.port = localPort;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    String convertToString(Reader reader) throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        return buffer.toString();
    }

    void printConsoleReport(String url, String expectedResponse, ResponseBody actualResponse) throws IOException {

        System.out.println("\n\n URL  - " + url);

        System.out.println("\n Actual Response :\n");
        actualResponse.prettyPrint();

        System.out.println("\nExpected Response :");
        System.out.println("\n" + expectedResponse + "\n\n");
    }

    Response getResponse(String url, String jsonContentRequest, HttpMethod method) {
        RequestSpecification specification = given()
                .contentType("application/json")
                .body(jsonContentRequest)
                .when();
        Response responce;
        if (HttpMethod.POST == method) {
            responce = specification.post(url);
        } else if (HttpMethod.GET == method) {
            responce = specification.get(url);
        } else {
            throw new RuntimeException("Unsupported method in getResponse()");
        }
        return responce
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    Response getControlResponse() {
        return getResponse(USER_POINT_SUM_URL, EMPTY_JSON_CONTENT_REQUEST, HttpMethod.GET);
    }
}
