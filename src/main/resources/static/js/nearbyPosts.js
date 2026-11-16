function loadNearbyPosts() {
  const nearbyPosts = document.getElementById("nearbyPosts");

  nearbyPosts.setAttribute("aria-busy", "true");

  if (!navigator.geolocation) {
    nearbyPosts.setAttribute("aria-busy", "false");
    nearbyPosts.textContent = "このブラウザでは位置情報を利用できません";
    return;
  }

  navigator.geolocation.getCurrentPosition(
    function (position) {
      const latitude = position.coords.latitude;
      const longitude = position.coords.longitude;

      csrfFetch("/posts/nearby", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          latitude: latitude,
          longitude: longitude,
        }),
      })
        .then((response) => {
          if (response.status === 401) {
            window.location.href = "/login";

            throw new Error("ログインが必要です");
          }

          if (!response.ok) {
            throw new Error("投稿の取得に失敗しました");
          }

          return response.json();
        })

        .then((posts) => {
          const nearbyPosts = document.getElementById("nearbyPosts");

          nearbyPosts.innerHTML = "";
          nearbyPosts.setAttribute("aria-busy", "false");

          if (posts.length === 0) {
            const message = document.createElement("div");

            message.classList.add("no_posts");

            message.textContent = "現在地から100m以内に投稿はありません";

            nearbyPosts.appendChild(message);

            return;
          }

          posts.forEach((post) => {
            const postElement = document.createElement("article");

            postElement.classList.add("post");

            const userElement = document.createElement("div");

            userElement.classList.add("post_user");

            const iconElement = document.createElement("img");

            iconElement.classList.add("user_icon");

            if (post.avatarUrl != null && post.avatarUrl !== "") {
              iconElement.src = post.avatarUrl;
            } else {
              iconElement.src = "/image/default_avatar.png";
            }

            iconElement.alt = "";

            const nameElement = document.createElement("div");

            nameElement.classList.add("user_name");

            nameElement.textContent = post.displayName ?? "Unknown";

            userElement.appendChild(iconElement);

            userElement.appendChild(nameElement);

            let contentElement = null;

            if (post.content != null && post.content.trim() !== "") {
              contentElement = document.createElement("div");
              contentElement.classList.add("nearby_post_content");
              contentElement.textContent = post.content;
            }

            let imagesElement = null;

            if (post.imageUrls && post.imageUrls.length > 0) {
              imagesElement = document.createElement("div");

              imagesElement.classList.add("post_images");

              post.imageUrls.forEach(function (imageUrl) {
                const postImage = document.createElement("img");

                postImage.src = imageUrl;

                postImage.alt = "投稿画像";

                postImage.classList.add("post_image");

                imagesElement.appendChild(postImage);
              });
            }

            const infoElement = document.createElement("div");

            infoElement.classList.add("post_info");

            let createdAt = "日時不明";

            if (post.createdAt != null) {
              const date = new Date(post.createdAt);

              createdAt = date.toLocaleString("ja-JP", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
              });
            }

            infoElement.textContent = `${post.distance}m ・ ${createdAt}`;

            postElement.appendChild(userElement);

            if (contentElement != null) {
              postElement.appendChild(contentElement);
            }

            if (imagesElement != null) {
              postElement.appendChild(imagesElement);
            }

            postElement.appendChild(infoElement);

            nearbyPosts.appendChild(postElement);
          });
        })

        .catch((error) => {
          console.error(error);

          const nearbyPosts = document.getElementById("nearbyPosts");

          nearbyPosts.setAttribute("aria-busy", "false");
          nearbyPosts.textContent = "投稿を取得できませんでした";
        });
    },

    function (error) {
      console.error(error);

      const nearbyPosts = document.getElementById("nearbyPosts");

      nearbyPosts.setAttribute("aria-busy", "false");
      nearbyPosts.textContent = "位置情報を取得できませんでした";
    },
    { timeout: 10000, maximumAge: 30000 },
  );
}

window.loadNearbyPosts = loadNearbyPosts;

loadNearbyPosts();
