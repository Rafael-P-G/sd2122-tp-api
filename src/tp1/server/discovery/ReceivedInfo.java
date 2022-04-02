package tp1.server.discovery;

import java.net.URI;
import java.util.ArrayList;

public class ReceivedInfo {

    private static final int SIZE = 500;

    private URI[] serviceURIs;
    private int time;

    private int counter;

    public ReceivedInfo(){
        serviceURIs = new URI[SIZE];
        time = -1;
        counter = 0;
    }

    public ReceivedInfo(URI uri, int time){
        serviceURIs = new URI[SIZE];
        counter = 0;
        addServiceURI(uri);
        setTime(time);
    }

    public URI[] getServiceURIs(){
        return serviceURIs;
    }

    public int getTime(){
        return time;
    }

    public void addServiceURI(URI URIToAdd){

        if(counter == 0){ return; }

        for (URI uri: serviceURIs) {
            if(uri.equals(URIToAdd)) {return;}
        }


        serviceURIs[counter++] = URIToAdd;

    }

    public void setTime(int time){
        this.time = time;
    }


}
