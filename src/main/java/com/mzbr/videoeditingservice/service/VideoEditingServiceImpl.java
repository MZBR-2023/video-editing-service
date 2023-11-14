package com.mzbr.videoeditingservice.service;

import java.io.BufferedWriter;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.kokorin.jaffree.ffmpeg.*;
import com.mzbr.videoeditingservice.component.SubtitleHeader;
import com.mzbr.videoeditingservice.dto.TempPreviewDto;
import com.mzbr.videoeditingservice.dto.VideoEditingRequestDto;
import com.mzbr.videoeditingservice.enums.EncodeFormat;
import com.mzbr.videoeditingservice.exception.MemberException;
import com.mzbr.videoeditingservice.model.entity.audio.Audio;
import com.mzbr.videoeditingservice.model.entity.Clip;
import com.mzbr.videoeditingservice.model.entity.HashTag;
import com.mzbr.videoeditingservice.model.entity.Member;
import com.mzbr.videoeditingservice.model.entity.Store;
import com.mzbr.videoeditingservice.model.entity.Subtitle;
import com.mzbr.videoeditingservice.model.entity.TempPreview;
import com.mzbr.videoeditingservice.model.entity.TempVideo;
import com.mzbr.videoeditingservice.model.entity.audio.UserUploadAudio;
import com.mzbr.videoeditingservice.model.entity.VideoData;
import com.mzbr.videoeditingservice.model.document.VideoEncodingDynamoTable;
import com.mzbr.videoeditingservice.model.entity.Video;
import com.mzbr.videoeditingservice.model.entity.VideoHash;
import com.mzbr.videoeditingservice.model.entity.VideoSegment;
import com.mzbr.videoeditingservice.repository.ClipRepository;
import com.mzbr.videoeditingservice.repository.HashTagRepository;
import com.mzbr.videoeditingservice.repository.MemberRepository;
import com.mzbr.videoeditingservice.repository.StoreRepository;
import com.mzbr.videoeditingservice.repository.SubtitleRepository;
import com.mzbr.videoeditingservice.repository.TempPreviewRepository;
import com.mzbr.videoeditingservice.repository.TempVideoRepository;
import com.mzbr.videoeditingservice.repository.UserUploadAudioRepository;
import com.mzbr.videoeditingservice.repository.VideoDataRepository;
import com.mzbr.videoeditingservice.repository.VideoHashRepository;
import com.mzbr.videoeditingservice.repository.VideoRepository;
import com.mzbr.videoeditingservice.repository.VideoSegmentRepository;
import com.mzbr.videoeditingservice.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class VideoEditingServiceImpl implements VideoEditingService {

	protected final S3Util s3Util;
	protected final SubtitleHeader subtitleHeader;
	private static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");

	@Value("${encoded-folder.prefix}")
	private String ENCODED_FOLDER;

	@Value("${cloud.aws.url}")
	private String S3_URL;

	private final VideoSegmentRepository videoSegmentRepository;
	private final VideoRepository videoRepository;
	private final DynamoService dynamoService;
	private final EncodingKinesisService encodingKinesisService;
	private final EditingKinesisProduceService editingKinesisProduceService;
	private final TempVideoRepository tempVideoRepository;
	private final TempPreviewRepository tempPreviewRepository;
	private final MemberRepository memberRepository;
	private final ClipRepository clipRepository;
	private final SubtitleRepository subtitleRepository;
	private final UserUploadAudioRepository userUploadAudioRepository;
	private final StoreRepository storeRepository;
	private final VideoDataRepository videoDataRepository;
	private final VideoHashRepository videoHashRepository;
	private final HashTagRepository hashTagRepository;

	@Override
	public void videoProcessStart(Integer memberId, String videoUuid) {
		Member member = memberRepository.findById(memberId).orElseThrow();
		Video videoEntity = Video.builder()
			.videoUuid(videoUuid)
			.member(member)
			.build();
		videoRepository.save(videoEntity);
	}

	@Transactional
	@Override
	public void videoEditing(VideoEditingRequestDto videoEditingRequestDto, Integer memberId) {
		Video videoEntity = videoRepository.findByVideoUuid(videoEditingRequestDto.getVideoUuid()).orElseThrow();
		Store store = storeRepository.findById(videoEditingRequestDto.getStoreId()).orElseThrow();
		if (videoEntity.getMember().getId() != memberId) {
			throw new MemberException("사용자의 엔티티가 아닙니다.");
		}
		videoEntity.storeRegister(store);

		List<Clip> clips = new ArrayList<>();
		for (VideoEditingRequestDto.uploadClip userClip : videoEditingRequestDto.getClips()) {
			clips.add(Clip.builder()
				.volume(userClip.getVolume())
				.url(tempVideoRepository.findByVideoName(userClip.getFileName()).getAfterCropUrl())
				.videoEntity(videoEntity)
				.build());
		}
		clipRepository.saveAll(clips);

		videoEntity.updateSegmentCount(clips.size());

		List<Subtitle> subtitles = new ArrayList<>();
		for (VideoEditingRequestDto.uploadSubtitle userSubtitle : videoEditingRequestDto.getSubtitles()) {
			subtitles.add(Subtitle.builder()
				.color(userSubtitle.getColor())
				.positionX(userSubtitle.getX())
				.positionY(userSubtitle.getY())
				.startTime((int)(userSubtitle.getStartDuration() * 1000))
				.endTime((int)(userSubtitle.getEndDuration() * 1000))
				.rotation(userSubtitle.getRotation())
				.scale(userSubtitle.getScale())
				.text(userSubtitle.getText())
				.videoEntity(videoEntity)
				.build());
		}
		subtitleRepository.saveAll(subtitles);

		userUploadAudioRepository.save(UserUploadAudio.builder()
			.url("audio/" + videoEditingRequestDto.getAudio().getFileName())
			.volume(videoEditingRequestDto.getAudio().getVolume())
			.videoEntity(videoEntity)
			.build());

		videoDataRepository.save(VideoData.builder()
			.description(videoEditingRequestDto.getDescription())
			.star(videoEditingRequestDto.getStar())
			.thumbnailUrl("thumbnail/" + videoEditingRequestDto.getThumbnailName())
			.P144Url(ENCODED_FOLDER + "/" + videoEntity.getVideoUuid() + "/" + "P144.m3u8")
			.P360Url(ENCODED_FOLDER + "/" + videoEntity.getVideoUuid() + "/" + "P360.m3u8")
			.P480Url(ENCODED_FOLDER + "/" + videoEntity.getVideoUuid() + "/" + "P480.m3u8")
			.P720Url(ENCODED_FOLDER + "/" + videoEntity.getVideoUuid() + "/" + "P720.m3u8")
			.videoEntity(videoEntity)
			.build());

		List<VideoHash> videoHashes = new ArrayList<>();
		for (String tag : videoEditingRequestDto.getTags()) {
			Optional<HashTag> tempTag = hashTagRepository.findByName(tag);
			HashTag hashTag=null;
			if (tempTag.isPresent()) {
				hashTag = tempTag.get();
			} else if (tempTag.isEmpty()) {
				hashTag = hashTagRepository.save(HashTag.builder()
					.name(tag)
					.build());
			}
			videoHashes.add(VideoHash.builder()
				.hashTag(hashTag)
				.videoEntity(videoEntity)
				.build());
		}
		videoHashRepository.saveAll(videoHashes);

		//영상 편집 시작
		dynamoService.createVideoEditingNewDocument(videoEntity.getId(), videoEntity.getSegmentCount());
		editingKinesisProduceService.publishIdToKinesis(videoEntity.getId());
	}

	@Override
	public void processVideo(Long videoId, int width, int height, String folderPath) throws Exception {
		Video videoEntity = videoRepository.findById(videoId).orElseThrow();

		//출력 이름 지정
		String outputPath = "%03d.mov";

		FFmpeg fFmpeg = FFmpeg.atPath();

		//자막파일 생성
		Path assPath = generateASSBySubtitles(videoEntity.getSubtitles(), videoEntity.getVideoUuid());

		//콘텐츠 주입
		//영상의 url을 기반으로 s3에서 영상을 다운 받고 path 리스트를 만듦
		List<Path> inputPathList = new ArrayList<>();
		for (Clip clip : videoEntity.getClips()) {
			inputPathList.add(s3Util.getFileToLocalDirectory(clip.getUrl()));
		}
		if (videoEntity.hasAudio()) {
			inputPathList.add(s3Util.getFileToLocalDirectory(videoEntity.getAudio().getUrl()));
		}
		for (Path path : inputPathList) {
			fFmpeg.addInput(UrlInput.fromPath(path));
		}

		FilterGraph filter = generateFilter(videoEntity, width, height, assPath);

		//비디오 생성
		executeSplitVideoIntoSegments(fFmpeg, 5, filter, outputPath);

		//비디오 경로리스트 생성
		List<Path> pathList = getSegementPathList();

		//생성 비디오 s3에 업로드
		uploadTempFileToS3(pathList, folderPath + "/" + videoEntity.getVideoUuid());

		//DB에 비디오 세그먼트 데이터 저장
		persistAndSendVideoSegment(folderPath, videoEntity, pathList);

		//m3u8파일 제작 후 업로드
		createAndUploadM3U8(videoEntity, pathList.size());

		// 임시 파일 삭제
		pathList.addAll(inputPathList);
		pathList.add(assPath);
		deleteTemporaryFile(pathList);

	}

	@Override
	@Transactional
	public String tempVideoProcess(String videoName, String folderPath, Integer memberId) throws Exception {
		String uploadUrl;
		Path filePath = null;
		Path beforeEditFile = null;
		TempVideo tempVideo = tempVideoRepository.findByVideoName(videoName);
		if (tempVideo.getVideoEntity().getMember().getId() != memberId) {
			throw new MemberException("사용자의 엔티티가 아닙니다.");
		}

		String fileName = tempVideo.getVideoName();
		if (tempVideo.getTempCrop() == null) {
			tempVideo.updateAfterCropUrl(tempVideo.getOriginVideoUrl());
			return tempVideo.getAfterCropUrl();
		}
		FFmpeg fFmpeg = FFmpeg.atPath();
		try {
			beforeEditFile = s3Util.getFileToLocalDirectory(tempVideo.getOriginVideoUrl());
			fFmpeg.addInput(UrlInput.fromPath(beforeEditFile));
			fFmpeg.addOutput(UrlOutput.toPath(Path.of(fileName)));

			fFmpeg.addArguments("-vf", String.format("crop=%d:%d:%d:%d",
				tempVideo.getTempCrop().getWidth(),
				tempVideo.getTempCrop().getHeight(),
				tempVideo.getTempCrop().getX(),
				tempVideo.getTempCrop().getY()
			)).addArguments("-c:v", "libx264");

			fFmpeg.execute();

			filePath = Paths.get(CURRENT_WORKING_DIR + "/" + fileName);
			uploadUrl = s3Util.uploadLocalFile(filePath, folderPath + "/" + fileName);
			tempVideo.updateAfterCropUrl(uploadUrl);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				Files.delete(filePath);
				Files.delete(beforeEditFile);
			} catch (Exception e) {
				log.error(e.getMessage());

			}

		}

		return uploadUrl;
	}

	@Override
	public String processTempPreview(TempPreviewDto tempPreviewDto, Integer memberId) throws Exception {
		Optional<Member> member = memberRepository.findById(memberId);
		if (member.isEmpty()) {
			throw new MemberException("사용자의 엔티티가 아닙니다.");
		}

		String outputPath = tempPreviewDto.getVersionId() + ".mp4";

		//versionId를 확인하여 db에 있는지 확인
		Optional<TempPreview> tempPreviewCheck = tempPreviewRepository.findById(tempPreviewDto.getVersionId());

		//있으면 영상 url 반환
		if (tempPreviewCheck.isPresent()) {
			if (tempPreviewCheck.get().getMember().getId() != memberId) {
				throw new MemberException("사용자의 엔티티가 아닙니다.");
			}
			return tempPreviewCheck.get().getS3Url();
		}

		//없으면 영상 제작
		//비디오 UUID를 기준으로 비디오 리스트 생성
		List<TempVideo> videos = tempPreviewDto.getVideoNameList().stream()
			.map(name -> tempVideoRepository.findByVideoName(name))
			.collect(Collectors.toList());
		//영상의 url을 기반으로 s3에서 영상을 다운 받고 path 리스트를 만듦
		FFmpeg fFmpeg = FFmpeg.atPath();
		List<Path> videoPathList = new ArrayList<>();
		for (TempVideo video : videos) {
			videoPathList.add(s3Util.getFileToLocalDirectory(video.getAfterCropUrl()));
		}
		for (Path path : videoPathList) {
			fFmpeg.addInput(UrlInput.fromPath(path));
		}

		FilterGraph filterGraph = new FilterGraph();

		for (int i = 0; i < videoPathList.size(); i++) {
			filterGraph.addFilterChain(FilterChain.of(
				Filter.withName("setpts").addArgument("PTS-STARTPTS").addInputLink(i + ":v").addOutputLink("vt" + i),
				Filter.withName("scale")
					.addArgument("width", "720")
					.addArgument("height", "1280")
					.addInputLink("vt" + i)
					.addOutputLink("v" + i)
			));
			filterGraph.addFilterChain(FilterChain.of(
				Filter.withName("asetpts").addArgument("PTS-STARTPTS").addInputLink(i + ":a").addOutputLink("a" + i)

			));

		}

		GenericFilter concatFilter = Filter.withName("concat")
			.addArgument("n", String.valueOf(videoPathList.size()))
			.addArgument("v", "1")
			.addArgument("a", "1")
			.addOutputLink("v_concat")
			.addOutputLink("a_concat");
		for (int i = 0; i < videoPathList.size(); i++) {
			concatFilter.addInputLink("v" + i);
			concatFilter.addInputLink("a" + i);
		}
		filterGraph.addFilterChain(FilterChain.of(concatFilter)
		);
		fFmpeg.setComplexFilter(filterGraph);
		fFmpeg.addOutput(UrlOutput.toUrl(outputPath))
			.addArguments("-map", "[v_concat]")
			.addArguments("-map", "[a_concat]")
			.execute();

		Path path = Paths.get(CURRENT_WORKING_DIR + "/" + outputPath);
		videoPathList.add(path);

		//s3에 업로드 후 url 반환
		String url = s3Util.uploadLocalFile(path, "preview/" + outputPath);

		//임시 파일 삭제 기능
		deleteTemporaryFile(videoPathList);

		TempPreview tempPreview = TempPreview.builder()
			.id(tempPreviewDto.getVersionId())
			.s3Url(url)
			.member(member.get())
			.build();
		tempPreviewRepository.save(tempPreview);

		return url;
	}

	private void createAndUploadM3U8(Video videoEntity, int size) {

		String[] versions = {"P144", "P360", "P480", "P720"};
		List<Path> pathList = new ArrayList<>();
		for (String version : versions) {
			StringBuilder m3u8Content = new StringBuilder();
			m3u8Content.append("#EXTM3U\n");
			m3u8Content.append("#EXT-X-VERSION:3\n");
			m3u8Content.append("#EXT-X-TARGETDURATION:5\n");
			for (int i = 0; i < size; i++) {
				m3u8Content.append("#EXTINF:5,\n");
				m3u8Content.append(
					S3_URL + ENCODED_FOLDER + "/" + videoEntity.getVideoUuid() + "/" + version + "/" + String.format(
						"%03d.ts", i));
				m3u8Content.append("\n");
			}
			m3u8Content.append("#EXT-X-ENDLIST\n");

			Path m3u8FilePath = Paths.get(version + ".m3u8");
			try (BufferedWriter writer = Files.newBufferedWriter(m3u8FilePath)) {
				writer.write(m3u8Content.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			pathList.add(m3u8FilePath);
		}
		s3Util.uploadLocalFileByStringFormat(pathList, ENCODED_FOLDER + "/" + videoEntity.getVideoUuid());

		for (Path path : pathList) {
			try {
				Files.delete(path);
				log.info("Deleted file: {}", path);
			} catch (IOException e) {
				log.info("File delete fail.");
			}
		}

	}

	private void persistAndSendVideoSegment(String folderPath, Video videoEntity, List<Path> pathList) {
		List<VideoSegment> videoSegmentList = new ArrayList<>();
		for (int i = 0; i < pathList.size(); i++) {
			videoSegmentList.add(VideoSegment.builder()
				.videoUrl(
					folderPath + "/" + videoEntity.getVideoUuid() + "/" + pathList.get(i).getFileName().toString())
				.videoSequence(i)
				.videoName(videoEntity.getVideoUuid())
				.videoEntity(videoEntity)
				.build());
		}
		videoSegmentRepository.saveAll(videoSegmentList);

		List<VideoEncodingDynamoTable> videoEncodingDynamoTableList = saveJopToDynamoDB(videoSegmentList);

		encodingKinesisService.publishUuidListToKinesis(
			videoEncodingDynamoTableList.stream()
				.map(VideoEncodingDynamoTable::getId)
				.collect(Collectors.toList()));

	}

	private List<VideoEncodingDynamoTable> saveJopToDynamoDB(List<VideoSegment> videoSegmentList) {
		List<VideoEncodingDynamoTable> videoEncodingDynamoTableList = new ArrayList<>();

		for (EncodeFormat encodeFormat : EncodeFormat.values()) {
			for (VideoSegment videoSegment : videoSegmentList) {
				videoEncodingDynamoTableList.add(VideoEncodingDynamoTable.builder()
					.id(UUID.randomUUID().toString())
					.rdbId(videoSegment.getId())
					.status("waiting")
					.format(encodeFormat.name())
					.build());
			}
		}

		dynamoService.videoEncodingListBatchSave(videoEncodingDynamoTableList);
		return videoEncodingDynamoTableList;
	}

	private FilterGraph generateFilter(Video videoEntity, int width, int height, Path assPath) throws
		Exception {
		FilterGraph filterGraph = new FilterGraph();

		int i = 0;
		for (Clip clip : videoEntity.getClips()) {
			filterGraph.addFilterChain(FilterChain.of(
				Filter.withName("setpts").addArgument("PTS-STARTPTS").addInputLink(i + ":v"),
				Filter.withName("scale")
					.addArgument("720")
					.addArgument("1280")
					.addOutputLink("v" + i)
			));
			filterGraph.addFilterChain(FilterChain.of(
				Filter.withName("asetpts").addArgument("PTS-STARTPTS").addInputLink(i + ":a"),
				Filter.withName("volume")
					.addArgument(String.valueOf(clip.getVolume()))
					.addOutputLink("a" + i)
			));
			i++;
		}
		String audioConcat = "outa";
		if (videoEntity.hasAudio()) {
			filterGraph.addFilterChain(
				FilterChain.of(
					Filter.withName("volume")
						.addArgument(String.valueOf(videoEntity.getAudio().getVolume()))
						.addInputLink(videoEntity.getClips().size() + ":a")
						.addOutputLink("a_special")));
			audioConcat = "a_concat";
		}
		GenericFilter concatFilter = Filter.withName("concat")
			.addArgument("n", String.valueOf(videoEntity.getClips().size()))
			.addArgument("v", "1")
			.addArgument("a", "1")
			.addOutputLink("v_concat")
			.addOutputLink(audioConcat);

		for (i = 0; i < videoEntity.getClips().size(); i++) {
			concatFilter.addInputLink("v" + i);
			concatFilter.addInputLink("a" + i);
		}
		filterGraph.addFilterChain(FilterChain.of(concatFilter)
		);
		if (videoEntity.hasAudio()) {

			filterGraph.addFilterChain(
				FilterChain.of(
					Filter.withName("amix")
						.addArgument("inputs", "2")
						.addArgument("duration", "first")
						.addInputLink("a_concat")
						.addInputLink("a_special")
						.addOutputLink("outa")
				)
			);

		}
		filterGraph.addFilterChain(
			FilterChain.of(
				Filter.withName("ass")
					.addArgument("'" + assPath.toAbsolutePath().toString() + "'")
					// .addArgumentEscaped(modifyWindowPathForFfmpeg(assPath.toAbsolutePath().toString()))
					.addInputLink("v_concat")
					.addOutputLink("outv")
			)
		);
		return filterGraph;
	}

	@Override
	public Path generateASSBySubtitles(Set<Subtitle> subtitles, String fileName) throws Exception {

		String assContent = createAssStringBySubtitles(subtitles);
		File tempFile = File.createTempFile(fileName, ".ass");

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile),
			StandardCharsets.UTF_8)) {
			writer.write(assContent);
		}

		return tempFile.getAbsoluteFile().toPath();
	}

	protected List<Path> getSegementPathList() {
		List<Path> result = new ArrayList<>();

		int index = 0;

		while (true) {
			Path filePath = Paths.get(CURRENT_WORKING_DIR + String.format("/%03d.mov", index));
			if (Files.exists(filePath)) {
				result.add(filePath);
			} else {
				break;
			}
			index++;
		}
		return result;
	}

	private String createAssStringBySubtitles(Set<Subtitle> subtitles) {
		StringBuilder assContent = new StringBuilder(subtitleHeader.getAssHeader());
		if (subtitles != null) {
			for (Subtitle subtitle : subtitles) {
				String startTime = millisecondsToTimeCode(subtitle.getStartTime());
				String endTime = millisecondsToTimeCode(subtitle.getEndTime());

				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append("{\\pos(")
					.append(subtitle.getPositionX())
					.append(',')
					.append(subtitle.getPositionY())
					.append(")\\fs")
					.append(20.0f * subtitle.getScale())
					.append("\\c")
					.append(convertToASSColor(subtitle.getColor()))
					.append('}')
					.append(subtitle.getText());

				assContent.append("Dialogue: ")
					.append(subtitle.getZIndex())
					.append(',')
					.append(startTime)
					.append(',')
					.append(endTime)
					.append(",Default,,0,0,0,,")
					.append(textBuilder)
					.append('\n');
			}
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
	public void executeSplitVideoIntoSegments(FFmpeg fFmpeg, int perSegmentSec, FilterGraph filter,
		String outputPath) throws
		Exception {
		fFmpeg
			.setComplexFilter(filter)
			.addOutput(UrlOutput.toPath(Path.of(outputPath))
				.addArguments("-r", "30")
				.addArguments("-g", "5")
				.addArguments("-f", "segment")
				.addArguments("-segment_time", String.valueOf(perSegmentSec))
				.addArguments("-segment_time_delta", "0.05")
				.addArgument("-copyts")
				.addArguments("-map", "[outv]")
				.addArguments("-map", "[outa]"))
			.execute();
	}

	@Override
	public void uploadTempFileToS3(List<Path> pathList, String folderName) throws Exception {
		s3Util.uploadLocalFileByStringFormat(pathList, folderName);
	}

	@Override
	public void deleteTemporaryFile(List<Path> pathList) throws Exception {
		for (Path path : pathList) {
			try {
				Files.delete(path);
				log.info("Deleted file: {}", path);
			} catch (IOException e) {
				log.info("File delete fail.");
			}
		}

	}

}
