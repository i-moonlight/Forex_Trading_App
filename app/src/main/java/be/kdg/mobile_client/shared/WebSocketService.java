package be.kdg.mobile_client.shared;

import android.util.Log;

import com.google.gson.Gson;

import javax.inject.Inject;

import be.kdg.mobile_client.shared.di.modules.ControllerModule;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

/**
 * All websocket traffic is handled here.
 */
public class WebSocketService {
    private StompClient stompClient;
    private static final String TAG = "WebSocketService";
    private static final int WEBSOCKET_HEARTBEAT_MS = 10000;

    public void connect() {
        if (stompClient == null) {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, ControllerModule.WEBSOCKET_URL);
            stompClient.withClientHeartbeat(WEBSOCKET_HEARTBEAT_MS).withServerHeartbeat(WEBSOCKET_HEARTBEAT_MS);
            stompClient.connect();
        }
    }

    public <T> Flowable<T> watch(String url, Class<T> clazz) {
        return stompClient.topic(url)
                .map(parseWithGsonInto(clazz))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(s -> Log.i(TAG, "Started listening on " + url))
                .doOnEach(each -> Log.i(TAG, "Update " + clazz.getSimpleName() + " received: " + each.getValue()));
    }

    public void send(String url, String json) {
        stompClient.send(url, json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private <T> Function<StompMessage, T> parseWithGsonInto(Class<T> clazz) {
        return msg -> new Gson().fromJson(msg.getPayload(), clazz);
    }

    public void disconnect() {
        if (stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

}
