const registerButton = document.getElementById("registerButton");

const registerForm = document.getElementById("registerForm");

registerForm.addEventListener("submit", function (event) {
  event.preventDefault();

  const name = document.getElementById("name").value.trim();

  const email = document.getElementById("email").value.trim();

  const password = document.getElementById("password").value;

  const result = document.getElementById("result");

  if (name === "" || email === "" || password === "") {
    result.textContent = "すべての項目を入力してください";

    return;
  }

  if (Array.from(name).length > 50) {
    result.textContent = "ユーザー名は50文字以内で入力してください";

    return;
  }

  if (password.length < 8 || password.length > 128) {
    result.textContent = "パスワードは8文字以上128文字以内で入力してください";

    return;
  }

  registerButton.disabled = true;
  result.textContent = "登録しています...";

  csrfFetch("/register", {
    method: "POST",

    headers: {
      "Content-Type": "application/json",
    },

    body: JSON.stringify({
      name: name,
      email: email,
      password: password,
    }),
  })
    .then(async (response) => {
      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(responseText || "登録に失敗しました");
      }

      return responseText;
    })
    .then((data) => {
      result.textContent = data;

      window.location.href = "/login";
    })
    .catch((error) => {
      console.error(error);

      result.textContent = error.message;
    })
    .finally(() => {
      registerButton.disabled = false;
    });
});
