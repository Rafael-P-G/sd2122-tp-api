package util;


public class UrlParser {

    private static final String RESTURIDESCRIPTOR = "/rest/";
    private static final String SOAPURIDESCRIPTOR = "/soap/";

    public static String extractFileURIFromURL(String url) {
        StringBuilder sb = new StringBuilder(url);
        int sizeOfService = RESTURIDESCRIPTOR.length() - 1;
        int lastIndexOfUri = sb.lastIndexOf(RESTURIDESCRIPTOR);

        if(lastIndexOfUri < 0){
            lastIndexOfUri = sb.lastIndexOf(SOAPURIDESCRIPTOR);
            sizeOfService = SOAPURIDESCRIPTOR.length() - 1;
        }

        String serverURI = sb.substring(0, lastIndexOfUri + sizeOfService);
        return serverURI;
    }
}
