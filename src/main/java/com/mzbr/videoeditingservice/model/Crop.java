package com.mzbr.videoeditingservice.model;import javax.persistence.Entity;import javax.persistence.FetchType;import javax.persistence.GeneratedValue;import javax.persistence.GenerationType;import javax.persistence.Id;import javax.persistence.JoinColumn;import javax.persistence.OneToOne;import javax.persistence.Table;import lombok.AllArgsConstructor;import lombok.Builder;import lombok.Getter;import lombok.NoArgsConstructor;@Getter@Entity@NoArgsConstructor@AllArgsConstructor@Builder@Table(name = "crop")public class Crop {	@Id	@GeneratedValue(strategy = GenerationType.IDENTITY)	Long id;	Integer startX;	Integer startY;	Float zoomFactor;	@OneToOne	@JoinColumn(name = "clip_id") // 이 부분은 실제 데이터베이스 테이블의 컬럼 이름에 맞춰 수정해야 합니다.	Clip clip;}