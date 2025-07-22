package com.example.livestream.model;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class Broadcast {
    private String id;
    private String broadcasterId;
    private String title;
    private String description;
    private String artGroupNm;
    private String artGroupNo;
    private String userId;
    private int comuProfileNo;
    private int chatChannelNo;
    private boolean isLive;
    private String liveCategory;
    private String liveQty;
    private String liveThmimgUrl;
    private transient Set<String> viewers = ConcurrentHashMap.newKeySet();
    private Date liveStartDate;
}