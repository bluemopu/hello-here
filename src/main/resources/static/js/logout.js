const logoutButton = document.getElementById("logoutButton");

logoutButton.addEventListener("click", function () {
  csrfFetch("/logout", {
    method: "POST",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("ログアウトに失敗しました");
      }

      return response.text();
    })
    .then(() => {
      window.location.href = "/login";
    })
    .catch((error) => {
      console.error(error);
    });
});
