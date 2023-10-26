package com.mzbr.videoeditingservice.service;import java.io.File;import java.io.FileOutputStream;import java.io.IOException;import java.io.OutputStreamWriter;import java.nio.charset.StandardCharsets;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.ArrayList;import java.util.List;import java.util.StringJoiner;import org.springframework.stereotype.Service;import com.github.kokorin.jaffree.ffmpeg.*;import com.mzbr.videoeditingservice.model.Audio;import com.mzbr.videoeditingservice.model.Clip;import com.mzbr.videoeditingservice.model.Subtitle;import com.mzbr.videoeditingservice.model.VideoEntity;import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;@Service@Slf4j@RequiredArgsConstructorpublic class VideoEditingServiceImpl implements VideoEditingService {	private static final String ASS_HEADER =		"[Script Info]\n" +			"ScriptType: v4.00+\n" +			"WrapStyle: 0\n" +			"ScaledBorderAndShadow: yes\n" +			"YCbCr Matrix: TV.601\n" +			"\n" +			"[V4+ Styles]\n" +			"Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n"			+			"Style: Default,Pretendard,20,&H00FFFFFF,&H0000FFFF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,0,0,5,10,10,10,0\n"			+			"\n" +			"[Events]\n" +			"Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n";	@Override	public String processVideo(VideoEntity videoEntity, int width, int height) throws Exception {		return null;	}	@Override	public List<Input> prepareVideoInputs(List<Clip> clips) throws Exception {		List<Input> inputs = new ArrayList<>();		return null;	}	@Override	public String generateVideoCropAndLayoutFilter(List<Clip> clips, Integer scaleX, Integer scaleY) throws Exception {		StringJoiner filterJoiner = new StringJoiner(";");		for (int i = 0; i < clips.size(); i++) {			Clip clip = clips.get(i);			StringBuilder baseFilter = new StringBuilder();			baseFilter.append(String.format("[%d:v]setpts=PTS-STARTPTS", i));			if (clip.getCrop() != null) {				Integer newWidth = (int)(clip.getWidth() / clip.getCrop().getZoomFactor());				Integer newHeight = (int)(clip.getHeight() / clip.getCrop().getZoomFactor());				baseFilter.append(String.format(",crop=%d:%d:%d:%d",					newWidth, newHeight, clip.getCrop().getStartX(), clip.getCrop().getStartY()));			}			baseFilter.append(String.format(",scale=%d:%d", scaleX, scaleY));			filterJoiner.add(baseFilter + String.format("[v%d]", i));		}		return filterJoiner.toString();	}	@Override	public String generateAudioVolumeFilter(List<Clip> clips) throws Exception {		StringJoiner filterJoiner = new StringJoiner(";");		for (int i = 0; i < clips.size(); i++) {			Clip clip = clips.get(i);			if (clip.getVolume() != null) { // 불륨 조정이 필요한 경우에만 필터를 추가합니다.				filterJoiner.add(String.format("[%d:a]volume=%.2f[a%d]", i, clip.getVolume(), i));			}		}		return filterJoiner.toString();	}	@Override	public String generateConcatVideoFilter(Integer clipCount) {		StringBuilder filterBuilder = new StringBuilder();		for (int i = 0; i < clipCount; i++) {			filterBuilder.append(String.format("[v%d][a%d]", i, i));		}		filterBuilder.append(String.format("concat=n=%d:v=1:a=1[v_concat][outa];", clipCount));		filterBuilder.append("[outa][2:a]amix=inputs=2:duration=first[a_final]");		return filterBuilder.toString();	}	@Override	public Input insertAudioToVideo(Audio audio) throws Exception {		return null;	}	@Override	public String generateASSBySubtitles(List<Subtitle> subtitles, String fileName) throws Exception {		if (subtitles.size() == 0) {			return "empty";		}		String assContent = createAssStringBySubtitles(subtitles);		File tempFile = File.createTempFile(fileName, ".ass");		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {			writer.write(assContent);		}		return tempFile.getAbsolutePath();	}	protected String modifyWindowPathForFfmpeg(String originalPath) {		String newPath = originalPath.replace("\\", "/");		newPath = newPath.replace(":", "\\\\:");		return newPath;	}	private String createAssStringBySubtitles(List<Subtitle> subtitles) {		StringBuilder assContent = new StringBuilder(ASS_HEADER);		for (Subtitle subtitle : subtitles) {			String startTime = millisecondsToTimeCode(subtitle.getStartTime());			String endTime = millisecondsToTimeCode(subtitle.getEndTime());			String position = subtitle.getPositionX() + "," + subtitle.getPositionY();			float adjustedFontSize = 20.0f * subtitle.getScale();			String colorInAssFormat = convertToASSColor(subtitle.getColor());			String text = "{\\pos(" + position + ")\\fs" + adjustedFontSize + "\\c" + colorInAssFormat + "}" + subtitle.getText();			assContent.append(				"Dialogue: " + subtitle.getZIndex() + "," + startTime + "," + endTime + ",Default,,0,0,0,," + text					+ "\n");		}		return assContent.toString();	}	private String convertToASSColor(Integer color) {		// Assuming color is in format 0xRRGGBB		int r = (color & 0xFF0000) >> 16;		int g = (color & 0x00FF00) >> 8;		int b = (color & 0x0000FF);		return String.format("&H%02X%02X%02X&", b, g, r);	}	private String millisecondsToTimeCode(Integer ms) {		int millis = ms % 1000;		int second = (ms / 1000) % 60;		int minute = (ms / (1000 * 60)) % 60;		int hour = ms / (1000 * 60 * 60);		return String.format("%1$01d:%2$02d:%3$02d.%4$02d", hour, minute, second, millis / 10);	}	@Override	public void splitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec, String filter, String outputPath) throws Exception {		fFmpeg.addOutput(UrlOutput.toPath(Path.of(outputPath))				.addArguments("-filter_complex", filter)				.addArguments("-r", "30")				.addArguments("-g", "5")				.addArguments("-f", "segment")				.addArguments("-segment_time", String.valueOf(perSegmentSec))				.addArguments("-segment_time_delta", "0.05")				.addArguments("-reset_timestamps", "1")				.addArguments("-map", "[outv]")				.addArguments("-map", "[a_final]"))			.execute();	}	@Override	public void uploadTempFileToS3(List<String> fileLocations) throws Exception {	}	@Override	public void deleteTemporaryFile(String uuid, String assPath) throws Exception {		String projectRootPath = System.getProperty("user.dir");		Path directory = Paths.get(projectRootPath);		int index = 0;		while (true) {			Path filePath = directory.resolve(String.format("%s[%03d].mov",uuid, index));			if (Files.exists(filePath)) {				try {					Files.delete(filePath);					log.info("Deleted file: {}",filePath);				} catch (IOException e) {					log.info("File delete End.");				}			} else {				break;			}			index++;		}		try {			Files.delete(Path.of(assPath));		}catch (IOException e){			log.info("자막 파일이 없습니다.");		}	}}