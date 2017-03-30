package com.alperez.bt_microphone.rest.command;

import android.support.annotation.NonNull;

import com.alperez.bt_microphone.bluetoorh.connector.OnConnectionStatusListener;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.bluetoorh.connector.data.OnTextDataReceivedListener;
import com.alperez.bt_microphone.rest.response.BaseResponse;
import com.alperez.bt_microphone.rest.response.ResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stanislav.perchenko on 3/28/2017.
 */

public abstract class BaseRestCommand {

    private BtDataTransceiver dataTransceiver;

    private AtomicInteger currentSequenceNumber = new AtomicInteger(0);
    private final Object zsgdunLock = new Object();
    private JSONObject jResponse;

    public BaseRestCommand(@NonNull BtDataTransceiver dataTransceiver) {
        this.dataTransceiver = dataTransceiver
                .addOnTransceiverStatusListener(connStatusListener)
                .addOnTextDataReceivedListener(rcvListener);
    }

    public final void release() {
        dataTransceiver.removeOnTextDataReceivedListener(rcvListener);
        dataTransceiver.removeOnTransceiverStatusListener(connStatusListener);
    }

    /**
     * Sends the command with currently set parameters in blocking mode and waits for the response.
     * Always returns a valid response (either OK of Error).
     * Throws IOException in case of timeout.
     * @param timeoutMillis timeout. Value 0 means no timeout.
     * @return
     * @throws IOException in case of timeout or other communication error.
     */
    public final BaseResponse sendBlocked(int timeoutMillis) throws IOException {
        int seqNumber = nextSequenceNumber();
        currentSequenceNumber.set(seqNumber);

        //----  Build request body  ----
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("id", seqNumber);
            jBody.put("command", getCommandName());
            fillInRequestBody(jBody);
        } catch (JSONException e) {
            throw new IOException("Error build request body", e);
        }

        //----  send request async  ----
        dataTransceiver.sendDataNonBlocked(jBody.toString());

        //----  Wait for the response with timeout  ----
        synchronized (zsgdunLock) {
            try {
                zsgdunLock.wait(timeoutMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Receiving was interrupted");
            }
            if (jResponse == null) {
                throw new IOException("Timeout - response was not received in time!");
            }
            try {
                return ResponseParser.parseResponse(jResponse);
            } catch (JSONException e) {
                throw new IOException("Error parse response", e);
            }
        }
    }


    /**
     * This method must be implemented by subclasses to actually fill in the request body
     * instance provided by the parent
     * @param jBody
     * @throws JSONException
     */
    protected abstract void fillInRequestBody(JSONObject jBody) throws JSONException;

    /**
     * This method must be implemented by subclasses. It returns a value of the "command" field
     * of the request JSON
     * @return
     */
    public abstract String getCommandName();




    private OnTextDataReceivedListener rcvListener = new OnTextDataReceivedListener() {
        @Override
        public void onReceive(String data) {
            try {
                JSONObject jrsp = new JSONObject(data);
                int id = jrsp.getInt("id");
                if (id == currentSequenceNumber.get()) {
                    synchronized (zsgdunLock) {
                        jResponse = jrsp;
                        zsgdunLock.notify();
                    }
                } else {
                    // This is not for me!
                }
            } catch (JSONException ignore) {
                // Ignore this error.
                // Timeout error may be fired in the waiting-for-response thread.
            }
        }
    };

    private OnConnectionStatusListener connStatusListener = new OnConnectionStatusListener() {
        @Override
        public void onConnectionRestorted(int nTry) {
            //TODO Do something useful here ))
        }

        @Override
        public void onConnectionAttemptFailed(int nTry) {
            //TODO Do something useful here ))
        }

        @Override
        public void onConnectionBroken(String nameThreadCauseFailure, Throwable reason) {
            //TODO Do something useful here ))
        }
    };






    /********************************  Sequence counter section  **********************************/
    private static int commonSequenceCounter = 1;
    private static int nextSequenceNumber() {
        return commonSequenceCounter ++;
    }
}
