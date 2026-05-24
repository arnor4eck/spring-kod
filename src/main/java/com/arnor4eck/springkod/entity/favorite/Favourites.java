package com.arnor4eck.springkod.entity.favorite;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "datasitory_favorite",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"datasitory_id", "user_id"})
        })
public class Favourites {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name="datasitory_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Datasitory datasitory;

    @Column(nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Favourites(User user, Datasitory datasitory){
        this.user = user;
        this.datasitory = datasitory;
    }
}
