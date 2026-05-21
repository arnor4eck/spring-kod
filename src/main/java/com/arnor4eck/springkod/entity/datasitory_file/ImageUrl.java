package com.arnor4eck.springkod.entity.datasitory_file;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image_url")
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ImageUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasitory_file_id", nullable = false)
    private DatasitoryFile datasitoryFile;

    @Column(nullable = false, name = "url")
    private String url;
}
