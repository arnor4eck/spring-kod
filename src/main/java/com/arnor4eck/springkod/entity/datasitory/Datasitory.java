package com.arnor4eck.springkod.entity.datasitory;

import com.arnor4eck.springkod.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "datasitory")
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Datasitory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DatasitoryType datasitoryType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DatasitoryStructure datasitoryStructure = DatasitoryStructure.IMAGES_AND_FILE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
