package com.jasperwong.smartbicycle.util;

/**
 * Created by JasperWong on 2016/8/24.
 */
public class parser {
    public int status;
    public int data_guide;
    public int data_distance;

    enum state_parser{
        STATE_WAIT_G,
        STATE_WAIT_GUIDE_DATA,
        STATE_WAIT_D,
        STATE_WAIT_DISTANCE_DATA,
        //		STATE_WAIT_CHECKSUM,
        STATE_WAIT_NEWLINE,
        STATE_PARSE_FINISH_PENDING,
    };

}
