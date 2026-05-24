package com.arnor4eck.springkod.entity.datasitory_file;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "datasitory_files",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"datasitory_id", "file_id"})
        })
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class DatasitoryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasitory_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Datasitory datasitory;

    @Column(nullable = false, name = "file_id")
    private String fileId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;
}
