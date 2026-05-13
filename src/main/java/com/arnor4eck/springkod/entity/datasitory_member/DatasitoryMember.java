package com.arnor4eck.springkod.entity.datasitory_member;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "datasitory_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"datasitory_id", "user_id"})
        })
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class DatasitoryMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasitory_id", nullable = false)
    private Datasitory datasitory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
