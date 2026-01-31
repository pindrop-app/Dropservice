package com.pindrop.loopservice.util;

public class KeyUtil {

    public static String loopPk(String loopId) {
        return "LOOP#" + loopId;
    }

    public static String userPk(String userId) {
        return "USER#" + userId;
    }

    public static String memberSk(String userId) {
        return "MEMBER#USER#" + userId;
    }

    public static String userLoopSk(String loopId) {
        return "LOOP#" + loopId;
    }

    public static String invitePk(String email) {
        return "INVITE#" + email;
    }
}

