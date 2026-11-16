package com.hellohere.hello_here.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hellohere.hello_here.entity.Post;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<Post> findByIdAndUser_IdAndDeletedAtIsNull(Long postId, Long userId);

    @Query(value = """
            SELECT
                p.id AS id,
                p.content AS content,
                p.created_at AS createdAt,


                COALESCE(
                    up.display_name,
                    'Unknown'
                ) AS displayName,

                up.avatar_url AS avatarUrl,

                GROUP_CONCAT(
                    pi.image_url
                    ORDER BY pi.id
                    SEPARATOR '||'
                ) AS imageUrls,

                ST_Distance_Sphere(
                    POINT(
                        p.longitude,
                        p.latitude
                    ),
                    POINT(
                        :longitude,
                        :latitude
                    )
                ) AS distance

            FROM posts p

            LEFT JOIN user_profiles up
                ON up.user_id = p.user_id

            LEFT JOIN post_images pi
                ON pi.post_id = p.id

            WHERE
                p.deleted_at IS NULL
                AND p.latitude IS NOT NULL
                AND p.longitude IS NOT NULL

            GROUP BY
                p.id,
                p.content,
                p.created_at,
                p.latitude,
                p.longitude,
                up.display_name,
                up.avatar_url

            HAVING distance <= 100

            ORDER BY distance ASC

            LIMIT 100
            """, nativeQuery = true)

    List<NearbyPostProjection> findNearbyPosts(@Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude);
}


