const postButton = document.getElementById("postButton");

const postImageInput = document.getElementById("postImageInput");
const selectPostImageButton = document.getElementById("selectPostImageButton");
const postImagePreview = document.getElementById("postImagePreview");

const MAX_POST_IMAGES = 4;
const MAX_POST_IMAGE_SIZE = 5 * 1024 * 1024;
const MAX_POST_CONTENT_LENGTH = 140;

let selectedPostImages = [];

postButton.addEventListener("click", function () {
  const content = document.getElementById("content").value.trim();

  if (Array.from(content).length > MAX_POST_CONTENT_LENGTH) {
    document.getElementById("result").textContent =
      "投稿本文は140文字以内で入力してください";

    return;
  }

  if (content === "" && selectedPostImages.length === 0) {
    document.getElementById("result").textContent =
      "文字または画像を入力してください";

    return;
  }

  navigator.geolocation.getCurrentPosition(
    function (position) {
      const latitude = position.coords.latitude;
      const longitude = position.coords.longitude;

      const formData = new FormData();

      formData.append("content", content);
      formData.append("latitude", latitude);
      formData.append("longitude", longitude);

      for (const image of selectedPostImages) {
        formData.append("images", image);
      }

      postButton.disabled = true;

      csrfFetch("/posts", {
        method: "POST",
        body: formData,
      })
        .then(async (response) => {
          if (response.status === 401) {
            window.location.href = "/login";

            throw new Error("ログインが必要です");
          }

          if (!response.ok) {
            const errorText = await response.text();

            throw new Error(errorText || "投稿に失敗しました");
          }

          return response.json();
        })

        .then((data) => {
          document.getElementById("result").textContent = "投稿が完了しました";

          const contentElement = document.getElementById("content");

          if (contentElement) {
            contentElement.value = "";
          }

          selectedPostImages = [];

          postImageInput.value = "";
          postImagePreview.innerHTML = "";

          if (window.loadNearbyPosts) {
            window.loadNearbyPosts();
          }
        })

        .catch((error) => {
          console.error(error);

          document.getElementById("result").textContent =
            error.message || "投稿に失敗しました";
        })

        .finally(() => {
          postButton.disabled = false;
        });
    },

    function (error) {
      console.error(error);

      alert("現在地を取得できませんでした");
    },
  );
});

selectPostImageButton.addEventListener("click", function () {
  postImageInput.click();
});

postImageInput.addEventListener("change", function () {
  const files = Array.from(postImageInput.files);

  for (const file of files) {
    if (selectedPostImages.length >= MAX_POST_IMAGES) {
      alert("画像は4枚まで選択できます");

      break;
    }

    if (file.type !== "image/png" && file.type !== "image/jpeg") {
      alert("PNGまたはJPEG画像を選択してください");

      continue;
    }

    if (file.size > MAX_POST_IMAGE_SIZE) {
      alert("画像1枚のサイズは5MB以下にしてください");

      continue;
    }

    const alreadySelected = selectedPostImages.some(function (selectedFile) {
      return (
        selectedFile.name === file.name &&
        selectedFile.size === file.size &&
        selectedFile.lastModified === file.lastModified
      );
    });

    if (alreadySelected) {
      continue;
    }

    selectedPostImages.push(file);
  }

  postImageInput.value = "";

  renderPostImagePreview();
});

function renderPostImagePreview() {
  postImagePreview.innerHTML = "";

  selectedPostImages.forEach(function (file, index) {
    const previewItem = document.createElement("div");

    previewItem.classList.add("post_image_preview_item");

    const image = document.createElement("img");
    const previewUrl = URL.createObjectURL(file);

    image.src = previewUrl;
    image.alt = "投稿画像";

    image.onload = function () {
      URL.revokeObjectURL(previewUrl);
    };

    const removeButton = document.createElement("button");

    removeButton.type = "button";
    removeButton.textContent = "×";

    removeButton.classList.add("post_image_remove_button");

    removeButton.addEventListener("click", function () {
      selectedPostImages.splice(index, 1);

      renderPostImagePreview();
    });

    previewItem.appendChild(image);
    previewItem.appendChild(removeButton);

    postImagePreview.appendChild(previewItem);
  });
}
