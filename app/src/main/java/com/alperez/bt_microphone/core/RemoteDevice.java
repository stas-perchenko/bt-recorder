package com.alperez.bt_microphone.core;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;
import com.alperez.bt_microphone.rest.command.impl.GainDownRestCommand;
import com.alperez.bt_microphone.rest.command.impl.GainUpRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PhantomOffRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PhantomOnRestCommand;
import com.alperez.bt_microphone.rest.command.impl.StatusRestCommand;
import com.alperez.bt_microphone.rest.command.impl.VersionRestCommand;
import com.alperez.bt_microphone.rest.response.BaseResponse;
import com.alperez.bt_microphone.rest.response.ErrorResponse;
import com.alperez.bt_microphone.rest.response.FileSuccessResponse;
import com.alperez.bt_microphone.rest.response.SimpleSuccessResponse;
import com.alperez.bt_microphone.rest.response.StatusSuccessResponse;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceFile;
import com.alperez.bt_microphone.rest.response.commonmodels.DeviceStatus;
import com.android.annotations.NonNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stanislav.perchenko on 3/26/2017.
 */

public class RemoteDevice {

    private BtDataTransceiver dataTransceiver;
    private int commandTimeout;

    private volatile boolean released;

    private CommandPool mCommandPool = new CommandPool();
    private Executor commandExecutor;

    private OnCommandResultListener resultListener;

    public RemoteDevice(int commandTimeout, @NonNull OnCommandResultListener resultListener) {
        this.commandTimeout = commandTimeout;
        this.resultListener = resultListener;
        commandExecutor = Executors.newSingleThreadExecutor();
        commandStatus();
        commandVersion();
        commandCurrentFile();

    }


    public void release() {
        released = true;
        if (commandExecutor instanceof ExecutorService) {
            ((ExecutorService) commandExecutor).shutdownNow();
        }
        mCommandPool.releaseAllCommands();
        //TODO Release device connection
    }


    public void commandVersion() {
        scheduleNoParamCommand(VersionRestCommand.class);
    }

    public void commandStatus() {
        scheduleNoParamCommand(StatusRestCommand.class);
    }

    public void commandPlay() {
        scheduleNoParamCommand(PlayRestCommand.class);
    }

    public void commandPause() {
        scheduleNoParamCommand(PauseRestCommand.class);
    }

    public void commandStop() {
        scheduleNoParamCommand(StopRestCommand.class);
    }


    public void commandRecord() {
        scheduleNoParamCommand(RecordRestCommand.class);
    }

    public void commandCurrentFile() {
        scheduleNoParamCommand(CurrFileRestCommand.class);
    }

    public void commandPrevFile() {
        scheduleNoParamCommand(PrevFileRestCommand.class);
    }

    public void commandNextFile() {
        scheduleNoParamCommand(NextFileRestCommand.class);
    }

    public void commandFormat() {
        khfsdif;
    }

    public void commandPowerOff() {
        scheduleNoParamCommand(PowerOffRestCommand.class);
    }

    public void commandSetTime(Date time) {
        fgdg;
    }

    public void commandSetLocation(Location location) {
        sdfdg;
    }

    public void commandSetSampleRate(int sampleRate) {
        fgfdg;
    }

    public void commandGainUp() {
        scheduleNoParamCommand(GainUpRestCommand.class);
    }

    public void commandGainDown() {
        scheduleNoParamCommand(GainDownRestCommand.class);
    }

    public void commandFastForward(int nSeconds) {

    }

    public void commandFastReverse(int nSeconds) {
        fgfdg;
    }

    public void commandPhantomOn() {
        scheduleNoParamCommand(PhantomOnRestCommand.class);
    }

    public void commandPhantomOff() {
        scheduleNoParamCommand(PhantomOffRestCommand.class);
    }

    private void scheduleNoParamCommand(Class<? extends BaseRestCommand> commandClass) {
        try {
            BaseRestCommand comm = mCommandPool.getCommand(commandClass.getName());
            commandExecutor.execute(new CommandHandler(comm, commandTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    /**********************************************************************************************/
    private class CommandPool {
        private Map<String, BaseRestCommand> commandMap = new HashMap<>();

        public BaseRestCommand getCommand(String className) throws Exception {
            BaseRestCommand comm = commandMap.get(className);
            if (comm == null) {
                comm = ((Class<BaseRestCommand>) Class.forName(className)).getConstructor(BtDataTransceiver.class).newInstance(dataTransceiver);
                commandMap.put(className, comm);
            }
            return comm;
        }

        public void releaseAllCommands() {
            for (BaseRestCommand comm : commandMap.values()) {
                comm.release();
            }
        }
    }


    /**********************************************************************************************/
    private class CommandHandler implements Runnable {
        private final BaseRestCommand command;
        private final int timeoutMillis;

        public CommandHandler(BaseRestCommand command, int timeoutMillis) {
            this.command = command;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public void run() {
            onExecuteCommandSynchronously(command, timeoutMillis);
        }
    }


    private void onExecuteCommandSynchronously(BaseRestCommand command, int timeoutMillis) {
        try {
            BaseResponse resp = command.sendBlocked(timeoutMillis);
            if (!resp.success() && resp instanceof ErrorResponse) {
                uiHandler.post(() -> resultListener.onDeviceResponseError(  ((ErrorResponse) resp).error()  ));
            } else if (resp.success() && resp instanceof SimpleSuccessResponse) {
                uiHandler.post(() -> resultListener.onSipleCommandComplete(  command.getCommandName()  ));
            } else if (resp.success() && resp instanceof FileSuccessResponse) {
                uiHandler.post(() -> resultListener.onNewFile(  currentDeviceFile = ((FileSuccessResponse) resp).getCurrentlySetFile()  ));
            } else if (resp.success() && resp instanceof StatusSuccessResponse) {
                uiHandler.post(() -> resultListener.onStatusUpdate( currentDeviceStatus = ((StatusSuccessResponse) resp).getDeviceStatus()  ));
            }

        } catch (IOException e) {
            e.printStackTrace();
            uiHandler.post(() -> {
                resultListener.onStatusUpdate(currentDeviceStatus);
                resultListener.onNewFile(currentDeviceFile);
                resultListener.onCommunicationError(e.getMessage());
            });
        }
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private DeviceStatus currentDeviceStatus;
    private DeviceFile currentDeviceFile;


    public interface OnCommandResultListener {
        void onStatusUpdate(DeviceStatus devStatus);
        void onNewFile(DeviceFile devFile);
        void onSipleCommandComplete(String commandName);
        void onDeviceResponseError(String reason);
        void onCommunicationError(String error);
    }

}
