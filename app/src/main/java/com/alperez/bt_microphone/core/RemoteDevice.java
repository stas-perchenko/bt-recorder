package com.alperez.bt_microphone.core;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alperez.bt_microphone.GlobalConstants;
import com.alperez.bt_microphone.bluetoorh.connector.data.BtDataTransceiver;
import com.alperez.bt_microphone.model.ValidDeviceDbModel;
import com.alperez.bt_microphone.rest.OnCompleteListener;
import com.alperez.bt_microphone.rest.command.BaseRestCommand;
import com.alperez.bt_microphone.rest.command.impl.CurrFileRestCommand;
import com.alperez.bt_microphone.rest.command.impl.FastForwardRestCommand;
import com.alperez.bt_microphone.rest.command.impl.FastReverseRestCommand;
import com.alperez.bt_microphone.rest.command.impl.FormatRestCommand;
import com.alperez.bt_microphone.rest.command.impl.GainDownRestCommand;
import com.alperez.bt_microphone.rest.command.impl.GainUpRestCommand;
import com.alperez.bt_microphone.rest.command.impl.NextFileRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PauseRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PhantomOffRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PhantomOnRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PlayRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PowerOffRestCommand;
import com.alperez.bt_microphone.rest.command.impl.PrevFileRestCommand;
import com.alperez.bt_microphone.rest.command.impl.RecordRestCommand;
import com.alperez.bt_microphone.rest.command.impl.SetSampleRateRestCommand;
import com.alperez.bt_microphone.rest.command.impl.SetTimeRestCommand;
import com.alperez.bt_microphone.rest.command.impl.StatusRestCommand;
import com.alperez.bt_microphone.rest.command.impl.StopRestCommand;
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
    public static final String TAG = "Remote-Device";

    private ValidDeviceDbModel mDevice;
    private int commandTimeout;

    private volatile boolean released;

    private CommandPool mCommandPool = new CommandPool();
    private Executor commandExecutor;

    private OnCommandResultListener resultListener;

    public RemoteDevice(ValidDeviceDbModel device, int commandTimeout, @NonNull OnCommandResultListener resultListener) {
        this.mDevice = device;
        this.commandTimeout = commandTimeout;
        this.resultListener = resultListener;

        commandExecutor = Executors.newSingleThreadExecutor();
        /*commandStatus();
        commandVersion();
        commandCurrentFile();*/
    }


    public void release() {
        released = true;
        if (commandExecutor instanceof ExecutorService) {
            ((ExecutorService) commandExecutor).shutdownNow();
        }
        mCommandPool.releaseAllCommands();
        mDevice.releaseDataTransceiver();
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

    public void commandFormat(@NonNull OnCompleteListener callback) {
        try {
            BaseRestCommand comm = mCommandPool.getCommand(FormatRestCommand.class.getName());
            commandExecutor.execute(new CommandHandlerWithCallback(comm, callback, GlobalConstants.FORMAT_COMMAND_TIMEOUT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandPowerOff() {
        scheduleNoParamCommand(PowerOffRestCommand.class);
    }

    public void commandSetTime(Date time) {
        try {
            SetTimeRestCommand comm = (SetTimeRestCommand) mCommandPool.getCommand(SetTimeRestCommand.class.getName());
            comm.setTime(time);
            commandExecutor.execute(new CommandHandler(comm, commandTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandSetLocation(Location location) {
        //TODO Implement later !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public void commandSetSampleRate(int sampleRate) {
        try {
            SetSampleRateRestCommand comm = (SetSampleRateRestCommand) mCommandPool.getCommand(SetSampleRateRestCommand.class.getName());
            comm.setSampleRate(sampleRate);
            commandExecutor.execute(new CommandHandler(comm, commandTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandGainUp() {
        scheduleNoParamCommand(GainUpRestCommand.class);
    }

    public void commandGainDown() {
        scheduleNoParamCommand(GainDownRestCommand.class);
    }

    public void commandFastForward(int nSeconds) {
        try {
            FastForwardRestCommand comm = (FastForwardRestCommand) mCommandPool.getCommand(FastForwardRestCommand.class.getName());
            comm.setSeconds(nSeconds);
            commandExecutor.execute(new CommandHandler(comm, commandTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandFastReverse(int nSeconds) {
        try {
            FastReverseRestCommand comm = (FastReverseRestCommand) mCommandPool.getCommand(FastReverseRestCommand.class.getName());
            comm.setSeconds(nSeconds);
            commandExecutor.execute(new CommandHandler(comm, commandTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandPhantomOnOff(boolean enabled) {
        if (enabled) {
            scheduleNoParamCommand(PhantomOnRestCommand.class);
        } else {
            scheduleNoParamCommand(PhantomOffRestCommand.class);
        }
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
                comm = ((Class<BaseRestCommand>) Class.forName(className)).getConstructor(BtDataTransceiver.class).newInstance(mDevice.getDataTransceiver());
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
            onExecuteCommandSynchronously(command, null, timeoutMillis);
        }
    }

    private class CommandHandlerWithCallback implements Runnable {
        private final BaseRestCommand command;
        private OnCompleteListener callback;
        private final int timeoutMillis;

        public CommandHandlerWithCallback(BaseRestCommand command, OnCompleteListener callback, int timeoutMillis) {
            this.command = command;
            this.callback = callback;
            this.timeoutMillis = timeoutMillis;
        }


        @Override
        public void run() {
            onExecuteCommandSynchronously(command, callback, timeoutMillis);
        }
    }


    private void onExecuteCommandSynchronously(BaseRestCommand command, OnCompleteListener callback, int timeoutMillis) {
        try {
            Log.d(TAG, "====> Send command: "+command.getCommandName());
            BaseResponse resp = command.sendBlocked(timeoutMillis);

            Log.i(TAG, "----> Command response: ");

            if (callback == null) {
                if (!resp.success() && resp instanceof ErrorResponse) {
                    uiHandler.post(() -> resultListener.onDeviceResponseError(command.getClass(), ((ErrorResponse) resp).error()));
                } else if (resp.success() && resp instanceof SimpleSuccessResponse) {
                    uiHandler.post(() -> resultListener.onSimpleCommandComplete(command.getCommandName()));
                } else if (resp.success() && resp instanceof FileSuccessResponse) {
                    uiHandler.post(() -> resultListener.onNewFile(currentDeviceFile = ((FileSuccessResponse) resp).getCurrentlySetFile()));
                } else if (resp.success() && resp instanceof StatusSuccessResponse) {
                    uiHandler.post(() -> resultListener.onStatusUpdate(currentDeviceStatus = ((StatusSuccessResponse) resp).getDeviceStatus()));
                }
            } else {
                if (!resp.success() && resp instanceof ErrorResponse) {
                    uiHandler.post(() -> callback.onError(  ((ErrorResponse) resp).error() ));
                } else {
                    uiHandler.post(() -> callback.onComplete());
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "~~~~> Commend error - "+e.getMessage(), e);
            e.printStackTrace();
            if (callback == null) {
                uiHandler.post(() -> {
                    if (currentDeviceStatus != null) resultListener.onStatusUpdate(currentDeviceStatus);
                    if (currentDeviceFile != null) resultListener.onNewFile(currentDeviceFile);
                    resultListener.onCommunicationError(command.getClass(), e.getMessage());
                });
            } else {
                uiHandler.post(() -> callback.onError(  e.getMessage() ));
            }
        }
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private DeviceStatus currentDeviceStatus;
    private DeviceFile currentDeviceFile;


    public interface OnCommandResultListener {
        void onStatusUpdate(DeviceStatus devStatus);
        void onNewFile(DeviceFile devFile);
        void onSimpleCommandComplete(String commandName);
        void onDeviceResponseError(Class<? extends BaseRestCommand> commandClass, String reason);
        void onCommunicationError(Class<? extends BaseRestCommand> commandClass, String error);
    }

}
