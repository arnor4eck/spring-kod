package com.arnor4eck.springkod.entity.datasitory_member;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "datasitory_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Datasitory datasitory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DatasitoryMemberRole datasitoryMemberRole;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
