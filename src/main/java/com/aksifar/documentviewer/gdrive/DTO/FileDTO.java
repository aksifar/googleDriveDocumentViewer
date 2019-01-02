package com.aksifar.documentviewer.gdrive.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class FileDTO {

	private String id;
	private String name;
	private String contentType;
	private String path;
	private String parent;
}
