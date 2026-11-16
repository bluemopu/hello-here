const loginButton = document.getElementById("loginButton");

const signUpButton = document.getElementById("signUpButton");

const loginForm = document.getElementById("loginForm");

const result = document.getElementById("result");

loginForm.addEventListener("submit", function (event) {
  event.preventDefault();

  const email = document.getElementById("email").value.trim();

  const password = document.getElementById("password").value;

  if (email === "" || password === "") {
    result.textContent = "メールアドレスとパスワードを入力してください";

    return;
  }

  loginButton.disabled = true;
  result.textContent = "ログインしています...";

  csrfFetch("/login", {
    method: "POST",

    headers: {
      "Content-Type": "application/json",
    },

    body: JSON.stringify({
      email: email,
      password: password,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("ログインに失敗しました");
      }

      return response.text();
    })
    .then(() => {
      window.location.href = "/home";
    })
    .catch((error) => {
      console.error(error);

      result.textContent = "メールアドレスまたはパスワードが違います";
    })
    .finally(() => {
      loginButton.disabled = false;
    });
});

signUpButton.addEventListener("click", function () {
  window.location.href = "/register";
});
