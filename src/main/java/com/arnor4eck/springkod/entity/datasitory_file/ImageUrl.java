package com.arnor4eck.springkod.entity.datasitory_file;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DatasitoryFile datasitoryFile;

    @Column(nullable = false, name = "url")
    private String url;
}
