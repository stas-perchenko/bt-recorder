package com.alperez.bt_microphone.core.response;

import com.alperez.bt_microphone.core.DeviceFile;
import com.alperez.bt_microphone.core.DeviceStatus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stas on 26.03.2017.
 */

public class ResponseParser {

    public static BaseResponse parseResponse(JSONObject jResponse) throws JSONException {
        return new Builder(jResponse).build();
    }





    private static class Builder {
        private int id;
        private boolean success;

        private String error;

        private DeviceStatus devStatus;
        private DeviceFile devFile;


        private Builder(JSONObject jResp) throws JSONException {
            id = jResp.getInt("id");
            success = checkSuccess(jResp);
            error = jResp.optString("error", null);
            JSONObject jStat = jResp.optJSONObject("status");
            if (jStat != null) {
                devStatus = DeviceStatusImpl.fromJson(jStat);
            }
            JSONObject jFile = jResp.optJSONObject("file");
            if (jFile != null) {
                devFile = DeviceFileImpl.fromJson(jFile);
            }
        }


        private boolean checkSuccess(JSONObject jObj) throws JSONException {
            String str = jObj.optString("answer", "ok");
            if ("ok".equalsIgnoreCase(str)) {
                return true;
            } else if ("error".equalsIgnoreCase(str)) {
                return false;
            } else {
                throw new JSONException("Bad value for 'answer' - "+str);
            }
        }


        public BaseResponse build() {
            if (!success) {
                return new ErrorResponse() {
                    @Override
                    public String error() {
                        return error;
                    }

                    @Override
                    public int sequenceNumber() {
                        return id;
                    }

                    @Override
                    public boolean success() {
                        return false;
                    }
                };
            } else {
                if (devStatus != null) {
                    return new StatusSuccessResponse() {
                        @Override
                        public DeviceStatus getDeviceStatus() {
                            return devStatus;
                        }

                        @Override
                        public int sequenceNumber() {
                            return id;
                        }

                        @Override
                        public boolean success() {
                            return true;
                        }
                    };

                } else if (devFile != null) {
                    return new FileSuccessResponse() {
                        @Override
                        public DeviceFile getCurrentlySetFile() {
                            return devFile;
                        }

                        @Override
                        public int sequenceNumber() {
                            return id;
                        }

                        @Override
                        public boolean success() {
                            return true;
                        }
                    };

                } else {
                    return new SimpleSuccessResponse() {
                        @Override
                        public int sequenceNumber() {
                            return id;
                        }

                        @Override
                        public boolean success() {
                            return true;
                        }
                    };

                }
            }
        }
    }
}
