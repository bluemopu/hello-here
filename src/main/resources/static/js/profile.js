const result = document.getElementById("result");

const saveProfileButton = document.getElementById("saveProfileButton");

const avatarInput = document.getElementById("avatarInput");

const selectAvatarButton = document.getElementById("selectAvatarButton");

const uploadAvatarButton = document.getElementById("uploadAvatarButton");

const profileAvatar = document.getElementById("profileAvatar");

const avatarEditorDialog = document.getElementById("avatarEditorDialog");

const avatarCanvas = document.getElementById("avatarCanvas");

const avatarContext = avatarCanvas.getContext("2d");

const avatarZoom = document.getElementById("avatarZoom");

const rotateLeftButton = document.getElementById("rotateLeftButton");

const rotateRightButton = document.getElementById("rotateRightButton");

const cancelAvatarButton = document.getElementById("cancelAvatarButton");

const applyAvatarButton = document.getElementById("applyAvatarButton");

const MAX_FILE_SIZE = 5 * 1024 * 1024;

const MIN_IMAGE_SIZE = 200;

const MAX_IMAGE_SIZE = 4096;

const OUTPUT_IMAGE_SIZE = 512;

let editorImage = null;

let editedAvatarBlob = null;

let previewObjectUrl = null;

let rotation = 0;

let zoom = 1;

let offsetX = 0;

let offsetY = 0;

let dragging = false;

let previousPointerX = 0;

let previousPointerY = 0;

function setResult(message) {
  result.textContent = message;
}

function redirectIfLoggedOut(response) {
  if (
    response.status === 401 ||
    (response.redirected && response.url.includes("/login"))
  ) {
    window.location.href = "/login";

    return true;
  }

  return false;
}

function resetEditorState() {
  rotation = 0;

  zoom = 1;

  offsetX = 0;

  offsetY = 0;

  dragging = false;

  avatarZoom.value = 1;
}

function drawAvatarEditor() {
  if (!editorImage) {
    return;
  }

  const canvasWidth = avatarCanvas.width;

  const canvasHeight = avatarCanvas.height;

  avatarContext.clearRect(0, 0, canvasWidth, canvasHeight);

  const normalizedRotation = ((rotation % 360) + 360) % 360;

  const rotated = normalizedRotation === 90 || normalizedRotation === 270;

  const rotatedWidth = rotated
    ? editorImage.naturalHeight
    : editorImage.naturalWidth;

  const rotatedHeight = rotated
    ? editorImage.naturalWidth
    : editorImage.naturalHeight;

  const baseScale = Math.max(
    canvasWidth / rotatedWidth,
    canvasHeight / rotatedHeight,
  );

  const scale = baseScale * zoom;

  const drawWidth = editorImage.naturalWidth * scale;

  const drawHeight = editorImage.naturalHeight * scale;

  const boundingWidth = rotated ? drawHeight : drawWidth;

  const boundingHeight = rotated ? drawWidth : drawHeight;

  const maxOffsetX = Math.max(0, (boundingWidth - canvasWidth) / 2);

  const maxOffsetY = Math.max(0, (boundingHeight - canvasHeight) / 2);

  offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX));

  offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY));

  avatarContext.save();

  avatarContext.translate(
    canvasWidth / 2 + offsetX,
    canvasHeight / 2 + offsetY,
  );

  avatarContext.rotate((rotation * Math.PI) / 180);

  avatarContext.drawImage(
    editorImage,

    -drawWidth / 2,
    -drawHeight / 2,

    drawWidth,
    drawHeight,
  );

  avatarContext.restore();
}

if (saveProfileButton) {
  saveProfileButton.addEventListener("click", function () {
    const displayName = document.getElementById("displayName").value.trim();

    const bio = document.getElementById("bio").value.trim();

    if (displayName === "") {
      setResult("表示名を入力してください");

      return;
    }

    if (Array.from(displayName).length > 50) {
      setResult("表示名は50文字以内で入力してください");

      return;
    }

    if (Array.from(bio).length > 160) {
      setResult("自己紹介は160文字以内で入力してください");

      return;
    }

    saveProfileButton.disabled = true;
    setResult("プロフィールを更新しています...");

    csrfFetch("/profile/update", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        displayName: displayName,
        bio: bio,
      }),
    })
      .then(async (response) => {
        if (redirectIfLoggedOut(response)) {
          return;
        }

        const responseText = await response.text();

        if (!response.ok) {
          throw new Error(responseText || "プロフィールの更新に失敗しました");
        }

        return responseText;
      })
      .then((responseText) => {
        if (!responseText) {
          return;
        }

        setResult(responseText);
      })
      .catch((error) => {
        console.error("プロフィール更新エラー:", error);

        setResult(error.message);
      })
      .finally(() => {
        saveProfileButton.disabled = false;
      });
  });
}

selectAvatarButton.addEventListener("click", function () {
  avatarInput.click();
});

avatarInput.addEventListener("change", function () {
  const file = avatarInput.files[0];

  if (!file) {
    return;
  }

  if (
    file.type !== "image/png" &&
    file.type !== "image/jpeg" &&
    file.type !== "image/gif"
  ) {
    setResult("PNG、JPEG画像またはGIF画像を選択してください");

    avatarInput.value = "";

    return;
  }

  if (file.size > MAX_FILE_SIZE) {
    setResult("画像サイズは5MB以下にしてください");

    avatarInput.value = "";

    return;
  }

  const image = new Image();

  const objectUrl = URL.createObjectURL(file);

  image.onload = function () {
    if (
      image.naturalWidth < MIN_IMAGE_SIZE ||
      image.naturalHeight < MIN_IMAGE_SIZE
    ) {
      URL.revokeObjectURL(objectUrl);

      setResult("画像は縦横200px以上にしてください");

      avatarInput.value = "";

      return;
    }

    if (
      image.naturalWidth > MAX_IMAGE_SIZE ||
      image.naturalHeight > MAX_IMAGE_SIZE
    ) {
      URL.revokeObjectURL(objectUrl);

      setResult("画像は縦横4096px以下にしてください");

      avatarInput.value = "";

      return;
    }

    editorImage = image;

    resetEditorState();

    drawAvatarEditor();

    avatarEditorDialog.showModal();

    setResult("");

    URL.revokeObjectURL(objectUrl);
  };

  image.onerror = function () {
    URL.revokeObjectURL(objectUrl);

    setResult("画像を読み込めませんでした");

    avatarInput.value = "";
  };

  image.src = objectUrl;
});

avatarZoom.addEventListener("input", function () {
  zoom = Number(avatarZoom.value);

  drawAvatarEditor();
});

rotateLeftButton.addEventListener("click", function () {
  rotation -= 90;

  offsetX = 0;
  offsetY = 0;

  drawAvatarEditor();
});

rotateRightButton.addEventListener("click", function () {
  rotation += 90;

  offsetX = 0;
  offsetY = 0;

  drawAvatarEditor();
});

avatarCanvas.addEventListener("pointerdown", function (event) {
  if (!editorImage) {
    return;
  }

  dragging = true;

  previousPointerX = event.clientX;

  previousPointerY = event.clientY;

  avatarCanvas.setPointerCapture(event.pointerId);
});

avatarCanvas.addEventListener("pointermove", function (event) {
  if (!dragging) {
    return;
  }

  const rectangle = avatarCanvas.getBoundingClientRect();

  const canvasScaleX = avatarCanvas.width / rectangle.width;

  const canvasScaleY = avatarCanvas.height / rectangle.height;

  const moveX = (event.clientX - previousPointerX) * canvasScaleX;

  const moveY = (event.clientY - previousPointerY) * canvasScaleY;

  offsetX += moveX;

  offsetY += moveY;

  previousPointerX = event.clientX;

  previousPointerY = event.clientY;

  drawAvatarEditor();
});

avatarCanvas.addEventListener("pointerup", function (event) {
  dragging = false;

  if (avatarCanvas.hasPointerCapture(event.pointerId)) {
    avatarCanvas.releasePointerCapture(event.pointerId);
  }
});

avatarCanvas.addEventListener("pointercancel", function () {
  dragging = false;
});

cancelAvatarButton.addEventListener("click", function () {
  avatarEditorDialog.close();

  editorImage = null;

  editedAvatarBlob = null;

  avatarInput.value = "";

  resetEditorState();

  setResult("画像の編集をキャンセルしました");
});

applyAvatarButton.addEventListener("click", function () {
  if (!editorImage) {
    setResult("編集する画像がありません");

    return;
  }

  const outputCanvas = document.createElement("canvas");

  outputCanvas.width = OUTPUT_IMAGE_SIZE;

  outputCanvas.height = OUTPUT_IMAGE_SIZE;

  const outputContext = outputCanvas.getContext("2d");

  outputContext.drawImage(
    avatarCanvas,

    0,
    0,
    avatarCanvas.width,
    avatarCanvas.height,

    0,
    0,
    OUTPUT_IMAGE_SIZE,
    OUTPUT_IMAGE_SIZE,
  );

  outputCanvas.toBlob(
    function (blob) {
      if (!blob) {
        setResult("画像の編集に失敗しました");

        return;
      }

      editedAvatarBlob = blob;

      if (previewObjectUrl) {
        URL.revokeObjectURL(previewObjectUrl);
      }

      previewObjectUrl = URL.createObjectURL(blob);

      profileAvatar.src = previewObjectUrl;

      avatarEditorDialog.close();

      setResult("編集した画像を確認して、アイコンを変更してください");
    },

    "image/png",
  );
});

uploadAvatarButton.addEventListener("click", function () {
  if (!editedAvatarBlob) {
    setResult("画像を選択して編集してください");

    return;
  }

  if (editedAvatarBlob.size > MAX_FILE_SIZE) {
    setResult("編集後の画像サイズが5MBを超えています");

    return;
  }

  const formData = new FormData();

  formData.append("avatar", editedAvatarBlob, "avatar.png");

  setResult("アイコンを保存しています...");
  uploadAvatarButton.disabled = true;

  csrfFetch("/profile/avatar", {
    method: "POST",
    body: formData,
  })
    .then(async (response) => {
      if (redirectIfLoggedOut(response)) {
        return;
      }

      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(responseText || "画像のアップロードに失敗しました");
      }

      return responseText;
    })
    .then((avatarUrl) => {
      if (!avatarUrl) {
        return;
      }

      if (previewObjectUrl) {
        URL.revokeObjectURL(previewObjectUrl);

        previewObjectUrl = null;
      }

      profileAvatar.src = avatarUrl + "?t=" + Date.now();

      editedAvatarBlob = null;

      editorImage = null;

      avatarInput.value = "";

      resetEditorState();

      setResult("アイコンを変更しました");
    })
    .catch((error) => {
      console.error("アイコン変更エラー:", error);

      setResult(error.message || "アイコンの変更に失敗しました");
    })
    .finally(() => {
      uploadAvatarButton.disabled = false;
    });
});

const deletePostButtons = document.querySelectorAll(".delete_post_button");

deletePostButtons.forEach(function (button) {
  button.addEventListener("click", async function () {
    const postId = button.dataset.postId;

    if (!postId || !window.confirm("この投稿を削除しますか？")) {
      return;
    }

    button.disabled = true;

    try {
      const response = await csrfFetch("/posts/" + postId, {
        method: "DELETE",
      });

      if (response.status === 401) {
        window.location.href = "/login";
        return;
      }

      if (response.status === 404) {
        throw new Error("投稿が見つからないか、削除する権限がありません");
      }

      if (!response.ok) {
        throw new Error("投稿の削除に失敗しました");
      }

      const postElement = button.closest(".profile_post");

      if (postElement) {
        postElement.remove();
      }

      setResult("投稿を削除しました");

      const remainingPosts = document.querySelectorAll(".profile_post");

      if (remainingPosts.length === 0) {
        showNoPostMessage();
      }
    } catch (error) {
      console.error("投稿削除エラー:", error);
      setResult(error.message);
    } finally {
      if (button.isConnected) {
        button.disabled = false;
      }
    }
  });
});

function showNoPostMessage() {
  const profilePosts = document.querySelector(".profile_posts");

  if (!profilePosts) {
    return;
  }

  const existingMessage = profilePosts.querySelector(".no_profile_posts");

  if (existingMessage) {
    existingMessage.style.display = "block";

    return;
  }

  const message = document.createElement("div");

  message.classList.add("no_profile_posts");

  message.textContent = "まだ投稿はありません";

  profilePosts.appendChild(message);
}
