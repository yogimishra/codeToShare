import android.content.Context;
import android.content.res.Resources;


/**
 * <p>This class has the default configuration for the endpoints. The class will look for an
 * end point configuration class, when not found, it will return the default values.</p>
 * @author Yogesh Mishra
 * @version 1.0
 */
public class EndPointConfiguration {
    private static String BASE_URL = "/nenx/api";
    private static String API_HOST;
    private static String LS_HOST;

    /**
     * <p>Returns the base url for API requests.</p>
     * @return a string containing where the api is in the server.
     */
    public static String getBaseUrl()
    {
        return BASE_URL;
    }

    /**
     * <p>Returns the host in use for API requests</p>
     * @return a string containing the host for API requests.
     */
    public static String getApiHost() {
        return API_HOST;
    }

    /**
     * <p>Returns the light streamer host.</p>
     * @return a string containing the light streamer host.
     */
    public static String getLightStreamerHost() {
        return LS_HOST;
    }

    /**
     * <p>Load end points from given strings.</p>
     * @param apiHost is the host for the API.
     * @param lightStreamerHost is the host for the light streamer.
     * @param baseUrl is the base URL for the API.
     */
    public static void loadFromStrings(String apiHost, String lightStreamerHost, String baseUrl)
    {
        BASE_URL = baseUrl;
        API_HOST = apiHost;
        LS_HOST = lightStreamerHost;
    }
}
