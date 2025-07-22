package com.example.livestream.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewerInfo {

	private String viewerId;
	private String broadcastId;
}
