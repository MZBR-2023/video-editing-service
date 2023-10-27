package com.mzbr.videoeditingservice.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.ffmpeg.*;
import com.mzbr.videoeditingservice.model.Audio;
import com.mzbr.videoeditingservice.model.Clip;
import com.mzbr.videoeditingservice.model.Subtitle;
import com.mzbr.videoeditingservice.model.VideoEntity;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEditingServiceImpl implements VideoEditingService {

	protected final S3Util s3Util;

	private static final String ASS_HEADER =
		"[Script Info]\n" +
			"ScriptType: v4.00+\n" +
			"WrapStyle: 0\n" +
			"ScaledBorderAndShadow: yes\n" +
			"YCbCr Matrix: TV.601\n" +
			"\n" +
			"[V4+ Styles]\n" +
			"Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n"
			+
			"Style: Default,Pretendard,20,&H00FFFFFF,&H0000FFFF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,0,0,5,10,10,10,0\n"
			+
			"\n" +
			"[Events]\n" +
			"Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n";

	@Override
	public String processVideo(VideoEntity videoEntity, int width, int height, String folderPath) throws Exception {

		//출력 이름 지정
		String outputPath = videoEntity.getVideoUuid() + "[%03d].mov";

		FFmpeg fFmpeg = FFmpeg.atPath();

		//자막파일 생성
		String assPath = generateASSBySubtitles(videoEntity.getSubtitles(), videoEntity.getVideoUuid());

		//콘텐츠 주입
		inputContents(videoEntity).forEach(input -> fFmpeg.addInput(input));

		//필터 생성
		StringBuilder filter = generateFilter(videoEntity, width, height, assPath);

		//비디오 생성
		excuteSplitVideoIntoSegments(fFmpeg, 5, filter.toString(), outputPath);

		//비디오 경로리스트 생성
		List<Path> pathList = getSegementPathList(videoEntity.getVideoUuid());

		//생성 비디오 s3에 업로드
		uploadTempFileToS3(pathList, folderPath + videoEntity.getVideoUuid());

		//임시 파일 삭제
		deleteTemporaryFile(pathList, assPath);

		return null;
	}

	private StringBuilder generateFilter(VideoEntity videoEntity, int width, int height, String assPath) throws
		Exception {
		StringBuilder filter = new StringBuilder();
		filter.append(generateVideoCropAndLayoutFilter(videoEntity.getClips(), width, height)).append(";");
		filter.append(generateVideoVolumeFilter(videoEntity.getClips())).append(";");
		if (videoEntity.hasAudio()) {
			filter.append(generateAudioFilter(videoEntity.getAudio(), videoEntity.getTotalDuration(),
				videoEntity.getClips().size())).append(";");
		}
		filter.append(generateConcatVideoFilter(videoEntity.getClips().size(), videoEntity.hasAudio())).append(";");

		filter.append("[v_concat]ass=").append(modifyWindowPathForFfmpeg(assPath)).append("[outv]");
		return filter;
	}

	private List<Input> inputContents(VideoEntity videoEntity) throws Exception {
		List<Input> inputs = prepareVideoInputs(videoEntity.getClips());

		if (videoEntity.hasAudio()) {
			inputs.add(insertAudioToVideo(videoEntity.getUserUploadAudioEntity()));
		}
		return inputs;

	}

	@Override
	public List<Input> prepareVideoInputs(List<Clip> clips) throws Exception {
		List<Input> inputs = new ArrayList<>();
		Map<Long, String> presignMap = s3Util.getClipsPresignedUrlMap(clips);

		List<Clip> sortedClipList = clips.stream().sorted(Comparator.comparing(Clip::getId)).collect(
			Collectors.toList());

		for (Clip clip : sortedClipList) {
			inputs.add(UrlInput.fromUrl("\"" + presignMap.get(clip.getId()) + "\""));
		}
		return inputs;
	}

	@Override
	public String generateVideoCropAndLayoutFilter(List<Clip> clips, Integer scaleX, Integer scaleY) throws Exception {
		StringJoiner filterJoiner = new StringJoiner(";");
		for (int i = 0; i < clips.size(); i++) {
			Clip clip = clips.get(i);
			StringBuilder baseFilter = new StringBuilder();
			baseFilter.append(String.format("[%d:v]setpts=PTS-STARTPTS", i));

			if (clip.getCrop() != null) {
				Integer newWidth = (int)(clip.getWidth() / clip.getCrop().getZoomFactor());
				Integer newHeight = (int)(clip.getHeight() / clip.getCrop().getZoomFactor());
				baseFilter.append(String.format(",crop=%d:%d:%d:%d",
					newWidth, newHeight, clip.getCrop().getStartX(), clip.getCrop().getStartY()));
			}
			baseFilter.append(String.format(",scale=%d:%d", scaleX, scaleY));
			filterJoiner.add(baseFilter + String.format("[v%d]", i));
		}
		return filterJoiner.toString();
	}

	@Override
	public String generateVideoVolumeFilter(List<Clip> clips) throws Exception {
		StringJoiner filterJoiner = new StringJoiner(";");
		for (int i = 0; i < clips.size(); i++) {
			Clip clip = clips.get(i);
			if (clip.getVolume() != null) {
				filterJoiner.add(String.format("[%d:a]volume=%.2f[a%d]", i, clip.getVolume(), i));
			}
		}
		return filterJoiner.toString();
	}

	@Override
	public String generateAudioFilter(Audio audio, int totalDurationTime, int clipCount) throws Exception {
		return String.format("[%d:a]volume=%.2f,atrim=start=%f:duration=%f[a_special]", clipCount, audio.getVolume(),
			(float)audio.getStartTime() / 1000, (float)totalDurationTime / 1000);
	}

	@Override
	public String generateConcatVideoFilter(Integer clipCount, boolean hasAudio) {
		StringBuilder filterBuilder = new StringBuilder();
		for (int i = 0; i < clipCount; i++) {
			filterBuilder.append(String.format("[v%d][a%d]", i, i));
		}

		if (hasAudio) {
			filterBuilder.append(String.format("concat=n=%d:v=1:a=1[v_concat][a_concat];", clipCount));
			filterBuilder.append("[a_concat][a_special]amix=inputs=2:duration=first[outa]");
			return filterBuilder.toString();
		}
		filterBuilder.append(String.format("concat=n=%d:v=1:a=1[v_concat][outa];", clipCount));

		return filterBuilder.toString();
	}

	@Override
	public Input insertAudioToVideo(Audio audio) throws Exception {
		return UrlInput.fromUrl("\"" + s3Util.getPresigndUrl(audio.getUrl()) + "\"");
	}

	@Override
	public String generateASSBySubtitles(List<Subtitle> subtitles, String fileName) throws Exception {
		if (subtitles.size() == 0) {
			return "empty";
		}
		String assContent = createAssStringBySubtitles(subtitles);
		File tempFile = File.createTempFile(fileName, ".ass");

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile),
			StandardCharsets.UTF_8)) {
			writer.write(assContent);
		}

		return tempFile.getAbsolutePath();
	}

	protected String modifyWindowPathForFfmpeg(String originalPath) {
		String newPath = originalPath.replace("\\", "/");
		newPath = newPath.replace(":", "\\\\:");
		return newPath;
	}

	protected List<Path> getSegementPathList(String uuid) {
		List<Path> result = new ArrayList<>();
		String projectRootPath = System.getProperty("user.dir");
		Path directory = Paths.get(projectRootPath);
		int index = 0;

		while (true) {
			Path filePath = directory.resolve(String.format("%s[%03d].mov", uuid, index));
			if (Files.exists(filePath)) {
				result.add(filePath);
			} else {
				break;
			}
			index++;
		}
		return result;
	}

	private String createAssStringBySubtitles(List<Subtitle> subtitles) {
		StringBuilder assContent = new StringBuilder(ASS_HEADER);

		for (Subtitle subtitle : subtitles) {
			String startTime = millisecondsToTimeCode(subtitle.getStartTime());
			String endTime = millisecondsToTimeCode(subtitle.getEndTime());
			String position = subtitle.getPositionX() + "," + subtitle.getPositionY();

			float adjustedFontSize = 20.0f * subtitle.getScale();
			String colorInAssFormat = convertToASSColor(subtitle.getColor());
			String text =
				"{\\pos(" + position + ")\\fs" + adjustedFontSize + "\\c" + colorInAssFormat + "}" + subtitle.getText();

			assContent.append(
				"Dialogue: " + subtitle.getZIndex() + "," + startTime + "," + endTime + ",Default,,0,0,0,," + text
					+ "\n");
		}
		return assContent.toString();
	}

	private String convertToASSColor(Integer color) {
		// Assuming color is in format 0xRRGGBB
		int r = (color & 0xFF0000) >> 16;
		int g = (color & 0x00FF00) >> 8;
		int b = (color & 0x0000FF);
		return String.format("&H%02X%02X%02X&", b, g, r);
	}

	private String millisecondsToTimeCode(Integer ms) {
		int millis = ms % 1000;
		int second = (ms / 1000) % 60;
		int minute = (ms / (1000 * 60)) % 60;
		int hour = ms / (1000 * 60 * 60);

		return String.format("%1$01d:%2$02d:%3$02d.%4$02d", hour, minute, second, millis / 10);
	}

	@Override
	public void excuteSplitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec, String filter, String outputPath) throws
		Exception {
		fFmpeg.addOutput(UrlOutput.toPath(Path.of(outputPath))
				.addArguments("-filter_complex", filter)
				.addArguments("-r", "30")
				.addArguments("-g", "5")
				.addArguments("-f", "segment")
				.addArguments("-segment_time", String.valueOf(perSegmentSec))
				.addArguments("-segment_time_delta", "0.05")
				.addArguments("-reset_timestamps", "1")
				.addArguments("-map", "[outv]")
				.addArguments("-map", "[outa]"))
			.execute();
	}

	@Override
	public void uploadTempFileToS3(List<Path> pathList, String folderName) throws Exception {
		s3Util.uploadLocalFileByStringFormat(pathList, folderName);
	}

	@Override
	public void deleteTemporaryFile(List<Path> pathList, String assPath) throws Exception {
		for (Path path : pathList) {
			try {
				Files.delete(path);
				log.info("Deleted file: {}", path);
			} catch (IOException e) {
				log.info("File delete fail.");
			}
		}

		try {
			Files.delete(Path.of(assPath));
		} catch (IOException e) {
			log.info("자막 파일이 없습니다.");
		}
	}

}
