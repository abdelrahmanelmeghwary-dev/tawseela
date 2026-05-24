package com.tawseela.entity;

import com.tawseela.common.BaseEntity;
import com.tawseela.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(nullable = false, length = 64)
    private String phone;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    /** Legacy Supabase column; nullable on fresh schemas (see V11). */
    @Column(name = "role", length = 32)
    private String role;
}
