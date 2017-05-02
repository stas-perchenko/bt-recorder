package com.alperez.bt_microphone;

import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class GlobalConstants {
    public static final String DB_NAME = "bt_recorder";
    public static final int DB_VERSION = 3;


    public static final UUID UUID_SERVICE_1_1 = UUID.fromString("11111111-eb38-49a2-9a7b-679e52aeb3d6");

    //public static final UUID UUID_SERVICE_1 = UUID.fromString("d6b3ae52-9e67-7b9a-a249-38eb11111111");
    public static final UUID UUID_SERVICE_1 = UUID.fromString("11111111-eb38-49a2-9a7b-679e52aeb3d6");

    public static final UUID UUID_SERVICE_2 = UUID.fromString("22222222-EB38-49A2-45D0-679E52AEB3D6");


    public static final UUID[] ALL_SERVICE_UUIDS = {UUID_SERVICE_1, UUID_SERVICE_2};

    public static final int MAX_TIME_FOR_SDP = 10000;
    public static final int MAX_TIME_AFTER_LAST_UUID_DISCOVERED = 4000;


    public static final int GENERAL_COMMAND_TIMEOUT = 2600;
    public static final int FORMAT_COMMAND_TIMEOUT = 16000;
    public static final int POWEROFF_COMMAND_TIMEOUT = 5000;



    public static final int SAMPLE_RATE_48K = 48000;
    public static final int SAMPLE_RATE_96K = 96000;
    public static final int SAMPLE_RATE_192K = 192000;
}
