package com.alperez.bt_microphone;

import java.util.UUID;

/**
 * Created by stanislav.perchenko on 3/9/2017.
 */

public class GlobalConstants {
    public static final String DB_NAME = "bt_recorder";
    public static final int DB_VERSION = 3;


    //public static final UUID UUID_SERVICE_1 = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //public static final UUID UUID_SERVICE_2 = UUID.fromString("6705ef4c-eb38-49a2-9a7b-679e065ab3d6");

    public static final UUID UUID_SERVICE_1 = UUID.fromString("11111111-eb38-49a2-9a7b-679e52aeb3d6");
    //public static final UUID UUID_SERVICE_2 = UUID.fromString("22222222-eb38-49a2-45d0-679e52aeb3d6");

    public static final UUID[] ALL_SERVICE_UUIDS = {UUID_SERVICE_1/*, UUID_SERVICE_2*/};

    public static final int MAX_TIME_FOR_SDP = 10000;
    public static final int MAX_TIME_AFTER_LAST_UUID_DISCOVERED = 4000;
}
